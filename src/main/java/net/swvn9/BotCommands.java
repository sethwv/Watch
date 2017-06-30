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
import net.dv8tion.jda.core.managers.GuildController;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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

@SuppressWarnings("unused")
class BotCommand {
    //BotCommand(String commandNode,Long rateLimit){
    //    this.commandNode = commandNode;
    //    this.rateLimit = rateLimit;
    //}
    protected final String commandNode;
    private final Long rateLimit;
    private final File watchFile;

    protected int jdaShard =0;
    protected final HashSet<String> commandMemory = new HashSet<>();
    protected Message commandMessage;
    protected Guild commandGuild;
    protected MessageChannel commandChannel;
    protected User commandUser;
    protected String commandArgs;
    protected BotUser botUser;
    protected boolean commandWaiting = false;
    protected LocalDateTime lastRun = LocalDateTime.now().minusYears(10L);
    protected MessageChannel lastChannel;
    protected boolean saveMemory = false;

    protected long start;

    protected String helpName = "Undefined";
    protected String helpUsage = "Undefined";
    protected String helpDesc = "Undefined";
    protected Boolean helpSkip = false;

    protected BotCommand(String commandNode) {
        this.commandNode = commandNode;
        this.rateLimit = 10L;
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
                        commandMemory.add(readfile.next());
                    }
                    readfile.close();
                    openfile.close();
                }
            } catch (IOException ignored) {

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
                for (String s : commandMemory) {
                    memstring.append(s).append(" ");
                }
                FileWriter writefile = new FileWriter(watchFile, false);
                writefile.append(memstring);
                writefile.close();
            } catch (IOException ignored) {
            }
        }
    }

    void setCommandWaiting(boolean commandWaiting) {
        this.commandWaiting = commandWaiting;
    }

    void run(Message m) {
        for(JDA jda:Bot.jdas){
            if(jda.getGuilds().contains(m.getGuild())) jdaShard = jda.getShardInfo().getShardId();
        }
        this.commandMessage = m;
        this.commandGuild = m.getGuild();
        this.commandChannel = m.getChannel();
        this.commandUser = m.getAuthor();
        this.botUser = new BotUser(commandUser, commandGuild);
        //commandChannel.sendTyping().queue();
        try{
            TimeUnit.MILLISECONDS.sleep(250);
        }catch(InterruptedException ignored){}
        if (commandNode.contains("#all")) {
            this.commandArgs = commandMessage.getContent().replaceFirst("(?i)::" + (commandNode.replace("command.", "")).replace("#all", ""), "");
        } else {
            this.commandArgs = commandMessage.getContent().replaceFirst("(?i)::" + (commandNode.replace("command.", "")), "");
        }
        if (botUser.hasPermission(commandNode) || botUser.isadmin() || commandNode.contains("#all")) {
            if (LocalDateTime.now().isBefore(lastRun) && !botUser.isadmin()) {
                long Seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), lastRun);
                commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `You can run this command again in " + Seconds + " seconds.` `" + commandMessage.getContent() + "`").queue(msg -> msg.delete().queueAfter((int) Seconds, TimeUnit.SECONDS));
                this.cleanup(true);
                return;
            }
            this.lastRun = LocalDateTime.now().plusSeconds(rateLimit);
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
        if (commandChannel.getMessageById(commandMessage.getId())!=null && commandMessage.getChannel().getType().equals(ChannelType.TEXT) && delete) commandMessage.delete().queue();
        this.commandMessage = null;
        this.commandGuild = null;
        this.commandChannel = null;
        this.commandUser = null;
        this.botUser = null;
        this.commandArgs = null;
        saveMemory();
    }

    void command() throws Exception {
        commandMessage.getChannel().sendMessage("<:WatchWarn:326815513634406419> `This command has not been configured, commandNode: " + this.commandNode + "`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
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
            this.helpName = "Help (This command)";
            this.helpUsage = "::help <keyword> <-a,c>";
            this.helpDesc = "See all of the commands associated with the bot that you can use, sent to you in a dm unless specified otherwise.\n#Flags:\n<-a> All commands\n<-c> In current commandChannel";
            this.helpSkip = false;
        }

        @Override
        void command() {
            EmbedBuilder showCommands = new EmbedBuilder();
            showCommands.setColor(new Color(148, 168, 249));
            showCommands.setFooter("List of commands.", Bot.jdas.get(jdaShard).getSelfUser().getAvatarUrl());
            boolean specific = false;
            String noargs = commandArgs.replace("-a", "").replace("-c", "").trim();
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
                if (botUser.hasPermission(bc.commandNode) || bc.commandNode.contains("#all") || commandArgs.contains("-a") || botUser.isadmin()) {
                    showCommands.addField(bc.helpName, "```Markdown\n#Node: \n" + bc.commandNode.replace("#all", "") + "\n#Usage: \n" + bc.helpUsage + "\n#Description: \n" + bc.helpDesc + "```", true);
                }
            }
            if (commandArgs.contains("-c") && botUser.isadmin()) {
                commandChannel.sendMessage(showCommands.build()).queue();
            } else {
                commandUser.openPrivateChannel().complete().sendMessage(showCommands.build()).queue();
                commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `" + commandUser.getName() + ", check your DMs!`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand inv = new BotCommand("command.inv") {
        @Override
        void help() {
            this.helpName = "Invite";
            this.helpUsage = "::inv";
            this.helpDesc = "Generate a one-time-use invite that is valid for 24 hours";
            this.helpSkip = false;
        }

        @Override
        void command() {
            String invcode;
            invcode = commandGuild.getPublicChannel().createInvite().setMaxUses(1).setMaxAge(24L, TimeUnit.HOURS).setUnique(true).complete().getCode();
            commandChannel.sendMessage("<:Watch:326815513550389249> `An invite has been created and sent to you `").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            commandUser.openPrivateChannel().complete().sendMessage("Your invite is valid for **24 hours and one use**. the link is: http://discord.gg/" + invcode).queue();
        }
    };
    public static BotCommand info = new BotCommand("command.info#all") {
        @Override
        void command() {
            EmbedBuilder stuff = new EmbedBuilder();
            stuff.setTitle(commandUser.getName() + "#" + commandUser.getDiscriminator());
            stuff.setThumbnail(commandUser.getAvatarUrl());
            stuff.setColor(new Color(148, 168, 249));
            DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("dd MMM yyyy");
            stuff.addField("ID", "`" + commandUser.getId() + "`", true);
            stuff.addField("Effective Name", "`" + commandGuild.getMember(commandUser).getEffectiveName() + "`", true);
            stuff.addField("Discord Join Date", "`" + commandUser.getCreationTime().format(dateformat) + "`", true);
            stuff.addField("Status", "`" + commandGuild.getMember(commandUser).getOnlineStatus().name() + "`", false);
            StringBuilder userroles = new StringBuilder();
            for (Role e : commandGuild.getMember(commandUser).getRoles()) {
                userroles.append("- ").append(e.getAsMention()).append("\n");
            }
            if (commandGuild.getMember(commandUser).getRoles().isEmpty()) userroles.append("`(none)`");
            stuff.addField("User Roles", userroles.toString(), false);
            commandChannel.sendMessage(stuff.build()).queue();

            EmbedBuilder rolecases = new EmbedBuilder();
            rolecases.setTitle("Role Interaction Cases");
            rolecases.setColor(new Color(255, 255, 255));
            if (!commandMessage.getMentionedRoles().isEmpty()) {
                for (Role r : commandMessage.getMentionedRoles()) {
                    rolecases.addField(r.getName(), r.getAsMention() + "\t" + commandGuild.getMember(commandUser).canInteract(r), false);
                }
                commandChannel.sendMessage(rolecases.build()).queue();
            }

            EmbedBuilder usercases = new EmbedBuilder();
            usercases.setTitle("User Interaction Cases");
            usercases.setColor(new Color(255, 255, 255));
            if (!commandMessage.getMentionedUsers().isEmpty()) {
                for (User u : commandMessage.getMentionedUsers()) {
                    usercases.addField(commandGuild.getMember(u).getEffectiveName(), u.getAsMention() + "\t" + commandGuild.getMember(commandUser).canInteract(commandGuild.getMember(u)), false);
                }
                commandChannel.sendMessage(usercases.build()).queue();
            }
        }
    };

    // Mod/Admin commands
    public static BotCommand say = new BotCommand("command.say") {
        @Override
        void help() {
            this.helpName = "Say";
            this.helpUsage = "::say <Message>";
            this.helpDesc = "Send a Message as the bot";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (!commandArgs.equals("")) {
                commandChannel.sendMessage(commandMessage.getRawContent().replaceFirst("(?i)::say", "")).queue();
            }
        }
    };
    public static BotCommand ban = new BotCommand("command.ban") {
        @Override
        void help() {
            this.helpName = "Ban";
            this.helpUsage = "::ban <mention(s)> <reason>";
            this.helpDesc = "Ban commandUser(s) with an optional Message";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (!commandArgs.equals("")) {
                TextChannel send = null;
                for (TextChannel c : commandGuild.getTextChannels()) {
                    if (c.getName().equalsIgnoreCase("logs")) {
                        send = c;
                    }
                }
                for (User u : commandMessage.getMentionedUsers()) {
                    this.commandArgs = commandArgs.replace("@" + u.getName(), "").trim();
                    if (commandGuild.getMember(commandUser).canInteract(commandGuild.getMember(u))) if (!u.isBot())
                        u.openPrivateChannel().complete().sendMessage("<:Watch:326815513550389249> You've been banned from " + commandGuild.getName() + " by " + commandUser.getAsMention() + " with the message `" + commandArgs + "`.").queue();
                }
                commandArgs = commandArgs.substring(0, Math.min(commandArgs.length(), 512));
                for (User u : commandMessage.getMentionedUsers()) {
                    if (commandGuild.getMember(commandUser).canInteract(commandGuild.getMember(u))) {
                        if (send != null) {
                            EmbedBuilder log = new EmbedBuilder();
                            log.setColor(new Color(255, 0, 0));
                            log.addField("Action", "Ban", false);
                            log.addField("User", u.getName() + "#" + u.getDiscriminator() + " (" + u.getId() + ")", false);
                            log.addField("Moderator:", commandUser.getName() + "#" + commandUser.getDiscriminator(), false);
                            log.addField("Reason", commandArgs, false);
                            log.setFooter(Bot.jdas.get(jdaShard).getSelfUser().getName() + "#" + Bot.jdas.get(jdaShard).getSelfUser().getDiscriminator(), Bot.jdas.get(1).getSelfUser().getAvatarUrl());
                            log.setTimestamp(LocalDateTime.now());
                            send.sendMessage(log.build()).queue();
                        }
                        if (!u.equals(commandUser)) commandGuild.getController().ban(u, 6, commandArgs).queue();
                        commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `" + commandUser.getName() + " banned " + u.getName() + "#" + u.getDiscriminator() + " (" + commandArgs + ")`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    } else {
                        commandMessage.getChannel().sendMessage("<:WatchError:326815514129072131> `Unable to ban " + u.getName() + "#" + u.getDiscriminator() + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    }
                }
                if (send != null)
                    commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `Bans have been logged in the `" + send.getAsMention() + "` commandChannel.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            } else {
                commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `" + commandUser.getName() + ", you need to mention at least one commandUser ::ban @mention(s)`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand kick = new BotCommand("command.kick") {
        @Override
        void help() {
            this.helpName = "Kick";
            this.helpUsage = "::kick <mention(s)> <reason>";
            this.helpDesc = "Kick commandUser(s) with an optional Message";
            this.helpSkip = false;
        }

        @Override
        void command() {
            TextChannel send = null;
            for (TextChannel c : commandGuild.getTextChannels()) {
                if (c.getName().equalsIgnoreCase("logs")) {
                    send = c;
                }
            }
            if (!commandArgs.equals("")) {
                for (User u : commandMessage.getMentionedUsers()) {
                    this.commandArgs = commandArgs.replace("@" + u.getName(), "").trim();
                    if (commandGuild.getMember(commandUser).canInteract(commandGuild.getMember(u))) if (!u.isBot())
                        u.openPrivateChannel().complete().sendMessage("<:Watch:326815513550389249> You've been kicked from " + commandGuild.getName() + " by " + commandUser.getAsMention() + " with the message `" + commandArgs + "`.").queue();
                }
                commandArgs = commandArgs.substring(0, Math.min(commandArgs.length(), 512));
                for (User u : commandMessage.getMentionedUsers()) {
                    if (commandGuild.getMember(commandUser).canInteract(commandGuild.getMember(u))) {
                        if (send != null) {
                            EmbedBuilder log = new EmbedBuilder();
                            log.setColor(new Color(0, 0, 255));
                            log.addField("Action", "Kick", false);
                            log.addField("User", u.getName() + "#" + u.getDiscriminator() + " (" + u.getId() + ")", false);
                            log.addField("Moderator:", commandUser.getName() + "#" + commandUser.getDiscriminator(), false);
                            log.addField("Reason", commandArgs, false);
                            log.setFooter(Bot.jdas.get(jdaShard).getSelfUser().getName() + "#" + Bot.jdas.get(jdaShard).getSelfUser().getDiscriminator(), Bot.jdas.get(1).getSelfUser().getAvatarUrl());
                            log.setTimestamp(LocalDateTime.now());
                            send.sendMessage(log.build()).queue();
                        }
                        if (!u.equals(commandUser)) commandGuild.getController().kick(u.getId(), commandArgs).queue();
                        commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `" + commandUser.getName() + " kicked " + u.getName() + "#" + u.getDiscriminator() + " (" + commandArgs + ")`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    } else {
                        commandMessage.getChannel().sendMessage("<:WatchError:326815514129072131> `Unable to kick " + u.getName() + "#" + u.getDiscriminator() + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                    }
                }

            } else {
                commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `" + commandUser.getName() + ", you need to mention at least one commandUser ::ban @mention(s)`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand watch = new BotCommand("command.watch") {
        @Override
        void help() {
            this.helpName = "Watch";
            this.helpUsage = "::watch <list/del/add> <keyword>";
            this.helpDesc = "Have the bot \"watch\" for certain keywords in chat, and log any occurrences to a commandChannel called #logs\nKeywords are not case-sensitive";
            this.helpSkip = false;
            this.saveMemory = true;
        }

        @Override
        void command() {
            if (!commandArgs.equals("")) {
                Scanner read = new Scanner(commandArgs);
                if (read.hasNext()) {
                    switch (read.next()) {
                        default:
                            commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `Invalid Syntax` `" + commandArgs + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            break;
                        case "add":
                            if (read.hasNext()) {
                                String keyword = read.next().toLowerCase();
                                if (!commandMemory.contains(keyword)) {
                                    commandMemory.add(keyword);
                                    commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `" + keyword + " has been added to the watch filter. type ::watch del " + keyword + " to remove it.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                } else {
                                    commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `I am already watching for " + keyword + ".`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                }
                            } else {
                                commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify a keyword!` `" + helpUsage + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            }
                            break;
                        case "del":
                            if (read.hasNext()) {
                                String keyword = read.next().toLowerCase();
                                if (commandMemory.contains(keyword)) {
                                    commandMemory.remove(keyword);
                                    commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `" + keyword + " has been removed from the watch filter.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                } else {
                                    commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `I am not currently watching for " + keyword + ". Do ::watch add " + keyword + " to add it to the list.`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                }
                            } else {
                                commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify a keyword!` `" + helpUsage + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            }
                            break;
                        case "list":
                            StringBuilder keywords = new StringBuilder();
                            for (String s : commandMemory) {
                                keywords.append(s).append(", ");
                            }
                            keywords.deleteCharAt(keywords.length() - 1).deleteCharAt(keywords.length() - 1);
                            commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `Right now I'm watching for " + keywords + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            break;
                    }
                }
            } else {
                commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify an action and a keyword!` `" + helpUsage + "`").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand purge = new BotCommand("command.purge") {
        @Override
        void help() {
            this.helpName = "Purge Messages";
            this.helpUsage = "::purge <mentions> <number>";
            this.helpDesc = "Mentions are optional, if a number of messages to purge is not specified, it will be 10. Pinned messages will not be deleted.";
            this.helpSkip = false;
        }

        @Override
        void command() throws Exception {
            this.commandArgs = commandMessage.getRawContent().replaceFirst("(?i)::purge", "");
            this.commandArgs = commandArgs.replaceAll("<(?:@(?:[!&])?|#|:\\w{2,}:)\\d{17,}>", "").trim();
            List<String> remove = new ArrayList<>();
            Scanner args = new Scanner(commandArgs);
            int limit;
            int arg;
            if (args.hasNextInt()) {
                arg = args.nextInt();
                limit = arg+2;
            } else {
                limit = 10;
            }
            int total = 0;
            Message msg = commandChannel.sendMessage("<:Watch:326815513550389249> `Attempting to purge " + (limit-2) + " messages.`").complete();
            for (int i = 0; limit > 0; i++) {
                int todelete;
                if (limit > 100) {
                    todelete = 100;
                } else {
                    todelete = limit;
                }
                List<Message> toPurge = commandGuild.getTextChannelById(commandChannel.getId()).getIterableHistory().stream()
                        .limit(todelete)
                        .filter(m -> m.getCreationTime().isAfter(OffsetDateTime.now().minusDays(13)))
                        .filter(m -> !m.isPinned())
                        .filter(m -> !commandMessage.equals(m))
                        .filter(m -> !msg.equals(m))
                        .collect(Collectors.toList());
                limit -= 100;
                if (!commandMessage.getMentionedUsers().isEmpty())
                    toPurge = toPurge.stream().filter(m -> commandMessage.getMentionedUsers().contains(m.getAuthor())).collect(Collectors.toList());
                total += toPurge.size();
                try {
                    commandGuild.getTextChannelById(commandChannel.getId()).deleteMessages(toPurge).queue();

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
            this.helpName = "ID";
            this.helpUsage = "::id";
            this.helpDesc = "Grab the ID and any permissions associated with your commandUser ID.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            commandChannel.sendMessage(commandUser.getAsMention() + ", Your ID is " + commandUser + ".").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            commandChannel.sendMessage(botUser.getPermissions().toString()).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            commandChannel.sendMessage(botUser.isadmin() + "").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    };
    public static BotCommand roles = new BotCommand("command.roles") {
        @Override
        void help() {
            this.helpName = "Roles";
            this.helpUsage = "::roles";
            this.helpDesc = "Get all of the role-names and IDs associated with the current discord commandGuild.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            EmbedBuilder roles = new EmbedBuilder();
            roles.setFooter("Roles for " + commandGuild.getName(), Bot.jdas.get(jdaShard).getSelfUser().getAvatarUrl());
            roles.setThumbnail(commandGuild.getIconUrl());
            roles.setTimestamp(LocalDateTime.now());
            for (Object s : commandGuild.getRoles().toArray()) {
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
            commandChannel.sendMessage(roles.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
        }
    };
    public static BotCommand showconfig = new BotCommand("command.showconfig") {
        @Override
        void help() {
            this.helpName = "Show Config";
            this.helpUsage = "::showconfig";
            this.helpDesc = "Spit out the contents of the Config.yml file to a rich embed.";
            this.helpSkip = false;
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
            other.setFooter("Config.yml (May not show all entries)", Bot.jdas.get(jdaShard).getSelfUser().getAvatarUrl());
            other.setTimestamp(LocalDateTime.now());
            commandChannel.sendMessage(other.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
        }
    };
    public static BotCommand pullconfig = new BotCommand("command.pullconfig") {
        @Override
        void help() {
            this.helpName = "Pull Config";
            this.helpUsage = "::pullconfig";
            this.helpDesc = "Pull the latest configuration from the Config.yml file.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            Config.loadConfig();
            WHITELIST = Config.getWhitelist();
            //commandChannel.addReactionById(commandMessage.getId(), "👍").queue();
        }
    };

    // Owner commands
    public static BotCommand shard = new BotCommand("command.shard"){
        @Override
        void command() throws Exception {
            EmbedBuilder sharding = new EmbedBuilder();
            sharding.setColor(new Color(114, 137, 218));
            sharding.setTimestamp(LocalDateTime.now());
            sharding.setFooter("Watch #6969 ",Bot.jdas.get(jdaShard).getSelfUser().getAvatarUrl());
            if(commandArgs.contains("-l")){
                for(int i=0;i<Bot.jdas.size();i++){
                    sharding.addField("Shard "+Bot.jdas.get(i).getShardInfo().getShardId()+" of "+Bot.jdas.get(i).getShardInfo().getShardTotal(),"```java\n"+Bot.jdas.get(i).getGuilds().toString()+"```",false);
                }
            }
            commandChannel.sendMessage(sharding.build()).queue();
        }
    };
    public static BotCommand kill = new BotCommand("command.kill") {
        @Override
        void help() {
            this.helpName = "Kill";
            this.helpUsage = "::kill";
            this.helpDesc = "Kill the bot and return the host machine to the command line/desktop.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (commandChannel.getType().equals(ChannelType.TEXT)) commandMessage.delete().queue();
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
            this.helpName = "Stop";
            this.helpUsage = "::stop";
            this.helpDesc = "Stop the jdaShard that the commandGuild is running on. (There is no way to restart)";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (commandChannel.getType().equals(ChannelType.TEXT)) commandMessage.delete().queue();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException eeee) {
                eeee.getMessage();
            }
            Bot.jdas.get(jdaShard).removeEventListener(Bot.jdas.get(jdaShard).getRegisteredListeners());
            Bot.jdas.get(jdaShard).shutdown(true);
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
            this.helpName = "Restart";
            this.helpUsage = "::restart";
            this.helpDesc = "Restart the bot, re-initialise everything.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (commandChannel.getType().equals(ChannelType.TEXT)) commandMessage.delete().queue();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException eeee) {
                eeee.getMessage();
            }
            Bot.restart(jdaShard);
        }
    };
    public static BotCommand input = new BotCommand("command.input") {
        @Override
        void help() {
            this.helpName = "Enable/Disable Input";
            this.helpUsage = "::input";
            this.helpDesc = "Enable or disable input for the bot so that the bot can stay running while testing.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (!commandWaiting) {
                this.commandWaiting = true;
                for(JDA jda:Bot.jdas){
                    jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                }
            } else {
                this.commandWaiting = false;
                if(BotReady.isDevelopmentEnvironment()){
                    for(JDA jda:Bot.jdas){
                        jda.getPresence().setStatus(OnlineStatus.IDLE);
                        jda.getPresence().setGame(Game.of(BotEvent.icon.get(jdaShard)+""));
                    }
                } else {
                    for(JDA jda:Bot.jdas){
                        jda.getPresence().setStatus(OnlineStatus.ONLINE);
                        jda.getPresence().setGame(Game.of(BotEvent.icon.get(jdaShard)));
                    }
                }
            }
        }
    };
    public static BotCommand bot = new BotCommand("command.bot") {
        @Override
        void help() {
            this.helpName = "Bot Utility";
            this.helpUsage = "::bot <-s/-a/-k> <?listener>";
            this.helpDesc = "You should probably stay away from everything this command does except for ::bot -s";
            this.helpSkip = false;
        }

        @Override
        void command() {
            if (commandArgs.contains("-s")) {
                EmbedBuilder stats = new EmbedBuilder();
                stats.setTimestamp(LocalDateTime.now());
                stats.setColor(new Color(114, 137, 218));
                stats.addField("Status", Bot.jdas.get(jdaShard).getStatus().name(), false);
                stats.addField("Heartbeat", "" + Bot.jdas.get(jdaShard).getPing() + "ms", true);
                stats.addField("API Responses", "" +Bot.jdas.get(jdaShard).getResponseTotal() + "", true);

                stats.setFooter("Watch#6969 ", Bot.jdas.get(jdaShard).getSelfUser().getAvatarUrl());

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
                for (String s : Bot.jdas.get(jdaShard).getCloudflareRays()) {
                    cfr.append("<").append(number++).append("/").append(s).append(">\n");
                }
                stats.addField("Shard Info",Bot.jdas.get(jdaShard).getShardInfo().toString(),true);
                stats.addField("CF Rays", "```Markdown\n" + cfr.toString() + "```", false);
                StringBuilder lst = new StringBuilder();
                int number2 = 0;
                for (Object obj : Bot.jdas.get(jdaShard).getRegisteredListeners()) {
                    lst.append("<").append(number2++).append("/").append(obj.toString()).append(">\n");
                }
                stats.addField("Listeners", "```Markdown\n" + lst.toString() + "```", false);
                commandChannel.sendMessage(stats.build()).queue();
            } else if (commandArgs.contains("-k")) {
                commandArgs = commandArgs.replace("-k", "");
                Scanner ln = new Scanner(commandArgs);
                if (ln.hasNextInt()) {
                    int chosen = ln.nextInt();
                    commandChannel.sendMessage("```Markdown\nYou have removed listener <" + chosen + "/" + Bot.jdas.get(jdaShard).getRegisteredListeners().get(chosen).toString() + ">```").queue();
                    Bot.jdas.get(jdaShard).removeEventListener(Bot.jdas.get(jdaShard).getRegisteredListeners().get(chosen));
                }
            } else if (commandArgs.contains("-a")) {
                commandArgs = commandArgs.replace("-a", "");
                Scanner ln = new Scanner(commandArgs);
                if (ln.hasNext()) {
                    String chosenL = ln.next();
                    switch (chosenL.toLowerCase()) {
                        case "botevent":
                            commandChannel.sendMessage("```Markdown\nYou have added a listener <net.swvn9.BotEvent>```").queue();
                            Bot.jdas.get(jdaShard).addEventListener(new BotEvent());
                            break;
                        case "botready":
                            commandChannel.sendMessage("```Markdown\nYou have added a listener <net.swvn9.BotReady>```").queue();
                            Bot.jdas.get(jdaShard).addEventListener(new BotReady());
                            break;
                        case "botlogging":
                            commandChannel.sendMessage("```Markdown\nYou have added a listener <net.swvn9.BotLogging>```").queue();
                            Bot.jdas.get(jdaShard).addEventListener(new BotLogging());
                            break;
                    }
                }
            }
        }
    };
    public static BotCommand eval = new BotCommand("command.eval") {
        @Override
        void help() {
            this.helpName = "Javascript Eval";
            this.helpUsage = "::eval <line>";
            this.helpDesc = "Evaluate a line of JS";
            this.helpSkip = false;
        }

        @Override
        void command() {
            try {
                engine.put("jda",Bot.jdas.get(jdaShard));
                engine.put("channel", commandChannel);
                engine.put("message", commandMessage);
                engine.put("guild", commandGuild);
                engine.put("user",commandUser);
                engine.put("moji",EmojiManager.getAllTags());
                commandChannel.sendMessage("```java\n//Evaluating\n" + commandArgs.replaceAll("\n","").replaceAll(";",";\n").trim() + "```").queue();
                String res = engine.eval(commandArgs).toString();
                if(res!=null)  commandChannel.sendMessage("```js\n//Response\n" + res + "```").queue();
            } catch (Exception se) {
                if(se.getClass()!=NullPointerException.class) commandChannel.sendMessage("```js\n//Exception\n" + se + "```").queue();
            }
        }
    };
    public static BotCommand link = new BotCommand("command.link") {
        @Override
        void help() {
            this.helpName = "Rebrandly Link";
            this.helpUsage = "::link <url> <redirect>";
            this.helpDesc = "Using the rebrandly api token defined in the config file, create redirect links.";
            this.helpSkip = false;
        }

        @Override
        void command() throws Exception {
            boolean info = false;
            if (commandArgs.contains("-j")) {
                info = true;
                commandArgs = commandArgs.replace("-j", "").trim();
            }
            Scanner args = new Scanner(commandArgs);
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
                        commandChannel.sendMessage("<:Watch:326815513550389249> **http://" + Config.getrebrandlyURL() + "/" + shortl + "** now links to **" + ouath + "**.\n```json\n" + line + "```").queue();
                    if (!info)
                        commandChannel.sendMessage("<:Watch:326815513550389249> **http://" + Config.getrebrandlyURL() + "/" + shortl + "** now links to **" + ouath + "**.").queue();
                } else {
                    commandChannel.sendMessage("<:WatchError:326815514129072131> " + res + " Something went wrong\n```JSON" + line + "```").queue();
                }
            }
            connection.disconnect();
        }
    };
    public static BotCommand icon = new BotCommand("command.icon") {
        @Override
        void command() throws Exception {
            BotReady.newIcons();
            commandChannel.sendMessage("<:Watch:326815513550389249>` Changed shard emojis.`").queue(m->m.delete().queueAfter(10,TimeUnit.SECONDS));
        }
    };


    // RuneScape commands
    public static BotCommand clan = new BotCommand("command.clan") {
        @Override
        void help() {
            this.helpName = "RuneScape Clan Ranks";
            this.helpUsage = "::clan";
            this.helpDesc = "(for now) Pull the upper ranks of the RuneScape clan Zamorak Cult, and match any names with those on the current discord commandGuild.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            RuneScapeAPI api = RuneScapeAPI.createHttp();
            Hiscores hiscores = api.hiscores();

            String Clan = "Zamorak Cult";
            try {
                List<ClanMate> clanMates = hiscores.clanInformation(Clan);
                EmbedBuilder Ranks = new EmbedBuilder();
                StringBuilder One = new StringBuilder();
                if (commandGuild.getId().equals("254861442799370240") && Clan.equals("Zamorak Cult"))
                    One.append(commandGuild.getRoleById(254883837757227008L).getAsMention()).append(System.lineSeparator());
                StringBuilder Two = new StringBuilder();
                if (commandGuild.getId().equals("254861442799370240") && Clan.equals("Zamorak Cult"))
                    Two.append(commandGuild.getRoleById(268490396617801729L).getAsMention()).append(System.lineSeparator());
                StringBuilder Three = new StringBuilder();
                if (commandGuild.getId().equals("254861442799370240") && Clan.equals("Zamorak Cult"))
                    Three.append(commandGuild.getRoleById(258350529229357057L).getAsMention()).append(System.lineSeparator());
                StringBuilder Four = new StringBuilder();
                if (commandGuild.getId().equals("254861442799370240") && Clan.equals("Zamorak Cult"))
                    Four.append(commandGuild.getRoleById(254881136524656640L).getAsMention()).append(System.lineSeparator());
                StringBuilder Five = new StringBuilder();
                StringBuilder Six = new StringBuilder();
                int admins = StringUtils.countMatches(clanMates.toString(), "Admin") / 2 + 1;
                int admincount = 0;
                for (ClanMate a : clanMates) {
                    boolean found = false;
                    switch (a.getRank()) {
                        case "Owner":
                            for (Member b : commandGuild.getMembers()) {
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
                            for (Member b : commandGuild.getMembers()) {
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
                            for (Member b : commandGuild.getMembers()) {
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
                            for (Member b : commandGuild.getMembers()) {
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
                            for (Member b : commandGuild.getMembers()) {
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
                Ranks.setFooter("Generated " + Time + " For " + Clan, Bot.jdas.get(jdaShard).getSelfUser().getAvatarUrl());
                commandChannel.sendMessage(Ranks.build()).queue(msg -> msg.delete().queueAfter(2, TimeUnit.MINUTES));
            } catch (java.lang.NoClassDefFoundError | IOException eeeee) {
                System.out.println(eeeee.getMessage());
                eeeee.printStackTrace();
            }

        }
    };
    public static BotCommand alog = new BotCommand("command.alog#all") {
        @Override
        void help() {
            this.helpName = "RuneScape Adventurer's Log";
            this.helpUsage = "::alog <RunescapeName>";
            this.helpDesc = "Fetch the RuneScape adventurer's log for the specified player name.";
            this.helpSkip = false;
        }

        @Override
        void command() {
            StringBuilder name = new StringBuilder();
            try {
                Scanner rsn = new Scanner(commandArgs);
                if (rsn.hasNext()) {
                    name = new StringBuilder(rsn.next());
                    while (rsn.hasNext()) {
                        name.append("+").append(rsn.next());
                    }
                } else {
                    commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `" + commandUser.getName() + ", you need to enter a name! ::alog NAME`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
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
                commandChannel.sendMessage(test.toString()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.MINUTES));
            } catch (IOException | FeedException e) {
                commandMessage.getChannel().sendMessage("<:Watch:326815513550389249> `" + commandUser.getName() + ", the name you've entered is invalid! (" + name.toString().replace("+", " ") + ")`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                this.lastRun = LocalDateTime.now().minusSeconds(getratelimit());
            }
        }
    };

    // Administer commands (won't work anywhere else)
    public static BotCommand m = new BotCommand("command.m") {
        @Override
        void command() {
            if (commandGuild.getId().equals("319606739550863360")) {
                Role maintain[] = new Role[]{commandGuild.getRoleById("319606870606217217")};
                if (commandGuild.getMemberById(commandUser.getId()).getRoles().contains(commandGuild.getRoleById("319606870606217217"))) {
                    commandGuild.getController().removeRolesFromMember(commandGuild.getMemberById(commandUser.getId()), Arrays.asList(maintain)).queue();
                } else {
                    commandGuild.getController().addRolesToMember(commandGuild.getMemberById(commandUser.getId()), Arrays.asList(maintain)).queue();
                }
            }
        }
    };
    public static BotCommand v = new BotCommand("command.v") {
        @Override
        void command() {
            if (commandGuild.getId().equals("319606739550863360")) {
                GuildController cont = commandGuild.getController();
                Scanner watch = new Scanner(commandMessage.getRawContent());
                String mention = watch.next();
                for (Member m : commandGuild.getMembers()) {
                    if (mention.equals(m.getAsMention())) {
                        if (m.equals(commandGuild.getMember(commandUser)) || m.isOwner() || m.getUser().isBot()) {
                            break;
                        }
                        if (m.getRoles().contains(commandGuild.getRoleById("320300565789802497"))) {
                            cont.removeRolesFromMember(m, commandGuild.getRoleById("320300565789802497")).queue();
                            cont.setNickname(m, "").queue();
                            commandChannel.sendMessage("`" + m.getUser().getName() + " has been un-verified by " + commandGuild.getMember(commandUser).getEffectiveName() + ".`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                        } else {
                            StringBuilder name = new StringBuilder(m.getEffectiveName());
                            if (watch.hasNext()) {
                                name = new StringBuilder(watch.next());
                                while (watch.hasNext()) {
                                    name.append(" ").append(watch.next());
                                }
                            }
                            cont.setNickname(m, name.toString()).queue();
                            cont.addRolesToMember(m, commandGuild.getRoleById("320300565789802497")).queue();
                            commandChannel.sendMessage("`" + m.getEffectiveName() + " has been verified by " + commandGuild.getMember(commandUser).getEffectiveName() + " as " + name + ".`").queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                        }
                    }
                }
            }
        }
    };
}
