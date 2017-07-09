package net.swvn9.Watch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mikebull94.rsapi.RuneScapeAPI;
import com.mikebull94.rsapi.hiscores.ClanMate;
import com.mikebull94.rsapi.hiscores.Hiscores;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;
import io.sentry.Sentry;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.managers.GuildController;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
class BotCommand {
    protected final String commandNode;
    protected final HashSet<String> memory = new HashSet<>();
    private final Long rateLimit;
    private final File watchFile;
    public MessageChannel channel;
    protected int shard =0;
    protected Message message;
    protected Guild guild;
    protected User author;
    protected String arguments;
    protected BotUser botUser;
    protected boolean waiting = false;
    protected LocalDateTime lastRun = LocalDateTime.now().minusYears(10L);
    protected MessageChannel lastChannel;
    protected Guild lastGuild;
    protected boolean saveMemory = false;
    protected long start;

    protected String helpName = "Undefined";
    protected String helpUsage = "Undefined";
    protected String helpDesc = "Undefined";
    protected Boolean helpSkip = false;

    protected BotCommand(String commandNode) {
        this.commandNode = commandNode;
        this.rateLimit = 1L;
        this.watchFile = new File("Commands" + File.separator + (commandNode.replace("command.", "").replace("#all", "")) + ".watch");
        this.help();
        BotCommands.commandList.add(this);
        if (saveMemory) {
            try {
                if (!watchFile.exists()) {
                    FileWriter newFile = new FileWriter(watchFile, false);
                    newFile.close();
                } else {
                    FileReader openfile = new FileReader(watchFile);
                    Scanner readfile = new Scanner(openfile);
                    while (readfile.hasNext()) {
                        memory.add(readfile.next());
                    }
                    readfile.close();
                    openfile.close();
                }
            } catch (Exception ex) {
                Sentry.capture(ex);
            }
        }
    }

    protected long getratelimit() {
        return this.rateLimit;
    }

    void help() {
        this.helpName = "Undefined";
        this.helpUsage = "Undefined";
        this.helpDesc = "Undefined";
        this.helpSkip = true;
    }

    void saveMemory() {
        if (saveMemory) {
            try {
                StringBuilder memstring = new StringBuilder();
                for (String s : memory) {
                    memstring.append(s).append(" ");
                }
                FileWriter writefile = new FileWriter(watchFile, false);
                writefile.append(memstring);
                writefile.close();
            } catch (Exception ex) {
                Sentry.capture(ex);
            }
        }
    }

    void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    void run(Message m) {
        for(JDA jda:Bot.shards){
            if(jda.getGuilds().contains(m.getGuild())) shard = jda.getShardInfo().getShardId();
        }
        this.message = m;
        this.guild = m.getGuild();
        this.channel = m.getChannel();
        this.author = m.getAuthor();
        this.botUser = new BotUser(author, guild);
        //channel.sendTyping().queue();
        try{
            TimeUnit.MILLISECONDS.sleep(250);
        }catch(Exception ex){
            Sentry.capture(ex);
        }
        if (commandNode.contains("#all")) {
            this.arguments = message.getContent().replaceFirst("(?i)"+BotListeners.LITERAL + (commandNode.replace("command.", "")).replace("#all", ""), "");
        } else {
            this.arguments = message.getContent().replaceFirst("(?i)"+BotListeners.LITERAL + (commandNode.replace("command.", "")), "");
        }
        if (botUser.hasPermission(commandNode) || botUser.isadmin() || commandNode.contains("#all")) {
            if (LocalDateTime.now().isBefore(lastRun) && !botUser.isadmin()) {
                long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), lastRun);
                message.getChannel().sendMessage("<:Watch:326815513550389249> `You can run this command again in " + seconds + " seconds.` `" + message.getContent() + "`").queue(msg -> msg.delete().queueAfter((int) seconds, TimeUnit.SECONDS));
                this.cleanup(true);
                return;
            }
            this.lastRun = LocalDateTime.now().plusSeconds(rateLimit);
            try {
                this.command();
            } catch (Exception ex) {
                ex.printStackTrace();
                Sentry.capture(ex);
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
        this.author = null;
        this.botUser = null;
        this.arguments = null;
        saveMemory();
    }

    void command() throws Exception {
        message.getChannel().sendMessage("<:WatchWarn:326815513634406419> `This command has not been configured, commandNode: " + this.commandNode + "`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
    }

}

@SuppressWarnings("unused")
class BotCommands {
    public static final HashSet<BotCommand> commandList = new HashSet<>();
    public static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    // General commands
    public static BotCommand help = new BotCommand("command.help#all") {
        @Override
        void help() {
            this.helpName = "Help (This command)";
            this.helpUsage = BotListeners.LITERAL+"help <keyword> <-a,c>";
            this.helpDesc = "See all of the commands associated with the bot that you can use, sent to you in a dm unless specified otherwise.\n#Flags:\n<-a> All commands\n<-c> In current channel";
            this.helpSkip = false;
        }

        @Override
        void command() {
            EmbedBuilder showCommands = new EmbedBuilder();
            showCommands.setColor(botColour(Bot.shards.get(shard).getSelfUser().getAvatarUrl(),1,1));
            showCommands.setFooter("List of commands.", Bot.shards.get(shard).getSelfUser().getAvatarUrl());
            boolean specific = false;
            String noargs = arguments.replace("-a", "").replace("-c", "").trim();
            if (new Scanner(noargs).hasNext()) {
                String next = new Scanner(noargs).next();
                for (BotCommand bc : commandList) {
                    if ((bc.helpName.toLowerCase()).contains(next.toLowerCase())) {
                        specific = true;
                        showCommands.addField(bc.helpName, "```Markdown\n#Node: \n" + bc.commandNode.replace("#all", "") + "\n#Usage: \n" + bc.helpUsage + "\n#Description: \n" + bc.helpDesc + "```", true);
                    }
                }
            }
            for (BotCommand bc : commandList) {
                if (bc.helpSkip || specific) continue;
                if (botUser.hasPermission(bc.commandNode) || bc.commandNode.contains("#all") || arguments.contains("-a") || botUser.isadmin()) {
                    showCommands.addField(bc.helpName, "```Markdown\n#Node: \n" + bc.commandNode.replace("#all", "") + "\n#Usage: \n" + bc.helpUsage + "\n#Description: \n" + bc.helpDesc + "```", true);
                }
            }
            if (arguments.contains("-c") && botUser.isadmin()) {
                channel.sendMessage(showCommands.build()).queue();
            } else {
                author.openPrivateChannel().queue(c->c.sendMessage(showCommands.build()).queue());
                message.getChannel().sendMessage("<:Watch:326815513550389249> `" + author.getName() + ", check your DMs!`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand inv = new BotCommand("command.inv") {
        @Override
        void help() {
            this.helpName = "Invite";
            this.helpUsage = BotListeners.LITERAL+"inv";
            this.helpDesc = "Generate a one-time-use invite that is valid for 24 hours";
            this.helpSkip = false;
        }

        @Override
        void command() {
            String invcode;
            invcode = guild.getPublicChannel().createInvite().setMaxUses(1).setMaxAge(24L, TimeUnit.HOURS).setUnique(true).complete().getCode();
            channel.sendMessage("<:Watch:326815513550389249> `An invite has been created and sent to you `").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            author.openPrivateChannel().queue(c->c.sendMessage("Your invite is valid for **24 hours and one use**. the link is: http://discord.gg/" + invcode).queue());
        }
    };
    public static BotCommand info = new BotCommand("command.info#all") {
        @Override
        void command() {
            EmbedBuilder stuff = new EmbedBuilder();
            stuff.setTitle(author.getName() + "#" + author.getDiscriminator());
            stuff.setThumbnail(author.getAvatarUrl());
            stuff.setColor(botColour(Bot.shards.get(shard).getSelfUser().getAvatarUrl(),1,1));
            DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("dd MMM yyyy");
            stuff.addField("ID", "`" + author.getId() + "`", true);
            stuff.addField("Effective Name", "`" + guild.getMember(author).getEffectiveName() + "`", true);
            stuff.addField("Discord Join Date", "`" + author.getCreationTime().format(dateformat) + "`", true);
            stuff.addField("Status", "`" + guild.getMember(author).getOnlineStatus().name() + "`", false);
            StringBuilder userroles = new StringBuilder();
            for (Role e : guild.getMember(author).getRoles()) {
                userroles.append("- ").append(e.getAsMention()).append("\n");
            }
            if (guild.getMember(author).getRoles().isEmpty()) userroles.append("`(none)`");
            stuff.addField("User Roles", userroles.toString(), false);
            channel.sendMessage(stuff.build()).queue();

            EmbedBuilder rolecases = new EmbedBuilder();
            rolecases.setTitle("Role Interaction Cases");
            rolecases.setColor(new Color(255, 255, 255));
            if (!message.getMentionedRoles().isEmpty()) {
                for (Role r : message.getMentionedRoles()) {
                    rolecases.addField(r.getName(), r.getAsMention() + "\t" + guild.getMember(author).canInteract(r), false);
                }
                channel.sendMessage(rolecases.build()).queue();
            }

            EmbedBuilder usercases = new EmbedBuilder();
            usercases.setTitle("User Interaction Cases");
            usercases.setColor(new Color(255, 255, 255));
            if (!message.getMentionedUsers().isEmpty()) {
                for (User u : message.getMentionedUsers()) {
                    usercases.addField(guild.getMember(u).getEffectiveName(), u.getAsMention() + "\t" + guild.getMember(author).canInteract(guild.getMember(u)), false);
                }
                channel.sendMessage(usercases.build()).queue();
            }
        }
    };

    // Mod/Admin commands
    public static BotCommand say = new BotCommand("command.say") {
        @Override
        void help() {
            this.helpName = "Say";
            this.helpUsage = BotListeners.LITERAL+"say <Message>";
            this.helpDesc = "Send a Message as the bot";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (!arguments.equals("")) {
                channel.sendMessage(message.getRawContent().replaceFirst("(?i)"+BotListeners.LITERAL+"say", "")).queue();
            }
        }
    };
    public static BotCommand ban = new BotCommand("command.ban") {
        @Override
        void help() {
            this.helpName = "Ban";
            this.helpUsage = BotListeners.LITERAL+"ban <mention(s)> <reason>";
            this.helpDesc = "Ban user(s) with an optional Message";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (!arguments.equals("")) {
                TextChannel send = null;
                for (TextChannel c : guild.getTextChannels()) {
                    if (c.getName().equalsIgnoreCase("logs")) {
                        send = c;
                    }
                }
                for (User u : message.getMentionedUsers()) {
                    this.arguments = arguments.replace("@" + u.getName(), "").trim();
                    if (guild.getMember(author).canInteract(guild.getMember(u))) if (!u.isBot())
                        u.openPrivateChannel().queue(c->c.sendMessage("<:Watch:326815513550389249> You've been banned from " + guild.getName() + " by " + author.getAsMention() + " with the message `" + arguments + "`.").queue());
                }
                arguments = arguments.substring(0, Math.min(arguments.length(), 512));
                for (User u : message.getMentionedUsers()) {
                    if (guild.getMember(author).canInteract(guild.getMember(u))) {
                        if (send != null) {
                            EmbedBuilder log = new EmbedBuilder();
                            log.setColor(new Color(255, 0, 0));
                            log.addField("Action", "Ban", false);
                            log.addField("User", u.getName() + "#" + u.getDiscriminator() + " (" + u.getId() + ")", false);
                            log.addField("Moderator:", author.getName() + "#" + author.getDiscriminator(), false);
                            log.addField("Reason", arguments, false);
                            log.setFooter(Bot.shards.get(shard).getSelfUser().getName() + "#" + Bot.shards.get(shard).getSelfUser().getDiscriminator(), Bot.shards.get(1).getSelfUser().getAvatarUrl());
                            log.setTimestamp(LocalDateTime.now());
                            send.sendMessage(log.build()).queue();
                        }
                        if (!u.equals(author)) guild.getController().ban(u, 6, arguments).queue();
                        message.getChannel().sendMessage("<:Watch:326815513550389249> `" + author.getName() + " banned " + u.getName() + "#" + u.getDiscriminator() + " (" + arguments + ")`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    } else {
                        message.getChannel().sendMessage("<:WatchError:326815514129072131> `Unable to ban " + u.getName() + "#" + u.getDiscriminator() + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    }
                }
                if (send != null)
                    message.getChannel().sendMessage("<:Watch:326815513550389249> `Bans have been logged in the `" + send.getAsMention() + "` channel.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            } else {
                message.getChannel().sendMessage("<:Watch:326815513550389249> `" + author.getName() + ", you need to mention at least one user "+BotListeners.LITERAL+"ban @mention(s)`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand kick = new BotCommand("command.kick") {
        @Override
        void help() {
            this.helpName = "Kick";
            this.helpUsage = BotListeners.LITERAL+"kick <mention(s)> <reason>";
            this.helpDesc = "Kick user(s) with an optional Message";
            this.helpSkip = false;
        }

        @Override
        void command() {
            TextChannel send = null;
            for (TextChannel c : guild.getTextChannels()) {
                if (c.getName().equalsIgnoreCase("logs")) {
                    send = c;
                }
            }
            if (!arguments.equals("")) {
                for (User u : message.getMentionedUsers()) {
                    this.arguments = arguments.replace("@" + u.getName(), "").trim();
                    if (guild.getMember(author).canInteract(guild.getMember(u))) if (!u.isBot())
                        u.openPrivateChannel().queue(c->c.sendMessage("<:Watch:326815513550389249> You've been kicked from " + guild.getName() + " by " + author.getAsMention() + " with the message `" + arguments + "`.").queue());
                }
                arguments = arguments.substring(0, Math.min(arguments.length(), 512));
                for (User u : message.getMentionedUsers()) {
                    if (guild.getMember(author).canInteract(guild.getMember(u))) {
                        if (send != null) {
                            EmbedBuilder log = new EmbedBuilder();
                            log.setColor(new Color(0, 0, 255));
                            log.addField("Action", "Kick", false);
                            log.addField("User", u.getName() + "#" + u.getDiscriminator() + " (" + u.getId() + ")", false);
                            log.addField("Moderator:", author.getName() + "#" + author.getDiscriminator(), false);
                            log.addField("Reason", arguments, false);
                            log.setFooter(Bot.shards.get(shard).getSelfUser().getName() + "#" + Bot.shards.get(shard).getSelfUser().getDiscriminator(), Bot.shards.get(1).getSelfUser().getAvatarUrl());
                            log.setTimestamp(LocalDateTime.now());
                            send.sendMessage(log.build()).queue();
                        }
                        if (!u.equals(author)) guild.getController().kick(u.getId(), arguments).queue();
                        message.getChannel().sendMessage("<:Watch:326815513550389249> `" + author.getName() + " kicked " + u.getName() + "#" + u.getDiscriminator() + " (" + arguments + ")`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    } else {
                        message.getChannel().sendMessage("<:WatchError:326815514129072131> `Unable to kick " + u.getName() + "#" + u.getDiscriminator() + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    }
                }

            } else {
                message.getChannel().sendMessage("<:Watch:326815513550389249> `" + author.getName() + ", you need to mention at least one user "+BotListeners.LITERAL+"kick @mention(s)`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand watch = new BotCommand("command.watch") {
        @Override
        void help() {
            this.helpName = "Watch";
            this.helpUsage = BotListeners.LITERAL+"watch <list/del/add> <keyword>";
            this.helpDesc = "Have the bot \"watch\" for certain keywords in chat, and log any occurrences to a channel called #logs\nKeywords are not case-sensitive";
            this.helpSkip = false;
            this.saveMemory = true;
        }

        @Override
        void command() {
            if (!arguments.equals("")) {
                Scanner read = new Scanner(arguments);
                if (read.hasNext()) {
                    switch (read.next()) {
                        default:
                            message.getChannel().sendMessage("<:Watch:326815513550389249> `Invalid Syntax` `" + arguments + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            break;
                        case "add":
                            if (read.hasNext()) {
                                String keyword = read.next().toLowerCase();
                                if (!memory.contains(keyword)) {
                                    memory.add(keyword);
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `" + keyword + " has been added to the watch filter. type "+BotListeners.LITERAL+"watch del " + keyword + " to remove it.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                } else {
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `I am already watching for " + keyword + ".`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                }
                            } else {
                                message.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify a keyword!` `" + helpUsage + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            }
                            break;
                        case "del":
                            if (read.hasNext()) {
                                String keyword = read.next().toLowerCase();
                                if (memory.contains(keyword)) {
                                    memory.remove(keyword);
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `" + keyword + " has been removed from the watch filter.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                } else {
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `I am not currently watching for " + keyword + ". Do "+BotListeners.LITERAL+"watch add " + keyword + " to add it to the list.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                }
                            } else {
                                message.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify a keyword!` `" + helpUsage + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
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
                message.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify an action and a keyword!` `" + helpUsage + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand purge = new BotCommand("command.purge") {
        @Override
        void help() {
            this.helpName = "Purge Messages";
            this.helpUsage = BotListeners.LITERAL+"purge <mentions> <number>";
            this.helpDesc = "Mentions are optional, if a number of messages to purge is not specified, it will be 10. Pinned messages will not be deleted.";
            this.helpSkip = false;
        }

        @Override
        void command() throws Exception {
            this.arguments = message.getRawContent().replaceFirst("(?i)"+BotListeners.LITERAL+"purge", "");
            this.arguments = arguments.replaceAll("<(?:@(?:[!&])?|#|:\\w{2,}:)\\d{17,}>", "").trim();
            List<String> remove = new ArrayList<>();
            Scanner args = new Scanner(arguments);
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

                }catch(Exception ex){
                    if(total==1&&msg!=null) msg.editMessage("<:Watch:326815513550389249> `Purged " + total + " message.`").queue(m->m.delete().queueAfter(30,TimeUnit.SECONDS));
                    if(total!=1&&msg!=null) msg.editMessage("<:Watch:326815513550389249> `Purged " + total + " messages.`").queue(m->m.delete().queueAfter(30,TimeUnit.SECONDS));
                    Sentry.capture(ex);
                    break;
                }
            }
            if(total==1&&msg!=null) msg.editMessage("<:Watch:326815513550389249> `Purged " + total + " message.`").queue(m->m.delete().queueAfter(30,TimeUnit.SECONDS));
            if(total!=1&&msg!=null) msg.editMessage("<:Watch:326815513550389249> `Purged " + total + " messages.`").queue(m->m.delete().queueAfter(30,TimeUnit.SECONDS));
        }
    };

    // Configuration commands
    public static BotCommand id = new BotCommand("command.id") {
        @Override
        void help() {
            this.helpName = "ID";
            this.helpUsage = BotListeners.LITERAL+"id";
            this.helpDesc = "Grab the ID and any permissions associated with your user ID.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            channel.sendMessage(author.getAsMention() + ", Your ID is " + author + ".").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            channel.sendMessage(botUser.getPermissions().toString()).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            channel.sendMessage(botUser.isadmin() + "").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    };
    public static BotCommand roles = new BotCommand("command.roles") {
        @Override
        void help() {
            this.helpName = "Roles";
            this.helpUsage = BotListeners.LITERAL+"roles";
            this.helpDesc = "Get all of the role-names and IDs associated with the current discord guild.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            EmbedBuilder roles = new EmbedBuilder();
            roles.setFooter("Roles for " + guild.getName(), Bot.shards.get(shard).getSelfUser().getAvatarUrl());
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
            roles.setColor(botColour(Bot.shards.get(shard).getSelfUser().getAvatarUrl(),1,1));
            channel.sendMessage(roles.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
        }
    };
    public static BotCommand config = new BotCommand("command.config") {
        @Override
        void help() {
            this.helpName = "Show Config";
            this.helpUsage = BotListeners.LITERAL+"showconfig";
            this.helpDesc = "Spit out the contents of the Config.yml file to a rich embed.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            EmbedBuilder botGroups = new EmbedBuilder();
            EmbedBuilder botUsers = new EmbedBuilder();

            botGroups.setColor(botColour(Bot.shards.get(shard).getSelfUser().getAvatarUrl(),1,1));
            botUsers.setColor(botColour(Bot.shards.get(shard).getSelfUser().getAvatarUrl(),1,1));

            if(BotConfig.config.getGroups()!=null)for (String key : BotConfig.config.getGroups().keySet()) {
                StringBuilder ids = new StringBuilder();
                ids.append("Group IDs").append(System.lineSeparator());
                for (String zz : BotConfig.config.getGroups().get(key).groupId)
                    ids.append("- `").append(zz).append("`").append(System.lineSeparator());
                botGroups.addField(key, ids.toString(), true);
                StringBuilder perms = new StringBuilder();
                perms.append("Permissions").append(System.lineSeparator());
                if (BotConfig.config.getGroups().get(key).permissions != null) {
                    for (String zz : BotConfig.config.getGroups().get(key).permissions)
                        perms.append("- `").append(zz).append("`").append(System.lineSeparator());
                } else {
                    perms.append("`none`");
                }
                if (BotConfig.config.getGroups().get(key).admin) {
                    botGroups.addField("Type: SuperUser Group", perms.toString(), true);
                } else {
                    botGroups.addField("Type: User Group", perms.toString(), true);
                }
                botGroups.addBlankField(true);
            }

            if(BotConfig.config.getUsers()!=null)for (String key : BotConfig.config.getUsers().keySet()) {
                botUsers.addField(key, "User ID" + System.lineSeparator() + "`" + BotConfig.config.getUsers().get(key).userId + "`", true);
                StringBuilder perms = new StringBuilder();
                perms.append("Permissions").append(System.lineSeparator());
                HashSet<String> thing = new HashSet<>();
                if (BotConfig.config.getUsers().get(key).permissions != null) {
                    thing.addAll(BotConfig.config.getUsers().get(key).permissions);
                } else {
                    perms.append("`none`");
                }
                for (String zz : thing)
                    perms.append("- `").append(zz).append("`").append(System.lineSeparator());
                if (BotConfig.config.getUsers().get(key).admin) {
                    botUsers.addField("Type: SuperUser", perms.toString(), true);
                } else {
                    botUsers.addField("Type: User", perms.toString(), true);
                }
                botUsers.addBlankField(true);
            }

            botGroups.setFooter("Groups ", Bot.shards.get(shard).getSelfUser().getAvatarUrl());
            botUsers.setFooter("Users ", Bot.shards.get(shard).getSelfUser().getAvatarUrl());
            botGroups.setTimestamp(LocalDateTime.now());
            botUsers.setTimestamp(LocalDateTime.now());

            if(BotConfig.config.getGroups()!=null) channel.sendMessage(botGroups.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
            if(BotConfig.config.getUsers()!=null) channel.sendMessage(botUsers.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
        }
    };
    public static BotCommand pullconfig = new BotCommand("command.pullconfig") {
        @Override
        void help() {
            this.helpName = "Pull Config";
            this.helpUsage = BotListeners.LITERAL+"pullconfig";
            this.helpDesc = "Pull the latest configuration from the Config.yml file.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            BotConfig.loadConfig();
            BotListeners.WHITELIST = BotConfig.getWhitelist();
            //channel.addReactionById(message.getId(), "👍").queue();
        }
    };

    // Owner commands
    public static BotCommand shard = new BotCommand("command.shard"){
        @Override
        void command() throws Exception {
            EmbedBuilder sharding = new EmbedBuilder();
            sharding.setColor(botColour(Bot.shards.get(shard).getSelfUser().getAvatarUrl(),1,1));
            sharding.setTimestamp(LocalDateTime.now());
            sharding.setFooter("Watch #6969 ",Bot.shards.get(shard).getSelfUser().getAvatarUrl());
            if(arguments.contains("-l")){
                for(int i = 0; i<Bot.shards.size(); i++){
                    sharding.addField("Shard "+(Bot.shards.get(i).getShardInfo().getShardId()+1)+" of "+Bot.shards.get(i).getShardInfo().getShardTotal(),"```java\n "+Bot.shards.get(i).getGuilds().toString().replace("),",")\n").replace("]","").replaceAll(" \\n", "\n").replaceAll("([\\[\\]])","")+"```",false);
                }
            }
            channel.sendMessage(sharding.build()).queue();
        }
    };
    public static BotCommand kill = new BotCommand("command.kill") {
        @Override
        void help() {
            this.helpName = "Kill";
            this.helpUsage = BotListeners.LITERAL+"kill";
            this.helpDesc = "Kill the bot and return the host machine to the command line/desktop.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (channel.getType().equals(ChannelType.TEXT)) message.delete().queue();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception ex) {
                Sentry.capture(ex);
            }
            Runtime.getRuntime().exit(0);
        }
    };
    public static BotCommand stop = new BotCommand("command.stop") {
        @Override
        void help() {
            this.helpName = "Stop";
            this.helpUsage = BotListeners.LITERAL+"stop";
            this.helpDesc = "Stop the shard that the guild is running on. (There is no way to restart)";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (channel.getType().equals(ChannelType.TEXT)) message.delete().queue();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception ex) {
                Sentry.capture(ex);
            }
            Bot.shards.get(shard).removeEventListener(Bot.shards.get(shard).getRegisteredListeners());
            Bot.shards.get(shard).shutdown(true);
        }
        @Override
        void cleanup(boolean delete) {}
    };
    /*
    public static BotCommand restart = new BotCommand("command.restart") {
        @Override
        void cleanup(boolean delete) {
        }

        @Override
        void help() {
            this.helpName = "Restart";
            this.helpUsage = BotListeners.LITERAL+"restart";
            this.helpDesc = "Restart the bot, re-initialise everything.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            Bot.restart(shard);
        }
    };
    */
    public static BotCommand input = new BotCommand("command.input") {
        @Override
        void help() {
            this.helpName = "Enable/Disable Input";
            this.helpUsage = BotListeners.LITERAL+"input";
            this.helpDesc = "Enable or disable input for the bot so that the bot can stay running while testing.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (!waiting) {
                this.waiting = true;
                for(JDA jda:Bot.shards){
                    jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                }
            } else {
                this.waiting = false;
                if(BotListeners.isDevelopmentEnvironment()){
                    for(JDA jda:Bot.shards){
                        jda.getPresence().setStatus(OnlineStatus.IDLE);
                    }
                } else {
                    for(JDA jda:Bot.shards){
                        jda.getPresence().setStatus(OnlineStatus.ONLINE);
                    }
                }
            }
        }
    };
    public static BotCommand bot = new BotCommand("command.bot") {
        @Override
        void help() {
            this.helpName = "Bot Utility";
            this.helpUsage = BotListeners.LITERAL+"bot <-s/-a/-k> <?listener>";
            this.helpDesc = "You should probably stay away from everything this command does except for "+BotListeners.LITERAL+"bot -s";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (arguments.contains("-s")) {
                EmbedBuilder stats = new EmbedBuilder();
                stats.setTimestamp(LocalDateTime.now());
                stats.setColor(botColour(Bot.shards.get(shard).getSelfUser().getAvatarUrl(),1,1));
                stats.addField("Status", Bot.shards.get(shard).getStatus().name(), false);
                stats.addField("Heartbeat", "" + Bot.shards.get(shard).getPing() + "ms", true);
                stats.addField("API Responses", "" +Bot.shards.get(shard).getResponseTotal() + "", true);

                stats.setFooter("Watch#6969 ", Bot.shards.get(shard).getSelfUser().getAvatarUrl());

                long uptime = System.nanoTime() - start;
                long days = TimeUnit.NANOSECONDS.toDays(uptime);
                long hours = TimeUnit.NANOSECONDS.toHours(uptime - TimeUnit.DAYS.toNanos(days));
                long minutes = TimeUnit.NANOSECONDS.toMinutes(uptime - TimeUnit.HOURS.toNanos(hours)-TimeUnit.DAYS.toNanos(days));
                long seconds = TimeUnit.NANOSECONDS.toSeconds(uptime - TimeUnit.MINUTES.toNanos(minutes)-TimeUnit.HOURS.toNanos(hours)-TimeUnit.DAYS.toNanos(days));

                StringBuilder uptimeString = new StringBuilder();

                if (days != 0) uptimeString.append(days).append("d ");
                if (hours != 0) uptimeString.append(hours).append("h ");
                if (minutes != 0) uptimeString.append(minutes).append("m ");
                if (seconds != 0) uptimeString.append(seconds).append("s");

                stats.addField("Uptime", "" + uptimeString.toString() + "", true);
                StringBuilder cfr = new StringBuilder();
                int number = 1;
                for (String s : Bot.shards.get(shard).getCloudflareRays()) {
                    cfr.append("<").append(number++).append("/").append(s).append(">\n");
                }
                stats.addField("Shard Info",Bot.shards.get(shard).getShardInfo().toString(),true);
                stats.addField("CF Rays", "```Markdown\n" + cfr.toString() + "```", false);
                StringBuilder lst = new StringBuilder();
                int number2 = 0;
                for (Object obj : Bot.shards.get(shard).getRegisteredListeners()) {
                    lst.append("<").append(number2++).append("/").append(obj.toString()).append(">\n");
                }
                stats.addField("Listeners", "```Markdown\n" + lst.toString() + "```", false);
                channel.sendMessage(stats.build()).queue();
            } else if (arguments.contains("-k")) {
                arguments = arguments.replace("-k", "");
                Scanner ln = new Scanner(arguments);
                if (ln.hasNextInt()) {
                    int chosen = ln.nextInt();
                    channel.sendMessage("```Markdown\nYou have removed listener <" + chosen + "/" + Bot.shards.get(shard).getRegisteredListeners().get(chosen).toString() + ">```").queue();
                    Bot.shards.get(shard).removeEventListener(Bot.shards.get(shard).getRegisteredListeners().get(chosen));
                }
            } else if (arguments.contains("-a")) {
                arguments = arguments.replace("-a", "");
                Scanner ln = new Scanner(arguments);
                if (ln.hasNext()) {
                    String chosenL = ln.next();
                    switch (chosenL.toLowerCase()) {
                        case "botlisteners":
                            channel.sendMessage("```Markdown\nYou have added a listener <BotListeners>```").queue();
                            Bot.shards.get(shard).addEventListener(new BotListeners());
                            break;
                        case "botgeneric":
                            channel.sendMessage("```Markdown\nYou have added a listener <BotGeneric>```").queue();
                            Bot.shards.get(shard).addEventListener(new BotGeneric());
                            break;
                        case "botlogging":
                            channel.sendMessage("```Markdown\nYou have added a listener <BotLogging>```").queue();
                            Bot.shards.get(shard).addEventListener(new BotLogging());
                            break;
                    }
                }
            } else if (arguments.contains("-b")){
                try{
                    File ciBadge = new File("badges/ci.png");
                    URL url=new URL("https://img.shields.io/circleci/project/github/swvn9/Watch.png");
                    URLConnection conn = url.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
                    conn.connect();
                    FileUtils.copyInputStreamToFile(conn.getInputStream(), ciBadge);
                    BufferedImage image = ImageIO.read(ciBadge);
                    int c = image.getRGB(51,2);
                    int  red = (c & 0x00ff0000) >> 16;
                    int  green = (c & 0x0000ff00) >> 8;
                    int  blue = c & 0x000000ff;
                    Color color1 = new Color(red,green,blue);
                    EmbedBuilder build = new EmbedBuilder();
                    if(color1.getRGB()==-2464434){
                        build.addField("Latest CI build:","**FAILING**\n[circleci.com](https://circleci.com/gh/swvn9/Watch/tree/master)",false);
                    } else {
                        build.addField("Latest CI build:","**PASSING**\n[circleci.com](https://circleci.com/gh/swvn9/Watch/tree/master)",false);
                    }
                    build.setColor(color1);
                    channel.sendMessage(build.build()).queue();
                }catch(Exception ex){
                    Sentry.capture(ex);
                }

            }
        }
    };
    public static BotCommand eval = new BotCommand("command.eval") {
        @Override
        void help() {
            this.helpName = "Javascript Eval";
            this.helpUsage = BotListeners.LITERAL+"eval <line>";
            this.helpDesc = "Evaluate a line of JS";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if(botUser.isadmin()){
                try {
                    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                    engine.put("jda",Bot.shards.get(shard));
                    engine.put("channel", channel);
                    engine.put("message", message);
                    engine.put("guild", guild);
                    engine.put("user", author);
                    channel.sendMessage("```java\n//Evaluating\n" + arguments.trim() + "```").queue();
                    String res = engine.eval(arguments).toString();
                    if(res!=null)  channel.sendMessage("```js\n//Response\n" + res + "```").queue();
                } catch (Exception ex) {
                    Sentry.capture(ex);
                    if(ex.getClass()!=NullPointerException.class) channel.sendMessage("```js\n//Exception\n" + ex + "```").queue();
                }
            }
        }
    };
    public static BotCommand link = new BotCommand("command.link") {
        @Override
        void help() {
            this.helpName = "Rebrandly Link";
            this.helpUsage = BotListeners.LITERAL+"link <url> <redirect>";
            this.helpDesc = "Using the rebrandly api token defined in the config file, create redirect links.";
            this.helpSkip = false;
        }

        @Override
        void command() throws Exception {
            boolean info = false;
            if (arguments.contains("-j")) {
                info = true;
                arguments = arguments.replace("-j", "").trim();
            }
            Scanner args = new Scanner(arguments);
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
            connection.setRequestProperty("apikey", BotConfig.getrebrandlyToken());
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write("{\"destination\": \"" + ouath + "\", \"slashtag\":\"" + shortl + "\", \"domain\": { \"fullName\": \"" + BotConfig.getrebrandlyURL() + "\" }}");
            out.flush();
            out.close();
            int res = connection.getResponseCode();
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (res == 200) {
                    if (info)
                        channel.sendMessage("<:Watch:326815513550389249> **http://" + BotConfig.getrebrandlyURL() + "/" + shortl + "** now links to **" + ouath + "**.\n```json\n" + line + "```").queue();
                    if (!info)
                        channel.sendMessage("<:Watch:326815513550389249> **http://" + BotConfig.getrebrandlyURL() + "/" + shortl + "** now links to **" + ouath + "**.").queue();
                } else {
                    channel.sendMessage("<:WatchError:326815514129072131> " + res + " Something went wrong\n```JSON" + line + "```").queue();
                }
            }
            connection.disconnect();
        }
    };

    //Music Bot commands
    /*
    public static BotCommand summon = new BotCommand("command.summon") {
        @Override
        void help() {
            this.helpName = "Music: Summon DEPRICATED";
            this.helpUsage = BotListeners.LITERAL+"summon";
            this.helpDesc = "Summon the bot to your channel.";
            this.helpSkip = false;
        }

        @Override
        void command() throws Exception {
            if(guild.getAudioManager().getConnectedChannel()==null){
                if(guild.getAudioManager().getSendingHandler()==null){
                    guild.getAudioManager().openAudioConnection(guild.getMember(author).getVoiceState().getChannel());
                }
                guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(BotAudio.player));
            }
        }
    };
    */
    public static BotCommand play = new BotCommand("command.play#all") {
        @Override
        void help() {
            this.helpName = "Music: Play";
            this.helpUsage = BotListeners.LITERAL+"play <youtube/playlist-link/watch-link/soundcloud>";
            this.helpDesc = "If the queue is empty, play the linked song. If not empty, add to the queue.";
            this.helpSkip = false;
        }

        @Override
        void command() throws Exception {
            VoiceChannel uservc = guild.getMember(author).getVoiceState().getChannel();
            VoiceChannel botvc = guild.getMember(Bot.shards.get(shard).getSelfUser()).getVoiceState().getChannel();
            if(guild.getId().equals("254861442799370240")&&(!uservc.getId().equals("254864458927702018")||!channel.getId().equals("303651971829727233"))){
                channel.sendMessage("<:WatchMusic:331969464121950209> You must be in the "+guild.getVoiceChannelById("254864458927702018").getName()+" voice channel, and use the commands in "+guild.getTextChannelById("303651971829727233").getAsMention()+" to use the music bot.").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                return;
            }

            if(uservc==null){
                channel.sendMessage("<:WatchMusic:331969464121950209> You must be in a voice channel to queue songs!").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }

            if(uservc!=botvc&&BotAudio.player!=null){
                if(BotAudio.player.getPlayingTrack()!=null){
                    channel.sendMessage("<:WatchMusic:331969464121950209> The music bot is in use in a different channel, Wait a while and try again.").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    return;
                }
            }

            if(guild.getAudioManager().getConnectedChannel()==null){
                if(guild.getAudioManager().getSendingHandler()==null){
                    guild.getAudioManager().openAudioConnection(guild.getMember(author).getVoiceState().getChannel());
                }
                guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(BotAudio.player));
                this.lastGuild = guild;
            }

            play.lastChannel = channel;
            this.arguments = arguments.trim();
            if(!message.getContent().contains("youtube.com")){
                this.arguments = "ytsearch:"+arguments;
                this.waiting=true;
            }
            BotAudio.playerManager.loadItem(arguments, new AudioLoadResultHandler() {
                MessageChannel channel = BotCommands.play.lastChannel;
                @Override
                public void trackLoaded(AudioTrack audioTrack) {
                    if(BotAudio.player.getPlayingTrack()==null){
                        channel.sendMessage("<:WatchMusic:331969464121950209> Now Playing **"+audioTrack.getInfo().title+"**").queue(msg->BotCommands.play.memory.add(msg.getId()));
                    } else {
                        channel.sendMessage("<:WatchMusic:331969464121950209> Added **"+audioTrack.getInfo().title+"** to Queue").queue(msg->msg.delete().queueAfter(30,TimeUnit.SECONDS));
                    }
                    BotAudio.trackScheduler.queue(audioTrack);
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {
                    if(BotAudio.player.getPlayingTrack()==null){
                        channel.sendMessage("<:WatchMusic:331969464121950209> Now Playing **"+audioPlaylist.getTracks().get(0).getInfo().title+"**").queue(msg->BotCommands.play.memory.add(msg.getId()));
                    } else if(BotCommands.play.waiting) {
                        channel.sendMessage("<:WatchMusic:331969464121950209> Added **"+audioPlaylist.getTracks().get(0).getInfo().title+"** to Queue").queue(msg->msg.delete().queueAfter(30,TimeUnit.SECONDS));
                    }
                    for(AudioTrack at:audioPlaylist.getTracks()){
                        BotAudio.trackScheduler.queue(at);
                        if(BotCommands.play.waiting){
                            break;
                        }
                        //System.out.println(at.getInfo().toString());
                    }
                    if(!BotCommands.play.waiting)BotCommands.play.lastChannel.sendMessage("<:WatchMusic:331969464121950209> Added **"+audioPlaylist.getTracks().size()+"** songs to the queue.").queue(msg->msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    BotCommands.play.waiting=false;
                }

                @Override
                public void noMatches() {
                    System.out.println("No Matches");
                }

                @Override
                public void loadFailed(FriendlyException e) {
                    System.out.println(e.getMessage());
                }
            });
        }
    };
    public static BotCommand queue = new BotCommand("command.queue#all"){
        @Override
        void help() {
            this.helpName = "Music: Queue";
            this.helpUsage = BotListeners.LITERAL+"queue";
            this.helpDesc = "Display the current music queue.";
            this.helpSkip = false;
        }

        @Override
        void command() throws Exception {
            StringBuilder queue = new StringBuilder();
            if(BotAudio.trackScheduler.queue.isEmpty()){
                queue.append("```The Queue is Empty```");
            } else {
                queue.append("```Markdown\n");
                int i = 1;
                for(AudioTrack at:BotAudio.trackScheduler.queue){
                    if(i<10){ queue.append(i++).append(".  ").append(at.getInfo().title).append("\n\tLink: ").append(at.getInfo().uri).append("\n\n");}
                    else { queue.append(i++).append(". ").append(at.getInfo().title).append("\n\tLink: ").append(at.getInfo().uri).append("\n\n");}

                }
                queue.append("```");
            }
            channel.sendMessage("<:WatchMusic:331969464121950209> **Music Bot Queue**"+queue.toString()).queue(msg->msg.delete().queueAfter(2,TimeUnit.MINUTES));
        }
    };
    public static BotCommand skip = new BotCommand("command.skip"){
        @Override
        void help() {
            this.helpName = "Music: Skip";
            this.helpUsage = BotListeners.LITERAL+"skip <pos>";
            this.helpDesc = "Skip the current song. If the queue is empty, the bot will stop playing. If you specify a position, the song will be removed from the queue.";
            this.helpSkip = false;
        }

        @Override
        void command() throws Exception {
            if(new Scanner(arguments.trim()).hasNextInt()){
                int toSkip = new Scanner(arguments.trim()).nextInt()-1;
                System.out.println(toSkip);
                if(toSkip>=0&&toSkip<BotAudio.trackScheduler.queue.size()){
                    for(AudioTrack a:BotAudio.trackScheduler.queue){
                        if(BotAudio.trackScheduler.queue.toArray()[toSkip].equals(a)){
                            channel.sendMessage("<:WatchMusic:331969464121950209> Removed **"+a.getInfo().title+"** from position "+(toSkip+1)+" of the queue.").queue(msg->msg.delete().queueAfter(30,TimeUnit.SECONDS));
                            BotAudio.trackScheduler.queue.remove(a);
                            break;
                        }
                    }
                }
            } else {
                if(!BotCommands.play.memory.isEmpty()){
                    BotCommands.play.lastChannel.deleteMessageById(BotCommands.play.memory.toArray()[0]+"").queue();
                    BotCommands.play.memory.clear();
                }
                BotAudio.trackScheduler.nextTrack();
            }
        }
    };
    public static BotCommand clear = new BotCommand("command.clear"){
        @Override
        void command() throws Exception {
            BotAudio.trackScheduler.queue.clear();
            channel.sendMessage("<:WatchMusic:331969464121950209> The queue has been cleared!").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    };

    // RuneScape commands
    public static BotCommand clan = new BotCommand("command.clan") {
        @Override
        void help() {
            this.helpName = "RuneScape Clan Ranks";
            this.helpUsage = BotListeners.LITERAL+"clan";
            this.helpDesc = "(for now) Pull the upper ranks of the RuneScape clan Zamorak Cult, and match any names with those on the current discord guild.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            RuneScapeAPI api = RuneScapeAPI.createHttp();
            Hiscores hiscores = api.hiscores();

            String clan = "Zamorak Cult";
            try {
                List<ClanMate> clanMates = hiscores.clanInformation(clan);
                EmbedBuilder ranks = new EmbedBuilder();
                StringBuilder one = new StringBuilder();
                if (guild.getId().equals("254861442799370240") && clan.equals("Zamorak Cult"))
                    one.append(guild.getRoleById(254883837757227008L).getAsMention()).append(System.lineSeparator());
                StringBuilder two = new StringBuilder();
                if (guild.getId().equals("254861442799370240") && clan.equals("Zamorak Cult"))
                    two.append(guild.getRoleById(268490396617801729L).getAsMention()).append(System.lineSeparator());
                StringBuilder three = new StringBuilder();
                if (guild.getId().equals("254861442799370240") && clan.equals("Zamorak Cult"))
                    three.append(guild.getRoleById(258350529229357057L).getAsMention()).append(System.lineSeparator());
                StringBuilder four = new StringBuilder();
                if (guild.getId().equals("254861442799370240") && clan.equals("Zamorak Cult"))
                    four.append(guild.getRoleById(254881136524656640L).getAsMention()).append(System.lineSeparator());
                StringBuilder five = new StringBuilder();
                StringBuilder six = new StringBuilder();
                int admins = StringUtils.countMatches(clanMates.toString(), "Admin") / 2 + 1;
                int admincount = 0;
                for (ClanMate a : clanMates) {
                    boolean found = false;
                    switch (a.getRank()) {
                        case "Owner":
                            for (Member b : guild.getMembers()) {
                                if (comapare(b.getEffectiveName(), a.getName())) {
                                    if (!one.toString().contains(b.getAsMention()))
                                        one.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) one.append(a.getName());
                            one.append(System.lineSeparator());
                            break;
                        case "Deputy Owner":
                            for (Member b : guild.getMembers()) {
                                if (comapare(b.getEffectiveName(), a.getName())) {
                                    if (!two.toString().contains(b.getAsMention()))
                                        two.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) two.append(a.getName());
                            two.append(System.lineSeparator());
                            break;
                        case "Overseer":
                            for (Member b : guild.getMembers()) {
                                if (comapare(b.getEffectiveName(), a.getName())) {
                                    if (!three.toString().contains(b.getAsMention()))
                                        three.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) three.append(a.getName());
                            three.append(System.lineSeparator());
                            break;
                        case "Coordinator":
                            for (Member b : guild.getMembers()) {
                                if (comapare(b.getEffectiveName(), a.getName())) {
                                    if (!four.toString().contains(b.getAsMention()))
                                        four.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) four.append(a.getName());
                            four.append(System.lineSeparator());
                            break;
                        case "Admin":
                            admincount++;
                            for (Member b : guild.getMembers()) {
                                if (comapare(b.getEffectiveName(), a.getName())) {
                                    if (!five.toString().contains(b.getAsMention()) && !six.toString().contains(b.getAsMention()))
                                        if (admincount <= admins)
                                            five.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    if (admincount > admins)
                                        six.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if (admincount <= admins)
                                if (!found) five.append(a.getName());
                            five.append(System.lineSeparator());
                            if (admincount > admins)
                                if (!found) six.append(a.getName());
                            six.append(System.lineSeparator());
                            break;
                    }
                }
                ranks.addField("Owner", one.toString(), false);
                ranks.addField("Deputy Owner", two.toString(), true);
                ranks.addField("Overseer", three.toString(), true);
                ranks.addField("Coordinator", four.toString(), false);
                ranks.addField("Admin", five.toString(), true);
                if (admincount > admins) ranks.addField("\u200B", six.toString(), true);
                ranks.setColor(botColour(Bot.shards.get(shard).getSelfUser().getAvatarUrl(),1,1));
                ranks.setDescription("The bot has matched the accounts with it's best guess of what their discord tag might be. There is still a significant margin for error, so let me know if something goes wrong, or something is omitted that should not be. However if you're running the command for a clan other than that which owns the discord server, things will be matched wrong.");
                String time = new SimpleDateFormat("MM/dd/YYYY hh:mma zzz").format(new Date());
                ranks.setFooter("Generated " + time + " For " + clan, Bot.shards.get(shard).getSelfUser().getAvatarUrl());
                channel.sendMessage(ranks.build()).queue(msg -> msg.delete().queueAfter(2, TimeUnit.MINUTES));
            } catch (Exception eeeee) {
                Sentry.capture(eeeee);
            }

        }
    };
    public static BotCommand alog = new BotCommand("command.alog#all") {
        @Override
        void help() {
            this.helpName = "RuneScape Adventurer's Log";
            this.helpUsage = BotListeners.LITERAL+"alog <RunescapeName>";
            this.helpDesc = "Fetch the RuneScape adventurer's log for the specified player name.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            StringBuilder name = new StringBuilder();
            try {
                Scanner rsn = new Scanner(arguments);
                if (rsn.hasNext()) {
                    name = new StringBuilder(rsn.next());
                    while (rsn.hasNext()) {
                        name.append("+").append(rsn.next());
                    }
                } else {
                    message.getChannel().sendMessage("<:Watch:326815513550389249> `" + author.getName() + ", you need to enter a name! "+BotListeners.LITERAL+"alog NAME`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
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
            } catch (Exception ex) {
                Sentry.capture(ex);
                message.getChannel().sendMessage("<:Watch:326815513550389249> `" + author.getName() + ", the name you've entered is invalid! (" + name.toString().replace("+", " ") + ")`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                this.lastRun = LocalDateTime.now().minusSeconds(getratelimit());
            }
        }
    };

    // Administer commands (won't work anywhere else)
    public static BotCommand m = new BotCommand("command.m") {
        @Override
        void command() {
            if (guild.getId().equals("319606739550863360")) {
                Role maintain[] = new Role[]{guild.getRoleById("319606870606217217")};
                if (guild.getMemberById(author.getId()).getRoles().contains(guild.getRoleById("319606870606217217"))) {
                    guild.getController().removeRolesFromMember(guild.getMemberById(author.getId()), Arrays.asList(maintain)).queue();
                } else {
                    guild.getController().addRolesToMember(guild.getMemberById(author.getId()), Arrays.asList(maintain)).queue();
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
                        if (m.equals(guild.getMember(author)) || m.isOwner() || m.getUser().isBot()) {
                            break;
                        }
                        if (m.getRoles().contains(guild.getRoleById("320300565789802497"))) {
                            cont.removeRolesFromMember(m, guild.getRoleById("320300565789802497")).queue();
                            cont.setNickname(m, "").queue();
                            channel.sendMessage("`" + m.getUser().getName() + " has been un-verified by " + guild.getMember(author).getEffectiveName() + ".`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
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
                            channel.sendMessage("`" + m.getEffectiveName() + " has been verified by " + guild.getMember(author).getEffectiveName() + " as " + name + ".`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                        }
                    }
                }
            }
        }
    };

    // Command utility methods
    private static boolean comapare(String a, String b) {
        Levenshtein l = new Levenshtein();
        JaroWinkler jw = new JaroWinkler();
        return a.contains(b) || b.contains(a) || a.equalsIgnoreCase(b) || l.distance(a, b) < 2 || jw.similarity(a, b) > 0.89d;
    }
    private static Color botColour(String in,int x,int y) {
        try{
            File ciBadge = new File("temp/sample.png");
            URL url = new URL(in);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
            conn.connect();
            FileUtils.copyInputStreamToFile(conn.getInputStream(), ciBadge);
            BufferedImage image = ImageIO.read(ciBadge);
            int c = image.getRGB(x,y);
            int  red = (c & 0x00ff0000) >> 16;
            int  green = (c & 0x0000ff00) >> 8;
            int  blue = c & 0x000000ff;
            return new Color(red,green,blue);
        }catch(Exception ex){
            Sentry.capture(ex);
            return Color.black;
        }
    }
}

