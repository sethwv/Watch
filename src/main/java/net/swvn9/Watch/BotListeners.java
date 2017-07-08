package net.swvn9.Watch;

import io.sentry.Sentry;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

class BotGeneric implements net.dv8tion.jda.core.hooks.EventListener {

    @Override
    public void onEvent(Event event) {
        /*
        if (event instanceof ReadyEvent) {
            UNUSED
        }
        */
    }

}

@SuppressWarnings("unused")
class BotListeners extends ListenerAdapter {

    public static String LITERAL = "::";
    static String WHITELIST[] = BotConfig.getWhitelist();
    private static final String LOGTIME = new SimpleDateFormat("MMMDDYY_HHmmss").format(new Date());
    private static FileWriter log;
    private static Guild Home;
    private static Random rand = new Random();

    //check if a specific channel ID is on the whitelist
    private boolean channelWhitelisted(String channelID) {
        for (String value : WHITELIST) { //for each value in the whitelist array
            if (value.equalsIgnoreCase(channelID)) return true; //if the id is whitelisted, return true.
        }
        return false; //if it isn't in the whitelist, return false
    }

    //create a prefix for any log entries
    static String logPrefix(int type) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date()); //ge tthe current timestamp
        String logType; //prepare the log type string
        switch (type) {
            default:
                logType = "Info"; //Mark the type as Info
                break;
            case 1:
                logType = "Error"; //mark the type as Error
                break;
            case 2:
                logType = "Chat"; //mark the type as Chat
                break;
            case 3:
                logType = "Moji"; //mark the type as Emoji/Reaction
                break;
            case 4:
                logType = "CACH";
                break;
            case 5:
                logType = "DBUG";
                break;
            case 6:
                logType = "Perm";
                break;
            case 8:
                logType = "SHRD";
                break;
        }
        return "[" + timeStamp + "] [" + logType + "] [Log]: "; //create and return the log prefix
    }
    static String logPrefix(int type, int shard) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date()); //ge tthe current timestamp
        String logType; //prepare the log type string
        switch (type) {
            default:
                logType = "Info"; //Mark the type as Info
                break;
            case 1:
                logType = "Error"; //mark the type as Error
                break;
            case 2:
                logType = "Chat"; //mark the type as Chat
                break;
            case 3:
                logType = "Moji"; //mark the type as Emoji/Reaction
                break;
            case 4:
                logType = "CACH";
                break;
            case 5:
                logType = "DBUG";
                break;
            case 6:
                logType = "Perm";
                break;
        }
        return "[" + timeStamp + "] [" + logType + "] [" + (shard + 1) + "] [Log]: "; //create and return the log prefix
    }
    public static boolean isDevelopmentEnvironment() {
        boolean isDev = true;
        if (System.getenv("deven") == null) {
            isDev = false;
        }
        return isDev;
        //return false;
    }
    static void logger(String input) {
        try {
            BotListeners.log = new FileWriter("Logs" + File.separator + "LOG_" + LOGTIME + ".txt", true);
            log.write(input + System.lineSeparator());
            log.close();
            System.out.println(input);
        } catch (Exception ex) {
            Sentry.capture(ex);
        }
    }
    private static boolean isCommand(Message m) {
        return m.getContent().indexOf(LITERAL) == 0;
    }

    @Override
    public void onReady(ReadyEvent event){
        for (Guild a : event.getJDA().getGuilds()) {
            switch (a.getId()) {
                //Possibility for special actions per-guild
                default:
                    BotListeners.logger(BotListeners.logPrefix(0, event.getJDA().getShardInfo().getShardId()) + "I'm in " + a.getName() + "! ID:" + a.getId());
                    break;
            }
        }
        BotCommands.bot.start = System.nanoTime();
        if (isDevelopmentEnvironment()) {
            event.getJDA().getPresence().setStatus(OnlineStatus.IDLE);
            /*if (LocalDateTime.now().getMonth() == Month.JULY && LocalDateTime.now().getDayOfMonth() == 1) {
                //event.getJDA().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                event.getJDA().getPresence().setGame(Game.of("\uD83C\uDF41Happy 150 ("+(event.getJDA().getShardInfo().getShardId()+1)+")"));
            } else*/ {
                event.getJDA().getPresence().setGame(Game.of("☕in Dev mode, "+(event.getJDA().getShardInfo().getShardId()+1)));
            }
        } else {
            event.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
            /*if (LocalDateTime.now().getMonth() == Month.JULY && LocalDateTime.now().getDayOfMonth() == 1) {
                //event.getJDA().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                event.getJDA().getPresence().setGame(Game.of("\uD83C\uDF41Happy 150 ("+(event.getJDA().getShardInfo().getShardId()+1)+")"));
                event.getJDA().getPresence().setGame(Game.of("\uD83C\uDF41Happy 150"));
            } else*/ {
                //event.getJDA().getPresence().setGame(Game.of("☕"));
                event.getJDA().getPresence().setGame(Game.of("☕Shard "+(event.getJDA().getShardInfo().getShardId()+1)));
            }
        }
    }

    @Override //any message sent that the bot can see
    public void onMessageReceived(MessageReceivedEvent e) {
        if(e.getGuild().getId().equals("254861442799370240")){
            BotListeners.LITERAL = "!";
        } else {
            BotListeners.LITERAL = "::";
        }
        if ((e.getAuthor().isBot() || e.getChannelType().equals(ChannelType.PRIVATE))) {
            return;
        }
        if (BotCommands.input.waiting && !e.getMessage().getContent().contains(LITERAL+"input")) {
            return;
        }

        if (Home == null) BotListeners.Home = e.getGuild();

        String input = e.getMessage().getRawContent();
        if (e.isFromType(ChannelType.TEXT))
            logger(logPrefix(2, e.getJDA().getShardInfo().getShardId()) + "(" + e.getGuild().getName() + ", #" + e.getTextChannel().getName() + ") " + e.getAuthor().getName() + ": " + input);
        if (e.isFromType(ChannelType.PRIVATE))
            logger(logPrefix(2, e.getJDA().getShardInfo().getShardId()) + "(Private Message) " + e.getAuthor().getName() + ": " + input);


        for (String s : BotCommands.watch.memory) {
            if (input.toLowerCase().contains(s) && !input.contains(LITERAL+"watch")) {
                TextChannel send = null;
                for (TextChannel c : e.getGuild().getTextChannels()) {
                    if (c.getName().contains("logs")) {
                        send = c;
                    }
                }
                if (send != null) {
                    EmbedBuilder log = new EmbedBuilder();
                    log.addField("A watched keyword has been said", s, false);
                    log.addField("Name", e.getAuthor().getAsMention(), true);
                    log.addField("Message", input, true);
                    log.addField("Channel", e.getTextChannel().getAsMention(), true);
                    log.setColor(new Color(148, 168, 249));
                    send.sendMessage(log.build()).queue();
                    send.sendMessage("@here").complete().delete().queueAfter(1, TimeUnit.SECONDS);
                    //e.getMessage().delete().queue();
                }
            }
        }

        if (e.getChannel().getId().equals("320615332840472576") && !e.getAuthor().isBot()) {
            if (input.charAt(0) == '<') {
                BotCommands.v.run(e.getMessage());
            }
        }

        if (e.getGuild().getId().equals("319606739550863360")) {
            Scanner message = new Scanner(input);
            if (input.contains("vote") || input.contains("poll") || input.contains("Vote") || input.contains("Poll")) {
                e.getChannel().sendMessage("<@&319607280540712961>, " + e.getMember().getEffectiveName() + " has called a vote! Leave your vote in the form of a reaction on this message!\n\n" + input).queue(msg -> {
                    while (message.hasNext()) {
                        String z = message.next();
                        switch (z) {
                            default:
                                break;
                            case "\uD83C\uDDE6":
                                e.getChannel().addReactionById(msg.getId(), z).queue();
                                break;
                            case "\uD83C\uDDE7":
                                e.getChannel().addReactionById(msg.getId(), z).queue();
                                break;
                            case "\uD83C\uDDE8":
                                e.getChannel().addReactionById(msg.getId(), z).queue();
                                break;
                            case "\uD83C\uDDE9":
                                e.getChannel().addReactionById(msg.getId(), z).queue();
                                break;
                            case "\uD83C\uDDEA":
                                e.getChannel().addReactionById(msg.getId(), z).queue();
                                break;
                            case "\uD83C\uDDEB":
                                e.getChannel().addReactionById(msg.getId(), z).queue();
                                break;
                            case "\uD83C\uDDEC":
                                e.getChannel().addReactionById(msg.getId(), z).queue();
                                break;
                            case "\uD83C\uDDED":
                                e.getChannel().addReactionById(msg.getId(), z).queue();
                                break;
                        }
                    }
                });
                if (e.getChannelType().isGuild()) e.getMessage().delete().queue();
            }
        }


        if ((isCommand(e.getMessage()))) {
            input = input.replaceFirst(LITERAL, "");
            Scanner command = new Scanner(input);
            if (command.hasNext()) {
                for (BotCommand b : BotCommands.commandList) {
                    if (input.toLowerCase().indexOf(b.commandNode.replace("command.", "").replace("#all", "").toLowerCase()) == 0 && !input.contains("verify")) {
                        b.run(e.getMessage());
                        break;
                    }
                }
            }
            command.close();
        }
    }

}
