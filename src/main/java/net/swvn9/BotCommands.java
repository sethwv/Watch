package net.swvn9;

import com.mikebull94.rsapi.RuneScapeAPI;
import com.mikebull94.rsapi.hiscores.ClanMate;
import com.mikebull94.rsapi.hiscores.Hiscores;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.vdurmont.emoji.EmojiManager;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.hooks.*;
import net.dv8tion.jda.core.managers.GuildController;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.swvn9.BotEvent.WHITELIST;
import static net.swvn9.BotEvent.icon;

@SuppressWarnings("unused")
class BotCommand {
    //BotCommand(String node,Long ratelimit){
    //    this.node = node;
    //    this.ratelimit = ratelimit;
    //}
    BotCommand(String node) {
        this.node = node;
        this.ratelimit = 10L;
        this.watchfile = new File("Commands" + File.separator + (node.replace("command.", "").replace("#all", "")) + ".watch");
        this.help();
        BotCommands.commandList.add(this);
        if (savemem) {
            try {
                if (!watchfile.exists()) {
                    FileWriter newFile = new FileWriter(watchfile, false);
                    newFile.close();
                } else {
                    FileReader openfile = new FileReader(watchfile);
                    Scanner readfile = new Scanner(openfile);
                    while (readfile.hasNext()) {
                        memory.add(readfile.next());
                    }
                    readfile.close();
                    openfile.close();
                }
            } catch (IOException ignored) {

            }
        }
    }

    protected final String node;
    private final Long ratelimit;
    private final File watchfile;


    long getratelimit() {
        return this.ratelimit;
    }

    protected int shard=0;
    protected final HashSet<String> memory = new HashSet<>();
    protected Message message;
    protected Guild guild;
    protected MessageChannel channel;
    protected User user;
    protected String commandargs;
    protected BotUser botUser;
    protected boolean waiting = false;
    protected LocalDateTime Lastrun = LocalDateTime.now().minusYears(10L);
    protected MessageChannel lastchannel;
    protected boolean savemem = false;

    protected long start;

    protected String helpname = "Undefined";
    protected String helpusage = "Undefined";
    protected String helpdesc = "Undefined";
    protected Boolean skip = false;

    void help() {
        this.helpname = "Undefined";
        this.helpusage = "Undefined";
        this.helpdesc = "Undefined";
        this.skip = true;
    }

    void saveMemory() {
        if (savemem) {
            try {
                StringBuilder memstring = new StringBuilder();
                for (String s : memory) {
                    memstring.append(s).append(" ");
                }
                FileWriter writefile = new FileWriter(watchfile, false);
                writefile.append(memstring);
                writefile.close();
            } catch (IOException ignored) {
            }
        }
    }

    void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    void run(Message m) {
        for(JDA jda:Bot.jdas){
            if(jda.getGuilds().contains(m.getGuild())) shard = jda.getShardInfo().getShardId();
        }
        this.message = m;
        this.guild = m.getGuild();
        this.channel = m.getChannel();
        this.user = m.getAuthor();
        this.botUser = new BotUser(user, guild);
        //channel.sendTyping().queue();
        try{
            TimeUnit.MILLISECONDS.sleep(250);
        }catch(InterruptedException ignored){}
        if (node.contains("#all")) {
            this.commandargs = message.getContent().replaceFirst("(?i)::" + (node.replace("command.", "")).replace("#all", ""), "");
        } else {
            this.commandargs = message.getContent().replaceFirst("(?i)::" + (node.replace("command.", "")), "");
        }
        if (botUser.hasPermission(node) || botUser.isadmin() || node.contains("#all")) {
            if (LocalDateTime.now().isBefore(Lastrun) && !botUser.isadmin()) {
                long Seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), Lastrun);
                message.getChannel().sendMessage("<:Watch:326815513550389249> `You can run this command again in " + Seconds + " seconds.` `" + message.getContent() + "`").queue(msg -> msg.delete().queueAfter((int) Seconds, TimeUnit.SECONDS));
                this.cleanup(true);
                return;
            }
            this.Lastrun = LocalDateTime.now().plusSeconds(ratelimit);
            try {
                this.command();
            } catch (Exception eeeeee) {
                eeeeee.getStackTrace();
            }
            this.cleanup(true);
        } else {
            this.cleanup(false);
        }
    }
    void cleanup(boolean delete) {
        if (channel.getMessageById(message.getId())!=null && message.getChannel().getType().equals(ChannelType.TEXT) && delete) message.delete().queue();
        this.message = null;
        this.guild = null;
        this.channel = null;
        this.user = null;
        this.botUser = null;
        this.commandargs = null;
        saveMemory();
    }

    void command() throws Exception {
        message.getChannel().sendMessage("<:WatchWarn:326815513634406419> `This command has not been configured, node: " + this.node + "`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
    }

}

@SuppressWarnings("unused")
class BotCommands {
    public static final HashSet<BotCommand> commandList = new HashSet<>();
    public static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    // Command utility methods
    private static boolean comapare(String a, String b) {
        Levenshtein l = new Levenshtein();
        JaroWinkler jw = new JaroWinkler();
        return a.contains(b) || b.contains(a) || a.equalsIgnoreCase(b) || l.distance(a, b) < 2 || jw.similarity(a, b) > 0.89d;
    }

    // General commands
    public static BotCommand help = new BotCommand("command.help#all") {
        @Override
        void help() {
            this.helpname = "Help (This command)";
            this.helpusage = "::help <keyword> <-a,c>";
            this.helpdesc = "See all of the commands associated with the bot that you can use, sent to you in a dm unless specified otherwise.\n#Flags:\n<-a> All commands\n<-c> In current channel";
            this.skip = false;
        }

        @Override
        void command() {
            EmbedBuilder showCommands = new EmbedBuilder();
            showCommands.setColor(new Color(148, 168, 249));
            showCommands.setFooter("List of commands.", Bot.jdas.get(shard).getSelfUser().getAvatarUrl());
            boolean specific = false;
            String noargs = commandargs.replace("-a", "").replace("-c", "").trim();
            if (new Scanner(noargs).hasNext()) {
                String next = new Scanner(noargs).next();
                for (BotCommand bc : commandList) {
                    if ((bc.helpname.toLowerCase()).contains(next.toLowerCase())) {
                        specific = true;
                        showCommands.addField(bc.helpname, "```Markdown\n#Node: \n" + bc.node.replace("#all", "") + "\n#Usage: \n" + bc.helpusage + "\n#Description: \n" + bc.helpdesc + "```", true);
                    }
                }
            }
            for (BotCommand bc : commandList) {
                if (bc.skip || specific) continue;
                if (botUser.hasPermission(bc.node) || bc.node.contains("#all") || commandargs.contains("-a") || botUser.isadmin()) {
                    showCommands.addField(bc.helpname, "```Markdown\n#Node: \n" + bc.node.replace("#all", "") + "\n#Usage: \n" + bc.helpusage + "\n#Description: \n" + bc.helpdesc + "```", true);
                }
            }
            if (commandargs.contains("-c") && botUser.isadmin()) {
                channel.sendMessage(showCommands.build()).queue();
            } else {
                user.openPrivateChannel().complete().sendMessage(showCommands.build()).queue();
                message.getChannel().sendMessage("<:Watch:326815513550389249> `" + user.getName() + ", check your DMs!`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand inv = new BotCommand("command.inv") {
        @Override
        void help() {
            this.helpname = "Invite";
            this.helpusage = "::inv";
            this.helpdesc = "Generate a one-time-use invite that is valid for 24 hours";
            this.skip = false;
        }

        @Override
        void command() {
            String invcode;
            invcode = guild.getPublicChannel().createInvite().setMaxUses(1).setMaxAge(24L, TimeUnit.HOURS).setUnique(true).complete().getCode();
            channel.sendMessage("<:Watch:326815513550389249> `An invite has been created and sent to you `").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            user.openPrivateChannel().complete().sendMessage("Your invite is valid for **24 hours and one use**. the link is: http://discord.gg/" + invcode).queue();
        }
    };
    public static BotCommand info = new BotCommand("command.info#all") {
        @Override
        void command() {
            EmbedBuilder stuff = new EmbedBuilder();
            stuff.setTitle(user.getName() + "#" + user.getDiscriminator());
            stuff.setThumbnail(user.getAvatarUrl());
            stuff.setColor(new Color(148, 168, 249));
            DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("dd MMM yyyy");
            stuff.addField("ID", "`" + user.getId() + "`", true);
            stuff.addField("Effective Name", "`" + guild.getMember(user).getEffectiveName() + "`", true);
            stuff.addField("Discord Join Date", "`" + user.getCreationTime().format(dateformat) + "`", true);
            stuff.addField("Status", "`" + guild.getMember(user).getOnlineStatus().name() + "`", false);
            StringBuilder userroles = new StringBuilder();
            for (Role e : guild.getMember(user).getRoles()) {
                userroles.append("- ").append(e.getAsMention()).append("\n");
            }
            if (guild.getMember(user).getRoles().isEmpty()) userroles.append("`(none)`");
            stuff.addField("User Roles", userroles.toString(), false);
            channel.sendMessage(stuff.build()).queue();

            EmbedBuilder rolecases = new EmbedBuilder();
            rolecases.setTitle("Role Interaction Cases");
            rolecases.setColor(new Color(255, 255, 255));
            if (!message.getMentionedRoles().isEmpty()) {
                for (Role r : message.getMentionedRoles()) {
                    rolecases.addField(r.getName(), r.getAsMention() + "\t" + guild.getMember(user).canInteract(r), false);
                }
                channel.sendMessage(rolecases.build()).queue();
            }

            EmbedBuilder usercases = new EmbedBuilder();
            usercases.setTitle("User Interaction Cases");
            usercases.setColor(new Color(255, 255, 255));
            if (!message.getMentionedUsers().isEmpty()) {
                for (User u : message.getMentionedUsers()) {
                    usercases.addField(guild.getMember(u).getEffectiveName(), u.getAsMention() + "\t" + guild.getMember(user).canInteract(guild.getMember(u)), false);
                }
                channel.sendMessage(usercases.build()).queue();
            }
        }
    };

    // Mod/Admin commands
    public static BotCommand say = new BotCommand("command.say") {
        @Override
        void help() {
            this.helpname = "Say";
            this.helpusage = "::say <message>";
            this.helpdesc = "Send a message as the bot";
            this.skip = false;
        }

        @Override
        void command() {
            if (!commandargs.equals("")) {
                channel.sendMessage(message.getRawContent().replaceFirst("(?i)::say", "")).queue();
            }
        }
    };
    public static BotCommand ban = new BotCommand("command.ban") {
        @Override
        void help() {
            this.helpname = "Ban";
            this.helpusage = "::ban <mention(s)> <reason>";
            this.helpdesc = "Ban user(s) with an optional message";
            this.skip = false;
        }

        @Override
        void command() {
            if (!commandargs.equals("")) {
                TextChannel send = null;
                for (TextChannel c : guild.getTextChannels()) {
                    if (c.getName().equalsIgnoreCase("logs")) {
                        send = c;
                    }
                }
                for (User u : message.getMentionedUsers()) {
                    this.commandargs = commandargs.replace("@" + u.getName(), "").trim();
                    if (guild.getMember(user).canInteract(guild.getMember(u))) if (!u.isBot())
                        u.openPrivateChannel().complete().sendMessage("<:Watch:326815513550389249> You've been banned from " + guild.getName() + " by " + user.getAsMention() + " with the message `" + commandargs + "`.").queue();
                }
                commandargs = commandargs.substring(0, Math.min(commandargs.length(), 512));
                for (User u : message.getMentionedUsers()) {
                    if (guild.getMember(user).canInteract(guild.getMember(u))) {
                        if (send != null) {
                            EmbedBuilder log = new EmbedBuilder();
                            log.setColor(new Color(255, 0, 0));
                            log.addField("Action", "Ban", false);
                            log.addField("User", u.getName() + "#" + u.getDiscriminator() + " (" + u.getId() + ")", false);
                            log.addField("Moderator:", user.getName() + "#" + user.getDiscriminator(), false);
                            log.addField("Reason", commandargs, false);
                            log.setFooter(Bot.jdas.get(shard).getSelfUser().getName() + "#" + Bot.jdas.get(shard).getSelfUser().getDiscriminator(), Bot.jda.getSelfUser().getAvatarUrl());
                            log.setTimestamp(LocalDateTime.now());
                            send.sendMessage(log.build()).queue();
                        }
                        if (!u.equals(user)) guild.getController().ban(u, 6, commandargs).queue();
                        message.getChannel().sendMessage("<:Watch:326815513550389249> `" + user.getName() + " banned " + u.getName() + "#" + u.getDiscriminator() + " (" + commandargs + ")`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    } else {
                        message.getChannel().sendMessage("<:WatchError:326815514129072131> `Unable to ban " + u.getName() + "#" + u.getDiscriminator() + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    }
                }
                if (send != null)
                    message.getChannel().sendMessage("<:Watch:326815513550389249> `Bans have been logged in the `" + send.getAsMention() + "` channel.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            } else {
                message.getChannel().sendMessage("<:Watch:326815513550389249> `" + user.getName() + ", you need to mention at least one user ::ban @mention(s)`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand kick = new BotCommand("command.kick") {
        @Override
        void help() {
            this.helpname = "Kick";
            this.helpusage = "::kick <mention(s)> <reason>";
            this.helpdesc = "Kick user(s) with an optional message";
            this.skip = false;
        }

        @Override
        void command() {
            TextChannel send = null;
            for (TextChannel c : guild.getTextChannels()) {
                if (c.getName().equalsIgnoreCase("logs")) {
                    send = c;
                }
            }
            if (!commandargs.equals("")) {
                for (User u : message.getMentionedUsers()) {
                    this.commandargs = commandargs.replace("@" + u.getName(), "").trim();
                    if (guild.getMember(user).canInteract(guild.getMember(u))) if (!u.isBot())
                        u.openPrivateChannel().complete().sendMessage("<:Watch:326815513550389249> You've been kicked from " + guild.getName() + " by " + user.getAsMention() + " with the message `" + commandargs + "`.").queue();
                }
                commandargs = commandargs.substring(0, Math.min(commandargs.length(), 512));
                for (User u : message.getMentionedUsers()) {
                    if (guild.getMember(user).canInteract(guild.getMember(u))) {
                        if (send != null) {
                            EmbedBuilder log = new EmbedBuilder();
                            log.setColor(new Color(0, 0, 255));
                            log.addField("Action", "Kick", false);
                            log.addField("User", u.getName() + "#" + u.getDiscriminator() + " (" + u.getId() + ")", false);
                            log.addField("Moderator:", user.getName() + "#" + user.getDiscriminator(), false);
                            log.addField("Reason", commandargs, false);
                            log.setFooter(Bot.jdas.get(shard).getSelfUser().getName() + "#" + Bot.jdas.get(shard).getSelfUser().getDiscriminator(), Bot.jda.getSelfUser().getAvatarUrl());
                            log.setTimestamp(LocalDateTime.now());
                            send.sendMessage(log.build()).queue();
                        }
                        if (!u.equals(user)) guild.getController().kick(u.getId(), commandargs).queue();
                        message.getChannel().sendMessage("<:Watch:326815513550389249> `" + user.getName() + " kicked " + u.getName() + "#" + u.getDiscriminator() + " (" + commandargs + ")`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    } else {
                        message.getChannel().sendMessage("<:WatchError:326815514129072131> `Unable to kick " + u.getName() + "#" + u.getDiscriminator() + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    }
                }

            } else {
                message.getChannel().sendMessage("<:Watch:326815513550389249> `" + user.getName() + ", you need to mention at least one user ::ban @mention(s)`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand watch = new BotCommand("command.watch") {
        @Override
        void help() {
            this.helpname = "Watch";
            this.helpusage = "::watch <list/del/add> <keyword>";
            this.helpdesc = "Have the bot \"watch\" for certain keywords in chat, and log any occurrences to a channel called #logs\nKeywords are not case-sensitive";
            this.skip = false;
            this.savemem = true;
        }

        @Override
        void command() {
            if (!commandargs.equals("")) {
                Scanner read = new Scanner(commandargs);
                if (read.hasNext()) {
                    switch (read.next()) {
                        default:
                            message.getChannel().sendMessage("<:Watch:326815513550389249> `Invalid Syntax` `" + commandargs + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            break;
                        case "add":
                            if (read.hasNext()) {
                                String keyword = read.next().toLowerCase();
                                if (!memory.contains(keyword)) {
                                    memory.add(keyword);
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `" + keyword + " has been added to the watch filter. type ::watch del " + keyword + " to remove it.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                } else {
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `I am already watching for " + keyword + ".`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                }
                            } else {
                                message.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify a keyword!` `" + helpusage + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            }
                            break;
                        case "del":
                            if (read.hasNext()) {
                                String keyword = read.next().toLowerCase();
                                if (memory.contains(keyword)) {
                                    memory.remove(keyword);
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `" + keyword + " has been removed from the watch filter.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                } else {
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `I am not currently watching for " + keyword + ". Do ::watch add " + keyword + " to add it to the list.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                }
                            } else {
                                message.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify a keyword!` `" + helpusage + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            }
                            break;
                        case "list":
                            StringBuilder keywords = new StringBuilder();
                            for (String s : memory) {
                                keywords.append(s).append(", ");
                            }
                            keywords.deleteCharAt(keywords.length() - 1).deleteCharAt(keywords.length() - 1);
                            message.getChannel().sendMessage("<:Watch:326815513550389249> `Right now I'm watching for " + keywords + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            break;
                    }
                }
            } else {
                message.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify an action and a keyword!` `" + helpusage + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand purge = new BotCommand("command.purge") {
        @Override
        void help() {
            this.helpname = "Purge Messages";
            this.helpusage = "::purge <mentions> <number>";
            this.helpdesc = "Mentions are optional, if a number of messages to purge is not specified, it will be 10. Pinned messages will not be deleted.";
            this.skip = false;
        }

        @Override
        void command() throws Exception {
            this.commandargs = message.getRawContent().replaceFirst("(?i)::purge", "");
            this.commandargs = commandargs.replaceAll("<(?:@(?:[!&])?|#|:\\w{2,}:)\\d{17,}>", "").trim();
            List<String> remove = new ArrayList<>();
            Scanner args = new Scanner(commandargs);
            int limit;
            int arg;
            if (args.hasNextInt()) {
                arg = args.nextInt();
                limit = arg+2;
            } else {
                limit = 10;
            }
            int total = 0;
            Message msg = channel.sendMessage("<:Watch:326815513550389249> `Attempting to purge " + (limit-2) + " messages.`").complete();
            for (int i = 0; limit > 0; i++) {
                int todelete;
                if (limit > 100) {
                    todelete = 100;
                } else {
                    todelete = limit;
                }
                List<Message> toPurge = guild.getTextChannelById(channel.getId()).getIterableHistory().stream()
                        .limit(todelete)
                        .filter(m -> m.getCreationTime().isAfter(OffsetDateTime.now().minusDays(13)))
                        .filter(m -> !m.isPinned())
                        .filter(m -> !message.equals(m))
                        .filter(m -> !msg.equals(m))
                        .collect(Collectors.toList());
                limit -= 100;
                if (!message.getMentionedUsers().isEmpty())
                    toPurge = toPurge.stream().filter(m -> message.getMentionedUsers().contains(m.getAuthor())).collect(Collectors.toList());
                total += toPurge.size();
                try {
                    guild.getTextChannelById(channel.getId()).deleteMessages(toPurge).queue();

                }catch(Exception exc){
                    if(total==1) msg.editMessage("<:Watch:326815513550389249> `Purged " + total + " message.`").complete().delete().queueAfter(30, TimeUnit.SECONDS);
                    if(total!=1) msg.editMessage("<:Watch:326815513550389249> `Purged " + total + " messages.`").complete().delete().queueAfter(30, TimeUnit.SECONDS);
                    break;
                }
            }
            if(total==1) msg.editMessage("<:Watch:326815513550389249> `Purged " + total + " message.`").complete().delete().queueAfter(30, TimeUnit.SECONDS);
            if(total!=1) msg.editMessage("<:Watch:326815513550389249> `Purged " + total + " messages.`").complete().delete().queueAfter(30, TimeUnit.SECONDS);
        }
    };

    // Configuration commands
    public static BotCommand id = new BotCommand("command.id") {
        @Override
        void help() {
            this.helpname = "ID";
            this.helpusage = "::id";
            this.helpdesc = "Grab the ID and any permissions associated with your user ID.";
            this.skip = false;
        }

        @Override
        void command() {
            channel.sendMessage(user.getAsMention() + ", Your ID is " + user + ".").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            channel.sendMessage(botUser.getPermissions().toString()).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            channel.sendMessage(botUser.isadmin() + "").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    };
    public static BotCommand roles = new BotCommand("command.roles") {
        @Override
        void help() {
            this.helpname = "Roles";
            this.helpusage = "::roles";
            this.helpdesc = "Get all of the role-names and IDs associated with the current discord guild.";
            this.skip = false;
        }

        @Override
        void command() {
            EmbedBuilder roles = new EmbedBuilder();
            roles.setFooter("Roles for " + guild.getName(), Bot.jdas.get(shard).getSelfUser().getAvatarUrl());
            roles.setThumbnail(guild.getIconUrl());
            roles.setTimestamp(LocalDateTime.now());
            for (Object s : guild.getRoles().toArray()) {
                String trimmed = s.toString().replace("R:", "");
                StringBuilder sid = new StringBuilder();
                for (int i = trimmed.length() - 19; i < trimmed.length() - 1; i++) {
                    sid.append(trimmed.charAt(i));
                }
                StringBuilder sname = new StringBuilder();
                for (int i = 0; i < trimmed.length() - 20; i++) {
                    sname.append(trimmed.charAt(i));
                }
                roles.addField(sname.toString(), "`" + sid.toString() + "`", true);
            }
            roles.setColor(new Color(148, 168, 249));
            channel.sendMessage(roles.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
        }
    };
    public static BotCommand showconfig = new BotCommand("command.showconfig") {
        @Override
        void help() {
            this.helpname = "Show Config";
            this.helpusage = "::showconfig";
            this.helpdesc = "Spit out the contents of the Config.yml file to a rich embed.";
            this.skip = false;
        }

        @Override
        void command() {
            EmbedBuilder other = new EmbedBuilder();
            //StringBuilder whitelisted = new StringBuilder();
            other.setColor(new Color(148, 168, 249));
            for (String a : WHITELIST)
                //whitelisted.append("- `").append(a).append("`").append(System.lineSeparator());
                //other.addField("Whitelist", whitelisted.toString(), false);
                for (String key : Config.config.getGroups().keySet()) {
                    StringBuilder ids = new StringBuilder();
                    ids.append("Group IDs").append(System.lineSeparator());
                    for (String zz : Config.config.getGroups().get(key).id)
                        ids.append("- `").append(zz).append("`").append(System.lineSeparator());
                    other.addField(key, ids.toString(), true);
                    StringBuilder perms = new StringBuilder();
                    perms.append("Permissions").append(System.lineSeparator());
                    if (Config.config.getGroups().get(key).permissions != null) {
                        for (String zz : Config.config.getGroups().get(key).permissions)
                            perms.append("- `").append(zz).append("`").append(System.lineSeparator());
                    } else {
                        perms.append("`none`");
                    }
                    if (Config.config.getGroups().get(key).admin) {
                        other.addField("Type: SuperUser Group", perms.toString(), true);
                    } else {
                        other.addField("Type: User Group", perms.toString(), true);
                    }
                    //other.addField("Power: " + Config.config.getGroups().get(key).power, "\u200B", true);
                    other.addBlankField(true);
                }
            other.addBlankField(false);
            for (String key : Config.config.getUsers().keySet()) {
                other.addField(key, "User ID" + System.lineSeparator() + "`" + Config.config.getUsers().get(key).id + "`", true);
                StringBuilder perms = new StringBuilder();
                perms.append("Permissions").append(System.lineSeparator());
                HashSet<String> thing = new HashSet<>();
                if (Config.config.getUsers().get(key).permissions != null) {
                    thing.addAll(Config.config.getUsers().get(key).permissions);
                } else {
                    perms.append("`none`");
                }
                for (String zz : thing)
                    perms.append("- `").append(zz).append("`").append(System.lineSeparator());
                if (Config.config.getUsers().get(key).admin) {
                    other.addField("Type: SuperUser", perms.toString(), true);
                } else {
                    other.addField("Type: User", perms.toString(), true);
                }
                //other.addField("Power: " + Config.config.getUsers().get(key).power, "\u200B", true);
                other.addBlankField(true);
            }
            other.setFooter("Config.yml (May not show all entries)", Bot.jdas.get(shard).getSelfUser().getAvatarUrl());
            other.setTimestamp(LocalDateTime.now());
            channel.sendMessage(other.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
        }
    };
    public static BotCommand pullconfig = new BotCommand("command.pullconfig") {
        @Override
        void help() {
            this.helpname = "Pull Config";
            this.helpusage = "::pullconfig";
            this.helpdesc = "Pull the latest configuration from the Config.yml file.";
            this.skip = false;
        }

        @Override
        void command() {
            Config.loadConfig();
            WHITELIST = Config.getWhitelist();
            //channel.addReactionById(message.getId(), "👍").queue();
        }
    };

    // Owner commands
    public static BotCommand shard = new BotCommand("command.shard"){
        @Override
        void command() throws Exception {
            EmbedBuilder sharding = new EmbedBuilder();
            sharding.setColor(new Color(114, 137, 218));
            sharding.setTimestamp(LocalDateTime.now());
            sharding.setFooter("Watch #6969 ",Bot.jdas.get(shard).getSelfUser().getAvatarUrl());
            if(commandargs.contains("-l")){
                for(int i=0;i<Bot.jdas.size();i++){
                    sharding.addField("Shard "+Bot.jdas.get(i).getShardInfo().getShardId()+" of "+Bot.jdas.get(i).getShardInfo().getShardTotal(),"```java\n"+Bot.jdas.get(i).getGuilds().toString()+"```",false);
                }
            }
            channel.sendMessage(sharding.build()).queue();
        }
    };
    public static BotCommand kill = new BotCommand("command.kill") {
        @Override
        void help() {
            this.helpname = "Kill";
            this.helpusage = "::kill";
            this.helpdesc = "Kill the bot and return the host machine to the command line/desktop.";
            this.skip = false;
        }

        @Override
        void command() {
            if (channel.getType().equals(ChannelType.TEXT)) message.delete().queue();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException eeee) {
                eeee.getMessage();
            }
            System.exit(1);
        }
    };
    public static BotCommand stop = new BotCommand("command.stop") {
        @Override
        void help() {
            this.helpname = "Stop";
            this.helpusage = "::stop";
            this.helpdesc = "Stop the shard that the guild is running on. (There is no way to restart)";
            this.skip = false;
        }

        @Override
        void command() {
            if (channel.getType().equals(ChannelType.TEXT)) message.delete().queue();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException eeee) {
                eeee.getMessage();
            }
            Bot.jdas.get(shard).removeEventListener(Bot.jdas.get(shard).getRegisteredListeners());
            Bot.jdas.get(shard).shutdown(true);
        }
        @Override
        void cleanup(boolean delete) {}
    };
    public static BotCommand restart = new BotCommand("command.restart") {
        @Override
        void cleanup(boolean delete) {
        }

        @Override
        void help() {
            this.helpname = "Restart";
            this.helpusage = "::restart";
            this.helpdesc = "Restart the bot, re-initialise everything.";
            this.skip = false;
        }

        @Override
        void command() {
            if (channel.getType().equals(ChannelType.TEXT)) message.delete().queue();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException eeee) {
                eeee.getMessage();
            }
            Bot.restart(shard);
        }
    };
    public static BotCommand input = new BotCommand("command.input") {
        @Override
        void help() {
            this.helpname = "Enable/Disable Input";
            this.helpusage = "::input";
            this.helpdesc = "Enable or disable input for the bot so that the bot can stay running while testing.";
            this.skip = false;
        }

        @Override
        void command() {
            if (!waiting) {
                this.waiting = true;
                for(JDA jda:Bot.jdas){
                    jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                }
            } else {
                this.waiting = false;
                if(BotReady.isDevelopmentEnvironment()){
                    for(JDA jda:Bot.jdas){
                        jda.getPresence().setStatus(OnlineStatus.IDLE);
                        jda.getPresence().setGame(Game.of(icon.get(shard)+"in Dev Mode"));
                    }
                } else {
                    for(JDA jda:Bot.jdas){
                        jda.getPresence().setStatus(OnlineStatus.ONLINE);
                        jda.getPresence().setGame(Game.of(icon.get(shard)));
                    }
                }
            }
        }
    };
    public static BotCommand bot = new BotCommand("command.bot") {
        @Override
        void help() {
            this.helpname = "Bot Utility";
            this.helpusage = "::bot <-s/-a/-k> <?listener>";
            this.helpdesc = "You should probably stay away from everything this command does except for ::bot -s";
            this.skip = false;
        }

        @Override
        void command() {
            if (commandargs.contains("-s")) {
                EmbedBuilder stats = new EmbedBuilder();
                stats.setTimestamp(LocalDateTime.now());
                stats.setColor(new Color(114, 137, 218));
                stats.addField("Status", Bot.jdas.get(shard).getStatus().name(), false);
                stats.addField("Heartbeat", "" + Bot.jdas.get(shard).getPing() + "ms", true);
                stats.addField("API Responses", "" +Bot.jdas.get(shard).getResponseTotal() + "", true);

                stats.setFooter("Watch#6969 ", Bot.jdas.get(shard).getSelfUser().getAvatarUrl());

                long uptime = System.nanoTime() - start;
                long Days = TimeUnit.NANOSECONDS.toDays(uptime);
                long Hours = TimeUnit.NANOSECONDS.toHours(uptime - TimeUnit.DAYS.toNanos(Days));
                long Minutes = TimeUnit.NANOSECONDS.toMinutes(uptime - TimeUnit.HOURS.toNanos(Hours)-TimeUnit.DAYS.toNanos(Days));
                long Seconds = TimeUnit.NANOSECONDS.toSeconds(uptime - TimeUnit.MINUTES.toNanos(Minutes)-TimeUnit.HOURS.toNanos(Hours)-TimeUnit.DAYS.toNanos(Days));

                StringBuilder uptimeString = new StringBuilder();

                if (Days != 0) uptimeString.append(Days).append("d ");
                if (Hours != 0) uptimeString.append(Hours).append("h ");
                if (Minutes != 0) uptimeString.append(Minutes).append("m ");
                if (Seconds != 0) uptimeString.append(Seconds).append("s");

                stats.addField("Uptime", "" + uptimeString.toString() + "", true);
                StringBuilder cfr = new StringBuilder();
                int number = 1;
                for (String s : Bot.jdas.get(shard).getCloudflareRays()) {
                    cfr.append("<").append(number++).append("/").append(s).append(">\n");
                }
                stats.addField("Shard Info",Bot.jdas.get(shard).getShardInfo().toString(),true);
                stats.addField("CF Rays", "```Markdown\n" + cfr.toString() + "```", false);
                StringBuilder lst = new StringBuilder();
                int number2 = 0;
                for (Object obj : Bot.jdas.get(shard).getRegisteredListeners()) {
                    lst.append("<").append(number2++).append("/").append(obj.toString()).append(">\n");
                }
                stats.addField("Listeners", "```Markdown\n" + lst.toString() + "```", false);
                channel.sendMessage(stats.build()).queue();
            } else if (commandargs.contains("-k")) {
                commandargs = commandargs.replace("-k", "");
                Scanner ln = new Scanner(commandargs);
                if (ln.hasNextInt()) {
                    int chosen = ln.nextInt();
                    channel.sendMessage("```Markdown\nYou have removed listener <" + chosen + "/" + Bot.jdas.get(shard).getRegisteredListeners().get(chosen).toString() + ">```").queue();
                    Bot.jdas.get(shard).removeEventListener(Bot.jdas.get(shard).getRegisteredListeners().get(chosen));
                }
            } else if (commandargs.contains("-a")) {
                commandargs = commandargs.replace("-a", "");
                Scanner ln = new Scanner(commandargs);
                if (ln.hasNext()) {
                    String chosenL = ln.next();
                    switch (chosenL.toLowerCase()) {
                        case "botevent":
                            channel.sendMessage("```Markdown\nYou have added a listener <net.swvn9.BotEvent>```").queue();
                            Bot.jdas.get(shard).addEventListener(new BotEvent());
                            break;
                        case "botready":
                            channel.sendMessage("```Markdown\nYou have added a listener <net.swvn9.BotReady>```").queue();
                            Bot.jdas.get(shard).addEventListener(new BotReady());
                            break;
                        case "botlogging":
                            channel.sendMessage("```Markdown\nYou have added a listener <net.swvn9.BotLogging>```").queue();
                            Bot.jdas.get(shard).addEventListener(new BotLogging());
                            break;
                    }
                }
            }
        }
    };
    public static BotCommand eval = new BotCommand("command.eval") {
        @Override
        void help() {
            this.helpname = "Javascript Eval";
            this.helpusage = "::eval <line>";
            this.helpdesc = "Evaluate a line of JS";
            this.skip = false;
        }

        @Override
        void command() {
            try {
                engine.put("jda",Bot.jdas.get(shard));
                engine.put("channel",channel);
                engine.put("message",message);
                engine.put("guild",guild);
                engine.put("moji",EmojiManager.getAllTags());
                channel.sendMessage("```java\n//Evaluating\n" + commandargs.replaceAll("\n","").replaceAll(";",";\n").trim() + "```").queue();
                String res = engine.eval(commandargs).toString();
                if(res!=null)  channel.sendMessage("```js\n//Response\n" + res + "```").queue();
            } catch (Exception se) {
                if(se.getClass()!=NullPointerException.class) channel.sendMessage("```js\n//Exception\n" + se + "```").queue();
            }
        }
    };
    public static BotCommand link = new BotCommand("command.link") {
        @Override
        void help() {
            this.helpname = "Rebrandly Link";
            this.helpusage = "::link <url> <redirect>";
            this.helpdesc = "Using the rebrandly api token defined in the config file, create redirect links.";
            this.skip = false;
        }

        @Override
        void command() throws Exception {
            boolean info = false;
            if (commandargs.contains("-j")) {
                info = true;
                commandargs = commandargs.replace("-j", "").trim();
            }
            Scanner args = new Scanner(commandargs);
            String shortl = "";
            String ouath = "";
            if (args.hasNext()) ouath = args.next();
            if (args.hasNext()) shortl = args.next();
            if (!ouath.contains("http://") && !ouath.contains("https://")) ouath = "http://" + ouath;
            URL url = new URL("https://api.rebrandly.com/v1/links");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);//5 secs
            connection.setReadTimeout(5000);//5 secs
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("apikey", Config.getrebrandlyToken());
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write("{\"destination\": \"" + ouath + "\", \"slashtag\":\"" + shortl + "\", \"domain\": { \"fullName\": \"" + Config.getrebrandlyURL() + "\" }}");
            out.flush();
            out.close();
            int res = connection.getResponseCode();
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (res == 200) {
                    if (info)
                        channel.sendMessage("<:Watch:326815513550389249> **http://" + Config.getrebrandlyURL() + "/" + shortl + "** now links to **" + ouath + "**.\n```json\n" + line + "```").queue();
                    if (!info)
                        channel.sendMessage("<:Watch:326815513550389249> **http://" + Config.getrebrandlyURL() + "/" + shortl + "** now links to **" + ouath + "**.").queue();
                } else {
                    channel.sendMessage("<:WatchError:326815514129072131> " + res + " Something went wrong\n```JSON" + line + "```").queue();
                }
            }
            connection.disconnect();
        }
    };


    // RuneScape commands
    public static BotCommand clan = new BotCommand("command.clan") {
        @Override
        void help() {
            this.helpname = "RuneScape Clan Ranks";
            this.helpusage = "::clan";
            this.helpdesc = "(for now) Pull the upper ranks of the RuneScape clan Zamorak Cult, and match any names with those on the current discord guild.";
            this.skip = false;
        }

        @Override
        void command() {
            RuneScapeAPI api = RuneScapeAPI.createHttp();
            Hiscores hiscores = api.hiscores();

            String Clan = "Zamorak Cult";
            try {
                java.util.List<ClanMate> clanMates = hiscores.clanInformation(Clan);
                EmbedBuilder Ranks = new EmbedBuilder();
                StringBuilder One = new StringBuilder();
                if (guild.getId().equals("254861442799370240") && Clan.equals("Zamorak Cult"))
                    One.append(guild.getRoleById(254883837757227008L).getAsMention()).append(System.lineSeparator());
                StringBuilder Two = new StringBuilder();
                if (guild.getId().equals("254861442799370240") && Clan.equals("Zamorak Cult"))
                    Two.append(guild.getRoleById(268490396617801729L).getAsMention()).append(System.lineSeparator());
                StringBuilder Three = new StringBuilder();
                if (guild.getId().equals("254861442799370240") && Clan.equals("Zamorak Cult"))
                    Three.append(guild.getRoleById(258350529229357057L).getAsMention()).append(System.lineSeparator());
                StringBuilder Four = new StringBuilder();
                if (guild.getId().equals("254861442799370240") && Clan.equals("Zamorak Cult"))
                    Four.append(guild.getRoleById(254881136524656640L).getAsMention()).append(System.lineSeparator());
                StringBuilder Five = new StringBuilder();
                StringBuilder Six = new StringBuilder();
                int admins = StringUtils.countMatches(clanMates.toString(), "Admin") / 2 + 1;
                int admincount = 0;
                for (ClanMate a : clanMates) {
                    boolean found = false;
                    switch (a.getRank()) {
                        case "Owner":
                            for (Member b : guild.getMembers()) {
                                if (comapare(b.getEffectiveName(), a.getName())) {
                                    if (!One.toString().contains(b.getAsMention()))
                                        One.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) One.append(a.getName());
                            One.append(System.lineSeparator());
                            break;
                        case "Deputy Owner":
                            for (Member b : guild.getMembers()) {
                                if (comapare(b.getEffectiveName(), a.getName())) {
                                    if (!Two.toString().contains(b.getAsMention()))
                                        Two.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) Two.append(a.getName());
                            Two.append(System.lineSeparator());
                            break;
                        case "Overseer":
                            for (Member b : guild.getMembers()) {
                                if (comapare(b.getEffectiveName(), a.getName())) {
                                    if (!Three.toString().contains(b.getAsMention()))
                                        Three.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) Three.append(a.getName());
                            Three.append(System.lineSeparator());
                            break;
                        case "Coordinator":
                            for (Member b : guild.getMembers()) {
                                if (comapare(b.getEffectiveName(), a.getName())) {
                                    if (!Four.toString().contains(b.getAsMention()))
                                        Four.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) Four.append(a.getName());
                            Four.append(System.lineSeparator());
                            break;
                        case "Admin":
                            admincount++;
                            for (Member b : guild.getMembers()) {
                                if (comapare(b.getEffectiveName(), a.getName())) {
                                    if (!Five.toString().contains(b.getAsMention()) && !Six.toString().contains(b.getAsMention()))
                                        if (admincount <= admins)
                                            Five.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    if (admincount > admins)
                                        Six.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if (admincount <= admins)
                                if (!found) Five.append(a.getName());
                            Five.append(System.lineSeparator());
                            if (admincount > admins)
                                if (!found) Six.append(a.getName());
                            Six.append(System.lineSeparator());
                            break;
                    }
                }
                Ranks.addField("Owner", One.toString(), false);
                Ranks.addField("Deputy Owner", Two.toString(), true);
                Ranks.addField("Overseer", Three.toString(), true);
                Ranks.addField("Coordinator", Four.toString(), false);
                Ranks.addField("Admin", Five.toString(), true);
                if (admincount > admins) Ranks.addField("\u200B", Six.toString(), true);
                Ranks.setColor(new Color(148, 168, 249));
                Ranks.setDescription("The bot has matched the accounts with it's best guess of what their discord tag might be. There is still a significant margin for error, so let me know if something goes wrong, or something is omitted that should not be. However if you're running the command for a clan other than that which owns the discord server, things will be matched wrong.");
                String Time = new SimpleDateFormat("MM/dd/YYYY hh:mma zzz").format(new Date());
                Ranks.setFooter("Generated " + Time + " For " + Clan, Bot.jdas.get(shard).getSelfUser().getAvatarUrl());
                channel.sendMessage(Ranks.build()).queue(msg -> msg.delete().queueAfter(2, TimeUnit.MINUTES));
            } catch (java.lang.NoClassDefFoundError | IOException eeeee) {
                System.out.println(eeeee.getMessage());
                eeeee.printStackTrace();
            }

        }
    };
    public static BotCommand alog = new BotCommand("command.alog#all") {
        @Override
        void help() {
            this.helpname = "RuneScape Adventurer's Log";
            this.helpusage = "::alog <RunescapeName>";
            this.helpdesc = "Fetch the RuneScape adventurer's log for the specified player name.";
            this.skip = false;
        }

        @Override
        void command() {
            StringBuilder name = new StringBuilder();
            try {
                Scanner rsn = new Scanner(commandargs);
                if (rsn.hasNext()) {
                    name = new StringBuilder(rsn.next());
                    while (rsn.hasNext()) {
                        name.append("+").append(rsn.next());
                    }
                } else {
                    message.getChannel().sendMessage("<:Watch:326815513550389249> `" + user.getName() + ", you need to enter a name! ::alog NAME`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                    return;
                }
                URL url = new URL("http://services.runescape.com/m=adventurers-log/c=tB0ermS1flc/rssfeed?searchName=" + name);
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(url));
                StringBuilder test = new StringBuilder();
                List entries = feed.getEntries();
                Iterator it = entries.iterator();
                test.append("Adventurer's Log for ").append(name.toString().replace("+", " ")).append("\n```\n");
                while (it.hasNext()) {
                    SyndEntry entry = (SyndEntry) it.next();
                    SyndContent description = entry.getDescription();
                    test.append(description.getValue().trim().replace("my  ", "my ").replace("   called:  ", " called: ").replace("   in Daemonheim.", " in Daemonheim.").replace("  in Daemonheim", " in Daemonheim").replace(" , ", ", ")).append("\n");
                }
                test.append("```");
                channel.sendMessage(test.toString()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.MINUTES));
            } catch (IOException | FeedException e) {
                message.getChannel().sendMessage("<:Watch:326815513550389249> `" + user.getName() + ", the name you've entered is invalid! (" + name.toString().replace("+", " ") + ")`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                this.Lastrun = LocalDateTime.now().minusSeconds(getratelimit());
            }
        }
    };

    // Administer commands (won't work anywhere else)
    public static BotCommand m = new BotCommand("command.m") {
        @Override
        void command() {
            if (guild.getId().equals("319606739550863360")) {
                Role maintain[] = new Role[]{guild.getRoleById("319606870606217217")};
                if (guild.getMemberById(user.getId()).getRoles().contains(guild.getRoleById("319606870606217217"))) {
                    guild.getController().removeRolesFromMember(guild.getMemberById(user.getId()), Arrays.asList(maintain)).queue();
                } else {
                    guild.getController().addRolesToMember(guild.getMemberById(user.getId()), Arrays.asList(maintain)).queue();
                }
            }
        }
    };
    public static BotCommand v = new BotCommand("command.v") {
        @Override
        void command() {
            if (guild.getId().equals("319606739550863360")) {
                GuildController cont = guild.getController();
                Scanner watch = new Scanner(message.getRawContent());
                String mention = watch.next();
                for (Member m : guild.getMembers()) {
                    if (mention.equals(m.getAsMention())) {
                        if (m.equals(guild.getMember(user)) || m.isOwner() || m.getUser().isBot()) {
                            break;
                        }
                        if (m.getRoles().contains(guild.getRoleById("320300565789802497"))) {
                            cont.removeRolesFromMember(m, guild.getRoleById("320300565789802497")).queue();
                            cont.setNickname(m, "").queue();
                            channel.sendMessage("`" + m.getUser().getName() + " has been un-verified by " + guild.getMember(user).getEffectiveName() + ".`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                        } else {
                            StringBuilder name = new StringBuilder(m.getEffectiveName());
                            if (watch.hasNext()) {
                                name = new StringBuilder(watch.next());
                                while (watch.hasNext()) {
                                    name.append(" ").append(watch.next());
                                }
                            }
                            cont.setNickname(m, name.toString()).queue();
                            cont.addRolesToMember(m, guild.getRoleById("320300565789802497")).queue();
                            channel.sendMessage("`" + m.getEffectiveName() + " has been verified by " + guild.getMember(user).getEffectiveName() + " as " + name + ".`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                        }
                    }
                }
            }
        }
    };
}
