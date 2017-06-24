package net.swvn9;

import com.mikebull94.rsapi.RuneScapeAPI;
import com.mikebull94.rsapi.hiscores.*;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.GuildController;
import org.apache.commons.lang3.StringUtils;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.swvn9.EventListener.WHITELIST;

class BotCommand{
    //BotCommand(String node,Long ratelimit){
    //    this.node = node;
    //    this.ratelimit = ratelimit;
    //}
    BotCommand(String node){
        this.node = node;
        this.ratelimit = 10L;
        this.watchfile = new File("Commands"+File.separator+(node.replace("command.","").replace("#all",""))+".watch");
        this.help();
        BotCommands.commandList.add(this);
        if(savemem){
            try{
                if(!watchfile.exists()){
                    FileWriter newFile = new FileWriter(watchfile,false);
                    newFile.close();
                } else {
                    FileReader openfile = new FileReader(watchfile);
                    Scanner readfile = new Scanner(openfile);
                    while(readfile.hasNext()){
                        memory.add(readfile.next());
                    }
                    readfile.close();
                    openfile.close();
                }
            }catch(IOException eee){

            }
        }
    }

    protected final String node;
    private final Long ratelimit;
    private final File watchfile;

    long getratelimit(){
        return this.ratelimit;
    }

    protected HashSet<String> memory = new HashSet<>();
    protected Message message;
    protected Guild guild;
    protected MessageChannel channel;
    protected User user;
    protected String commandargs;
    protected BotUser botUser;
    protected boolean waiting=false;
    protected LocalDateTime Lastrun = LocalDateTime.now().minusYears(10L);
    protected MessageChannel lastchannel;
    protected boolean savemem = false;

    protected String helpname = "Undefined";
    protected String delpusage = "Undefined";
    protected String helpdesc = "Undefined";
    protected Boolean skip = false;


    boolean getWaiting(){
        return waiting;
    }

    void help(){
        this.helpname = "Undefined";
        this.delpusage = "Undefined";
        this.helpdesc = "Undefined";
        this.skip = true;
    }

    void saveMemory(){
            if(savemem) {
                try {
                    StringBuilder memstring = new StringBuilder();
                    for (String s : memory) {
                        memstring.append(s).append(" ");
                    }
                    FileWriter writefile = new FileWriter(watchfile, false);
                    writefile.append(memstring);
                    writefile.close();
                } catch (IOException eee) {
                }
            }
    }

    void setWaiting(boolean waiting){
        this.waiting = waiting;
    }

    void run(Message m){
        this.message = m;
        this.guild = m.getGuild();
        this.channel = m.getChannel();
        this.user = m.getAuthor();
        this.botUser = new BotUser(user,guild);
        if (node.contains("#all")) {
            this.commandargs = message.getContent().replaceFirst("(?i)::"+(node.replace("command.","")).replace("#all",""),"");
        } else {
            this.commandargs = message.getContent().replaceFirst("(?i)::"+(node.replace("command.","")),"");
        }
        if(botUser.hasPermission(node)||botUser.isadmin()||node.contains("#all")){
            if(LocalDateTime.now().isBefore(Lastrun)&&!botUser.isadmin()){
                long Seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), Lastrun);
                message.getChannel().sendMessage("<:Watch:326815513550389249> `You can run this command again in "+Seconds+" seconds.` `"+message.getContent()+"`").queue(msg->msg.delete().queueAfter((int)Seconds, TimeUnit.SECONDS));
                this.cleanup(true);
                return;
            }
            this.Lastrun = LocalDateTime.now().plusSeconds(ratelimit);
            this.command();
            this.cleanup(true);
        } else {
            this.cleanup(false);
        }
    }
    void cleanup(boolean delete){
        if(message.getChannel().getType().equals(ChannelType.TEXT)&&delete) message.delete().queue();
        this.message = null;
        this.guild = null;
        this.channel = null;
        this.user = null;
        this.botUser = null;
        this.commandargs = null;
        saveMemory();
    }
    void command(){
        message.getChannel().sendMessage("<:WatchWarn:326815513634406419> `This command has not been configured, node: "+this.node+"`").queue(msg->msg.delete().queueAfter(10, TimeUnit.SECONDS));
    }

}

class BotCommands {
    public static HashSet<BotCommand> commandList = new HashSet<>();

    private static boolean comapare(String a,String b) {
        Levenshtein l = new Levenshtein();
        JaroWinkler jw = new JaroWinkler();
        return a.contains(b) || b.contains(a) || a.equalsIgnoreCase(b) || l.distance(a, b) < 2 || jw.similarity(a, b) > 0.89d;
    }

    // LITERAL COMMANDS

    public static BotCommand help = new BotCommand("command.help#all"){
        @Override
        void help(){
            this.helpname = "Help (This command)";
            this.delpusage = "::help <keyword> <-a>";
            this.helpdesc = "See all of the commands associated with the bot that you can use. Add the -a flag to see all commands.";
            this.skip = false;
        }
        @Override
        void command(){
            EmbedBuilder showCommands = new EmbedBuilder();
            showCommands.setColor(new Color(148,168,249));
            showCommands.setFooter("List of commands.",Bot.jda.getSelfUser().getAvatarUrl());
            boolean specific = false;
            if(new Scanner(commandargs).hasNext()) {
                String next = new Scanner(commandargs).next();
                for (BotCommand bc : commandList) {
                    if ((bc.helpname.toLowerCase()).contains(next.toLowerCase())) {
                        specific = true;
                        showCommands.addField(bc.helpname, "```YAML\nNode: " + bc.node.replace("#all", "") + "\nUsage: " + bc.delpusage + "\nDescription: " + bc.helpdesc + "```", true);
                    }
                }
            }
            for(BotCommand bc:commandList){
                if(bc.skip||specific) continue;
                if(botUser.hasPermission(bc.node)||bc.node.contains("#all")||commandargs.contains("-a")||botUser.isadmin()){
                    showCommands.addField(bc.helpname,"```YAML\nNode: "+bc.node.replace("#all","")+"\nUsage: "+bc.delpusage+"\nDescription: "+bc.helpdesc+"```",true);
                }
            }
            channel.sendMessage(showCommands.build()).queue(msg->msg.delete().queueAfter(1,TimeUnit.MINUTES));
        }
    };

    public static BotCommand say = new BotCommand("command.say"){
        @Override
        void help(){
            this.helpname = "Say";
            this.delpusage = "::say (Message)";
            this.helpdesc = "Send a message as the bot";
            this.skip = false;
        }
        @Override
        void command(){
            if(!commandargs.equals("")){
                channel.sendMessage(commandargs).queue();
            }
        }
    };
    public static BotCommand ban = new BotCommand("command.ban"){
        @Override
        void help(){
            this.helpname = "Ban";
            this.delpusage = "::ban [user mention(s)] <reason>";
            this.helpdesc = "Ban user(s) with an optional message";
            this.skip = false;
        }
        @Override
        void command(){
            if(!commandargs.equals("")){
                for(User u:message.getMentionedUsers()){
                    this.commandargs = commandargs.replace("@"+u.getName(),"").trim();
                }
                for(User u:message.getMentionedUsers()){
                    guild.getController().ban(u,6,commandargs).queue();
                    message.getChannel().sendMessage("<:Watch:326815513550389249> `"+user.getName()+" banned "+u.getName()+"#"+u.getDiscriminator()+" ("+commandargs+")`").queue();
                }

            } else {
                message.getChannel().sendMessage("<:Watch:326815513550389249> `"+user.getName()+", you need to mention at least one user ::ban @mention(s)`").queue(msg->msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };
    public static BotCommand id = new BotCommand("command.id"){
        @Override
        void help(){
            this.helpname = "ID";
            this.delpusage = "::id";
            this.helpdesc = "Grab the ID and any permissions associated with your user ID.";
            this.skip = false;
        }
        @Override
        void command(){
            channel.sendMessage(user.getAsMention()+", Your ID is "+user+".").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            channel.sendMessage(botUser.getPermissions().toString()).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            channel.sendMessage(botUser.isadmin()+"").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    };
    public static BotCommand roles = new BotCommand("command.roles"){
        @Override
        void help(){
            this.helpname = "Roles";
            this.delpusage = "::roles";
            this.helpdesc = "Get all of the role-names and IDs associated with the current discord guild.";
            this.skip = false;
        }
        @Override
        void command(){
            EmbedBuilder roles = new EmbedBuilder();
            roles.setFooter("Roles for " + guild.getName(), Bot.jda.getSelfUser().getAvatarUrl());
            roles.setThumbnail(guild.getIconUrl());
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
            roles.setColor(new Color(148,168,249));
            channel.sendMessage(roles.build()).queue(msg->msg.delete().queueAfter(1,TimeUnit.MINUTES));
        }
    };
    public static BotCommand showconfig = new BotCommand("command.showconfig"){
        @Override
        void help(){
            this.helpname = "Show Config";
            this.delpusage = "::showconfig";
            this.helpdesc = "Spit out the contents of the Config.yml file to a rich embed.";
            this.skip = false;
        }
        @Override
        void command(){
            EmbedBuilder other = new EmbedBuilder();
            StringBuilder whitelisted = new StringBuilder();
            other.setColor(new Color(148,168,249));
            for (String a : WHITELIST)
                whitelisted.append("- `").append(a).append("`").append(System.lineSeparator());
            //other.addField("Whitelist", whitelisted.toString(), false);
            other.setFooter("Settings + Whitelist from Config.yml", Bot.jda.getSelfUser().getAvatarUrl());
            other.addBlankField(false);
            for (String key : Config.config.getGroups().keySet()) {
                StringBuilder ids = new StringBuilder();
                ids.append("Group IDs").append(System.lineSeparator());
                for (String zz : Config.config.getGroups().get(key).id)
                    ids.append("- `").append(zz).append("`").append(System.lineSeparator());
                other.addField(key,ids.toString(), true);
                StringBuilder perms = new StringBuilder();
                perms.append("Permissions").append(System.lineSeparator());
                for (String zz : Config.config.getGroups().get(key).permissions)
                    perms.append("- `").append(zz).append("`").append(System.lineSeparator());
                if (Config.config.getGroups().get(key).admin) {
                    other.addField("Type: SuperUser Group", perms.toString(), true);
                } else {
                    other.addField("Type: User Group", perms.toString(), true);
                }
                other.addField("Power: " + Config.config.getGroups().get(key).power, "\u200B", true);
            }
            other.addBlankField(false);
            for (String key : Config.config.getUsers().keySet()) {
                other.addField(key, "User ID" + System.lineSeparator() + "`" + Config.config.getUsers().get(key).id + "`", true);
                StringBuilder perms = new StringBuilder();
                perms.append("Permissions").append(System.lineSeparator());
                HashSet<String> thing = new HashSet<>();
                thing.addAll(Config.config.getUsers().get(key).permissions);
                for (String zz : thing)
                    perms.append("- `").append(zz).append("`").append(System.lineSeparator());
                if (Config.config.getUsers().get(key).admin) {
                    other.addField("Type: SuperUser", perms.toString(), true);
                } else {
                    other.addField("Type: User", perms.toString(), true);
                }
                other.addField("Power: " + Config.config.getUsers().get(key).power, "\u200B", true);
            }
            other.setFooter("Config.yml", Bot.jda.getSelfUser().getAvatarUrl());
            channel.sendMessage(other.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
        }
    };
    public static BotCommand pullconfig = new BotCommand("command.pullconfig"){
        @Override
        void help(){
            this.helpname = "Pull Config";
            this.delpusage = "::pullconfig";
            this.helpdesc = "Pull the latest configuration from the Config.yml file.";
            this.skip = false;
        }
        @Override
        void command(){
            Config.loadConfig();
            WHITELIST = Config.getWhitelist();
            channel.addReactionById(message.getId(), "👍").queue();
        }
    };
    public static BotCommand kill = new BotCommand("command.kill"){
        @Override
        void help(){
            this.helpname = "Kill";
            this.delpusage = "::kill";
            this.helpdesc = "Kill the bot and return the host machine to the command line/desktop.";
            this.skip = false;
        }
        @Override
        void command(){
            if(channel.getType().equals(ChannelType.TEXT)) message.delete().queue();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            }catch (InterruptedException eeee){
                eeee.getMessage();
            }
            System.exit(1);
        }
    };
    public static BotCommand restart = new BotCommand("command.restart"){
        @Override
        void cleanup(boolean delete){
        }
        @Override
        void help(){
            this.helpname = "Restart";
            this.delpusage = "::restart";
            this.helpdesc = "Restart the bot, re-initialise everything.";
            this.skip = false;
        }
        @Override
        void command(){
            if(channel.getType().equals(ChannelType.TEXT)) message.delete().queue();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            }catch (InterruptedException eeee){
                eeee.getMessage();
            }
            Bot.restart();
        }
    };
    public static BotCommand c = new BotCommand("command.c"){
        @Override
        void help(){
            this.helpname = "Clan Bot Command";
            this.delpusage = "::c (command)";
            this.helpdesc = "Send a command to the RuneScape clan-sync bot and return the result.";
            this.skip = false;
        }
        @Override
        void command() {
            lastchannel = channel;
            waiting = true;
            Bot.jda.getUserById("320611242366730241").openPrivateChannel().complete().sendMessage(commandargs).queue();
        }
    };

    public static BotCommand watch = new BotCommand("command.watch"){
        @Override
        void help(){
            this.helpname = "Watch";
            this.delpusage = "::watch (add/del/list) <keyword>";
            this.helpdesc = "Have the bot \"watch\" for certain keywords in chat, and log any occurrences to a channel called #logs\nKeywords are not case-sensitive";
            this.skip = false;
            this.savemem = true;
        }
        @Override
        void command(){
            if(!commandargs.equals("")) {
                Scanner read = new Scanner(commandargs);
                if(read.hasNext()){
                    switch(read.next()){
                        default:
                            message.getChannel().sendMessage("<:Watch:326815513550389249> `Invalid Syntax` `"+commandargs+"`").queue(msg->msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            break;
                        case"add":
                            if(read.hasNext()){
                                String keyword = read.next().toLowerCase();
                                if(!memory.contains(keyword)){
                                    memory.add(keyword);
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `"+keyword+" has been added to the watch filter. type ::watch del "+keyword+" to remove it.`").queue(msg->msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                } else {
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `I am already watching for "+keyword+".`").queue(msg->msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                }
                            } else {
                                message.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify a keyword!` `"+delpusage+"`").queue(msg->msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            }
                            break;
                        case"del":
                            if(read.hasNext()){
                                String keyword = read.next().toLowerCase();
                                if(memory.contains(keyword)){
                                    memory.remove(keyword);
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `"+keyword+" has been removed from the watch filter.`").queue(msg->msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                } else {
                                    message.getChannel().sendMessage("<:Watch:326815513550389249> `I am not currently watching for "+keyword+". Do ::watch add "+keyword+" to add it to the list.`").queue(msg->msg.delete().queueAfter(30, TimeUnit.SECONDS));
                                }
                            } else {
                                message.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify a keyword!` `"+delpusage+"`").queue(msg->msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            }
                            break;
                        case"list":
                            StringBuilder keywords = new StringBuilder();
                            for(String s:memory){
                                keywords.append(s).append(", ");
                            }
                            keywords.deleteCharAt(keywords.length()-1).deleteCharAt(keywords.length()-1);
                            message.getChannel().sendMessage("<:Watch:326815513550389249> `Right now I'm watching for "+keywords+"`").queue(msg->msg.delete().queueAfter(30, TimeUnit.SECONDS));
                            break;
                    }
                }
            } else {
                message.getChannel().sendMessage("<:Watch:326815513550389249> `You must specify an action and a keyword!` `"+delpusage+"`").queue(msg->msg.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
    };

    public static BotCommand clan = new BotCommand("command.clan"){
        @Override
        void help(){
            this.helpname = "RuneScape Clan Ranks";
            this.delpusage = "::clan";
            this.helpdesc = "(for now) Pull the upper ranks of the RuneScape clan Zamorak Cult, and match any names with those on the current discord guild.";
            this.skip = false;
        }
        @Override
        void command(){
            RuneScapeAPI api = RuneScapeAPI.createHttp();
            Hiscores hiscores = api.hiscores();

            String Clan = "Zamorak Cult";
            try{
                java.util.List<ClanMate> clanMates = hiscores.clanInformation(Clan);
                EmbedBuilder Ranks = new EmbedBuilder();
                StringBuilder One = new StringBuilder();
                if(guild.getId().equals("254861442799370240")&&Clan.equals("Zamorak Cult"))One.append(guild.getRoleById(254883837757227008L).getAsMention()).append(System.lineSeparator());
                StringBuilder Two = new StringBuilder();
                if(guild.getId().equals("254861442799370240")&&Clan.equals("Zamorak Cult"))Two.append(guild.getRoleById(268490396617801729L).getAsMention()).append(System.lineSeparator());
                StringBuilder Three = new StringBuilder();
                if(guild.getId().equals("254861442799370240")&&Clan.equals("Zamorak Cult"))Three.append(guild.getRoleById(258350529229357057L).getAsMention()).append(System.lineSeparator());
                StringBuilder Four = new StringBuilder();
                if(guild.getId().equals("254861442799370240")&&Clan.equals("Zamorak Cult"))Four.append(guild.getRoleById(254881136524656640L).getAsMention()).append(System.lineSeparator());
                StringBuilder Five = new StringBuilder();
                StringBuilder Six = new StringBuilder();
                int admins = StringUtils.countMatches(clanMates.toString(),"Admin")/2+1;
                int admincount = 0;
                for(ClanMate a : clanMates){
                    boolean found = false;
                    switch (a.getRank()){
                        case "Owner":
                            for(Member b:guild.getMembers()){
                                if(comapare(b.getEffectiveName(),a.getName())){
                                    if(!One.toString().contains(b.getAsMention())) One.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if(!found) One.append(a.getName());
                            One.append(System.lineSeparator());
                            break;
                        case "Deputy Owner":
                            for(Member b:guild.getMembers()){
                                if(comapare(b.getEffectiveName(),a.getName())){
                                    if(!Two.toString().contains(b.getAsMention())) Two.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if(!found) Two.append(a.getName());
                            Two.append(System.lineSeparator());
                            break;
                        case "Overseer":
                            for(Member b:guild.getMembers()){
                                if(comapare(b.getEffectiveName(),a.getName())){
                                    if(!Three.toString().contains(b.getAsMention())) Three.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if(!found) Three.append(a.getName());
                            Three.append(System.lineSeparator());
                            break;
                        case "Coordinator":
                            for(Member b:guild.getMembers()){
                                if(comapare(b.getEffectiveName(),a.getName())){
                                    if(!Four.toString().contains(b.getAsMention())) Four.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if(!found) Four.append(a.getName());
                            Four.append(System.lineSeparator());
                            break;
                        case "Admin":
                            admincount++;
                            for(Member b:guild.getMembers()){
                                if(comapare(b.getEffectiveName(),a.getName())){
                                    if(!Five.toString().contains(b.getAsMention())&&!Six.toString().contains(b.getAsMention()))
                                        if(admincount<=admins)
                                            Five.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    if(admincount>admins)
                                        Six.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
                                    found = true;
                                    break;
                                }
                            }
                            if(admincount<=admins)
                                if(!found) Five.append(a.getName());
                            Five.append(System.lineSeparator());
                            if(admincount>admins)
                                if(!found) Six.append(a.getName());
                            Six.append(System.lineSeparator());
                            break;
                    }
                }
                Ranks.addField("Owner",One.toString(),false);
                Ranks.addField("Deputy Owner",Two.toString(),true);
                Ranks.addField("Overseer",Three.toString(),true);
                Ranks.addField("Coordinator",Four.toString(),false);
                Ranks.addField("Admin",Five.toString(),true);
                if(admincount>admins) Ranks.addField("\u200B",Six.toString(),true);
                Ranks.setColor(new Color(148,168,249));
                Ranks.setDescription("The bot has matched the accounts with it's best guess of what their discord tag might be. There is still a significant margin for error, so let me know if something goes wrong, or something is omitted that should not be. However if you're running the command for a clan other than that which owns the discord server, things will be matched wrong.");
                String Time = new SimpleDateFormat("MM/dd/YYYY hh:mma zzz").format(new Date());
                Ranks.setFooter("Generated "+Time+" For "+Clan,Bot.jda.getSelfUser().getAvatarUrl());
                channel.sendMessage(Ranks.build()).queue( msg -> msg.delete().queueAfter(2,TimeUnit.MINUTES));
            }catch(java.lang.NoClassDefFoundError|IOException eeeee){
                System.out.println(eeeee.getMessage());
                eeeee.printStackTrace();
            }

        }
    };
    public static BotCommand alog = new BotCommand("command.alog#all"){
        @Override
        void help(){
            this.helpname = "RuneScape Adventurer's Log";
            this.delpusage = "::alog (Runescape Name)";
            this.helpdesc = "Fetch the RuneScape adventurer's log for the specified player name.";
            this.skip = false;
        }
        @Override
        void command() {
            StringBuilder name= new StringBuilder();
            try {
                Scanner rsn = new Scanner(commandargs);
                if(rsn.hasNext()){
                    name = new StringBuilder(rsn.next());
                    while(rsn.hasNext()){
                        name.append("+").append(rsn.next());
                    }
                } else {
                    message.getChannel().sendMessage("<:Watch:326815513550389249> `"+user.getName()+", you need to enter a name! ::alog NAME`").queue(msg->msg.delete().queueAfter(10, TimeUnit.SECONDS));
                    return;
                }
                URL url = new URL("http://services.runescape.com/m=adventurers-log/c=tB0ermS1flc/rssfeed?searchName="+name);
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(url));
                StringBuilder test = new StringBuilder();
                List entries = feed.getEntries();
                Iterator it = entries.iterator();
                test.append("Adventurer's Log for ").append(name.toString().replace("+", " ")).append("\n```\n");
                while (it.hasNext()) {
                    SyndEntry entry = (SyndEntry)it.next();
                    SyndContent description = entry.getDescription();
                    test.append(description.getValue().trim().replace("my  ","my ").replace("   called:  "," called: ").replace("   in Daemonheim."," in Daemonheim.").replace("  in Daemonheim"," in Daemonheim").replace(" , ",", ")).append("\n");
                }
                test.append("```");
                channel.sendMessage(test.toString()).queue(msg->msg.delete().queueAfter(10,TimeUnit.MINUTES));
            } catch (IOException | FeedException e) {
                message.getChannel().sendMessage("<:Watch:326815513550389249> `"+user.getName()+", the name you've entered is invalid! ("+ name.toString().replace("+"," ")+")`").queue(msg->msg.delete().queueAfter(10, TimeUnit.SECONDS));
                this.Lastrun = LocalDateTime.now().minusSeconds(getratelimit());
            }
        }
    };

    public static BotCommand maintain = new BotCommand("command.m"){
        @Override
        void command(){
            if(guild.getId().equals("319606739550863360")){
                Role maintain[] = new Role[]{guild.getRoleById("319606870606217217")};
                if(guild.getMemberById(user.getId()).getRoles().contains(guild.getRoleById("319606870606217217"))){
                    guild.getController().removeRolesFromMember(guild.getMemberById(user.getId()), Arrays.asList(maintain)).queue();
                } else {
                    guild.getController().addRolesToMember(guild.getMemberById(user.getId()),Arrays.asList(maintain)).queue();
                }
            }
        }
    };
    public static BotCommand verify = new BotCommand("command.v"){
        @Override
        void command(){
            if(guild.getId().equals("319606739550863360")){
                GuildController cont = guild.getController();
                Scanner watch = new Scanner(message.getRawContent());
                String mention = watch.next();
                for(Member m:guild.getMembers()){
                    if(mention.equals(m.getAsMention())){
                        if(m.equals(guild.getMember(user))||m.isOwner()||m.getUser().isBot()){
                            break;
                        }
                        if(m.getRoles().contains(guild.getRoleById("320300565789802497"))){
                            cont.removeRolesFromMember(m,guild.getRoleById("320300565789802497")).queue();
                            cont.setNickname(m,"").queue();
                            channel.sendMessage("`"+m.getUser().getName()+" has been un-verified by "+guild.getMember(user).getEffectiveName()+".`").queue(msg->msg.delete().queueAfter(10,TimeUnit.SECONDS));
                        } else {
                            StringBuilder name = new StringBuilder(m.getEffectiveName());
                            if(watch.hasNext()){
                                name = new StringBuilder(watch.next());
                                while(watch.hasNext()){
                                    name.append(" ").append(watch.next());
                                }
                            }
                            cont.setNickname(m, name.toString()).queue();
                            cont.addRolesToMember(m,guild.getRoleById("320300565789802497")).queue();
                            channel.sendMessage("`"+m.getEffectiveName()+" has been verified by "+guild.getMember(user).getEffectiveName()+" as "+name+".`").queue(msg->msg.delete().queueAfter(10,TimeUnit.SECONDS));
                        }
                    }
                }
            }
        }
    };

    // SPECIAL COMMANDS

}
