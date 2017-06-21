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
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.GuildController;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
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
    }

    private final String node;
    private final Long ratelimit;

    long getratelimit(){
        return this.ratelimit;
    }

    protected Message message;
    protected Guild guild;
    protected MessageChannel channel;
    protected User user;
    protected String commandargs;
    protected BotUser botUser;

    protected LocalDateTime Lastrun = LocalDateTime.now().minusYears(10L);

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
    }
    void command(){
        message.getChannel().sendMessage("<:WatchWarn:326815513634406419> `This command has not been configured, node: "+this.node+"`").queue(msg->msg.delete().queueAfter(10, TimeUnit.SECONDS));
    }

}

class BotCommands {

    private static boolean comapare(String a,String b) {
        Levenshtein l = new Levenshtein();
        JaroWinkler jw = new JaroWinkler();
        return a.contains(b) || b.contains(a) || a.equalsIgnoreCase(b) || l.distance(a, b) < 2 || jw.similarity(a, b) > 0.89d;
    }

    // LITERAL COMMANDS

    public static BotCommand id = new BotCommand("command.id"){
        @Override
        void command(){
            channel.sendMessage(user.getAsMention()+", Your ID is "+user+".").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            channel.sendMessage(botUser.getPermissions().toString()).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
            channel.sendMessage(botUser.isadmin()+"").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    };
    public static BotCommand clan = new BotCommand("command.clan"){
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
    public static BotCommand roles = new BotCommand("command.roles"){
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
    public static BotCommand showconfig = new BotCommand("NOPERM"){
        @Override
        void command(){
            EmbedBuilder other = new EmbedBuilder();
            StringBuilder whitelisted = new StringBuilder();
            other.setColor(new Color(148,168,249));
            for (String a : WHITELIST)
                whitelisted.append("- `").append(a).append("`").append(System.lineSeparator());
            other.addField("Whitelist", whitelisted.toString(), false);
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
    public static BotCommand pullconfig = new BotCommand("NOPERM"){
        @Override
        void command(){
            Config.loadConfig();
            WHITELIST = Config.getWhitelist();
            channel.addReactionById(message.getId(), "👍").queue();
        }
    };
    public static BotCommand kill = new BotCommand("NOPERM"){
        @Override
        void command(){
            System.exit(1);
        }
    };
    public static BotCommand restart = new BotCommand("NOPERM"){
        @Override
        void command(){
            Bot.restart();
        }
    };
    public static BotCommand alog = new BotCommand("command.alog#all"){
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

    // SPECIAL COMMANDS

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

}
