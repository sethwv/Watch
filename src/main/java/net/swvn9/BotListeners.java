package net.swvn9;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

class ReadyListener implements net.dv8tion.jda.core.hooks.EventListener {
	@Override
	public void onEvent(net.dv8tion.jda.core.events.Event event)
	{
		if(event instanceof ReadyEvent){

			for(Guild a : event.getJDA().getGuilds()){
				switch(a.getId()){
					default:
						EventListener.logger(EventListener.logPrefix(0) + "I'm in "+a.getName()+"! ID:"+a.getId());
						break;
					case "123527831664852992":
						event.getJDA().getPresence().setStatus(OnlineStatus.INVISIBLE);
						EventListener.logger(EventListener.logPrefix(0) + "I'm in seb's server!");
						break;
					case "243112682142695446":
						event.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
						event.getJDA().getPresence().setGame(Game.of("in Dev Mode"));
						EventListener.logger(EventListener.logPrefix(0) + "I'm in the test server!");
						break;
				}
			}
		}
	}
}

class EventListener extends ListenerAdapter {

	private static final String SETH = "111592329424470016";
    private static final String LITERAL = "!";
	private static final String DEVLITERAL = "::";
    private static final Color blurple = new Color(148,168,249);
	private static String WHITELIST[] = Config.getWhitelist();
	private static String ADMINROLES[] = Config.getAdminRoles();
	private static final String LOGTIME = new SimpleDateFormat("MMMDDYY_HHmmss").format(new Date());
	private static final String EMPTYCACHE[] = {"","","",""};
	private static final int CACHES[] = {3};
	private static final String[] BOTCACHE = EMPTYCACHE;
    private static FileWriter log;
    private static Guild Home;

	//check if a specific channel ID is on the whitelist
    private boolean channelWhitelisted (String channelID){
        for(String value : WHITELIST){ //for each value in the whitelist array
            if(value.equalsIgnoreCase(channelID)) return true; //if the id is whitelisted, return true.
        }
    return false; //if it isn't in the whitelist, return false
    }

	private boolean isAdmin (Guild g,User u){
		for(String e:ADMINROLES){
			if(g.getMemberById(u.getId()).getRoles().toString().contains(e)) return true;
		}
    	return false;
	}
	private boolean isSeth (User u){
		return u.getId().equalsIgnoreCase(SETH);
	}

	//create a prefix for any log entries
	static String logPrefix(int type){
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date()); //ge tthe current timestamp
		String logType; //prepare the log type string
		switch(type) {
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
		}
		return "["+timeStamp+"] ["+logType+"] [Log]: "; //create and return the log prefix
	}

	@SuppressWarnings("unused")
	static void editCache(String MessageID){
		if(BOTCACHE[0].contains(MessageID)){
			EventListener.BOTCACHE[0] = BOTCACHE[0].replace(MessageID+",","");
			logger(logPrefix(4)+"Removed "+MessageID+" from cache row 0");
		} else {
			EventListener.BOTCACHE[0] = BOTCACHE[0]+MessageID+",";
			logger(logPrefix(4)+"Added "+MessageID+" to cache row 0");
		}
	}
	private static void editCache(String MessageID,int cache){
		boolean cacheExists = false;
		for(int a:CACHES){
			if(a>=cache) cacheExists = true;
		}
		if(cacheExists){
			if(BOTCACHE[cache].contains(MessageID)||BOTCACHE[0].contains(MessageID)){
				EventListener.BOTCACHE[0] = BOTCACHE[0].replace(MessageID+",","");
				EventListener.BOTCACHE[cache] = BOTCACHE[cache].replace(MessageID+",","");
				logger(logPrefix(4)+"Removed "+MessageID+" from cache rows 0 and "+cache);
			} else {
				EventListener.BOTCACHE[0] = BOTCACHE[0]+MessageID+",";
				EventListener.BOTCACHE[cache] = BOTCACHE[cache]+MessageID+",";
				logger(logPrefix(4)+"Added "+MessageID+" to cache rows 0 and "+cache);
			}
		} else {
			logger(logPrefix(1)+"Cache row "+cache+" Not found in current instance.");
		}
	}
	static void logger(String input){
		try {
			EventListener.log =new FileWriter("Logs"+File.separator+"LOG_"+LOGTIME+".txt",true);
			log.write(input+System.lineSeparator());
			log.close();
			System.out.println(input);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private static boolean isCommand(Message m){
		for(int i = 0;i<LITERAL.length();i++){
			if(m.getContent().charAt(i)!=LITERAL.charAt(i)){
				return false;
			}
		}
		return true;
	}
	private static boolean isDevCommand(Message m){
		for(int i = 0;i<DEVLITERAL.length();i++){
			if(m.getContent().charAt(i)!=DEVLITERAL.charAt(i)){
				return false;
			}
		}
		return true;
	}

    @Override //any reaction that is added that the bot can see
    public void onMessageReactionAdd(MessageReactionAddEvent e){
	    if(Home==null) EventListener.Home= e.getGuild();
	    if((e.getUser().isBot()||e.getUser().getAsMention().equals(Bot.jda.getSelfUser().getAsMention())||!channelWhitelisted(e.getChannel().getId()+""))&&e.getChannelType().isGuild()){
		    return;
	    }
		if(e.isFromType(ChannelType.TEXT)) logger(logPrefix(3)+"("+e.getGuild().getName()+", #"+e.getTextChannel().getName()+") "+e.getUser().getName()+" Added "+e.getReaction().getEmote().getName()+" to "+e.getMessageId());
		if(e.isFromType(ChannelType.PRIVATE)&&!e.getUser().isBot()) logger(logPrefix(3)+"(Private Message) "+e.getUser().getName()+" Added "+e.getReaction().getEmote().getName()+" to "+e.getMessageId());
		if((e.getChannelType()!= ChannelType.TEXT||channelWhitelisted(e.getChannel().getId()+""))&&!e.getUser().isBot()&&BOTCACHE[0].contains(e.getMessageId())){
			if(BOTCACHE[1].contains(e.getMessageId())){
				switch(e.getReaction().getEmote().getName()){
					case "SeThink":
						e.getChannel().editMessageById(e.getMessageId(),e.getMember().getAsMention()+" added a "+e.getReactionEmote().getName()+" reaction").queue();
						editCache(e.getMessageId(),1);
						if(e.getChannelType().equals(ChannelType.TEXT))e.getTextChannel().clearReactionsById(e.getMessageIdLong()).queue();
						break;
					case "\uD83D\uDD27":
						e.getChannel().editMessageById(e.getMessageId(),e.getMember().getAsMention()+" added a "+e.getReactionEmote().getName()+" reaction").queue();
						editCache(e.getMessageId(),1);
						if(e.getChannelType().equals(ChannelType.TEXT))e.getTextChannel().clearReactionsById(e.getMessageIdLong()).queue();
						break;
					default:
						e.getChannel().editMessageById(e.getMessageId(),e.getMember().getAsMention()+" added a "+e.getReactionEmote().getName()+" reaction").queue();
						editCache(e.getMessageId(),1);
						if(e.getChannelType().equals(ChannelType.TEXT))e.getTextChannel().clearReactionsById(e.getMessageIdLong()).queue();
						break;
				}
			}
			if(BOTCACHE[2].contains(e.getMessageId())&&(isAdmin(e.getGuild(),e.getUser())||isSeth(e.getUser()))){
				switch(e.getReaction().getEmote().getName()){
					case "❌":
						editCache(e.getMessageId(),2);
						for(int i=0;i<BOTCACHE.length;i++) EventListener.BOTCACHE[i]="";
						logger(logPrefix(0)+"The cache has been cleared and re-initialised.");
						e.getChannel().sendMessage("*The Cache has been cleared by "+e.getMember().getAsMention()+".*").queue(msg -> msg.delete().queueAfter(10,TimeUnit.SECONDS));
						if(e.getChannelType().equals(ChannelType.TEXT))e.getTextChannel().clearReactionsById(e.getMessageId()).queue();
						e.getChannel().deleteMessageById(e.getMessageId()).queueAfter(10, TimeUnit.SECONDS);
						break;
				}
			}
			if(BOTCACHE[3].contains(e.getMessageId())&&(isAdmin(e.getGuild(),e.getUser())||isSeth(e.getUser()))){
				switch(e.getReaction().getEmote().getName()){
					case "❌":
						e.getChannel().deleteMessageById(e.getMessageId()).queue();
						editCache(e.getMessageId(),3);
						break;
					case "\uD83C\uDD70":
						e.getChannel().deleteMessageById(e.getMessageId()).complete();
						Bot.jda.getPresence().setStatus(OnlineStatus.INVISIBLE);
						Bot.jda.shutdown(false);
						break;
					case "\uD83C\uDD71":
						editCache(e.getMessageId(),3);
						e.getChannel().deleteMessageById(e.getMessageId()).complete();
						Bot.jda.removeEventListener(Bot.jda.getRegisteredListeners().get(0));
						Bot.restart();
						break;
				}
			}
		}
    }

    @Override //any message sent that the bot can see
    public void onMessageReceived(MessageReceivedEvent e) {
    	if(Home==null) EventListener.Home= e.getGuild();
	    if((e.getAuthor().isBot()||
		        e.getAuthor().getAsMention().equals(Bot.jda.getSelfUser().getAsMention())||
		        (!channelWhitelisted(e.getChannel().getId()+"")&&!e.getChannelType().equals(ChannelType.PRIVATE)))){
            return;
        }
        String input = e.getMessage().getRawContent();
        if(e.isFromType(ChannelType.TEXT)) logger(logPrefix(2)+"("+e.getGuild().getName()+", #"+e.getTextChannel().getName()+") "+e.getAuthor().getName()+": "+input);
	    if(e.isFromType(ChannelType.PRIVATE)) logger(logPrefix(2)+"(Private Message) "+e.getAuthor().getName()+": "+input);

	    if(isCommand(e.getMessage())){
			input=input.replaceFirst(LITERAL,"");
			Scanner command = new Scanner(input);
			if(command.hasNext()){
				switch(command.next().toLowerCase()){
					default:
						break;
					case "id":
						e.getChannel().sendMessage(e.getAuthor().getAsMention()+", Your ID is "+e.getAuthor()+".").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
						if(e.getChannelType().isGuild()) e.getMessage().delete().queue();
						break;
				}
			}
		}

		if((isDevCommand(e.getMessage()))){
	    	if(e.getChannelType().equals(ChannelType.PRIVATE)||isAdmin(e.getGuild(),e.getAuthor())||isSeth(e.getAuthor())){
			input=input.replaceFirst(DEVLITERAL,"");
			Scanner command = new Scanner(input);
			if(command.hasNext()) switch (command.next().toLowerCase()) {
				case "roles":
					if (e.getChannelType().isGuild()) e.getMessage().delete().queue();
					EmbedBuilder roles = new EmbedBuilder();
					roles.setFooter("Roles for " + e.getGuild().getName(), Bot.jda.getSelfUser().getAvatarUrl());
					roles.setThumbnail(e.getGuild().getIconUrl());
					for (Object s : e.getGuild().getRoles().toArray()) {
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
					roles.setColor(blurple);
					e.getChannel().sendMessage(roles.build()).queue();
					break;
				case "react":
					e.getChannel().sendMessage("Add, or click a reaction.").queue(msg -> {
						//msg.addReaction(Home.getEmoteById(310928672389464064L)).queue();
						msg.addReaction("\uD83D\uDD27").queue();
						editCache(msg.getId(), 1);
					});
					if (e.getChannelType().isGuild()) e.getMessage().delete().queue();
					break;
				case "embed":
					EmbedBuilder testEmbed = new EmbedBuilder();
					testEmbed.setColor(Color.GRAY);
					testEmbed.setFooter("Test Rich Embed", Bot.jda.getSelfUser().getAvatarUrl());
					testEmbed.setImage("http://u.swvn9.net/2017/5jXhJ.jpg");
					e.getChannel().sendMessage(testEmbed.build()).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
					if (e.getChannelType().isGuild()) e.getMessage().delete().queue();
					break;
				case "cache":
					EmbedBuilder cache = new EmbedBuilder();
					cache.setFooter("Instance Cache. Admins can react with ❌ to clear the cache.", Bot.jda.getSelfUser().getAvatarUrl());
					for (int i = 0; i < BOTCACHE.length; i++) {
						cache.addField("Cache Layer " + i, "```\n" + BOTCACHE[i].replace(",", " ") + "\n```", false);
					}
					cache.setColor(blurple);
					//cache.setDescription("If you're an admin, you can react with the ❌ emoji to clear the message cache.");
					e.getChannel().sendMessage(cache.build()).queue(msg -> {
						//msg.addReaction("❌").queue();
						editCache(msg.getId(), 2);
					});
					if (e.getChannelType().isGuild()) e.getMessage().delete().queue();
					break;
				case "config":
					EmbedBuilder other = new EmbedBuilder();
					StringBuilder whitelisted = new StringBuilder();
					other.setColor(blurple);
					for (String a : WHITELIST)
						whitelisted.append("- `").append(a).append("`").append(System.lineSeparator());
					other.addField("Whitelist", whitelisted.toString(), false);
					other.setFooter("Settings + Whitelist from Config.yml", Bot.jda.getSelfUser().getAvatarUrl());

					//EmbedBuilder groups = new EmbedBuilder();
					//groups.setColor(blurple);
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
						if (Config.config.getGroups().get(key).isadmin) {
							other.addField("Type: SuperUser Group", perms.toString(), true);
						} else {
							other.addField("Type: User Group", perms.toString(), true);
						}
						other.addField("Power: " + Config.config.getGroups().get(key).power, "\u200B", true);
					}
					//groups.setFooter("Groups from Config.yml", Bot.jda.getSelfUser().getAvatarUrl());
					//e.getChannel().sendMessage(groups.build()).queue( msg -> msg.delete().queueAfter(1,TimeUnit.MINUTES));

					//EmbedBuilder user = new EmbedBuilder();
					//user.setColor(blurple);
					other.addBlankField(false);
					for (String key : Config.config.getUsers().keySet()) {
						other.addField(key, "User ID" + System.lineSeparator() + "`" + Config.config.getUsers().get(key).id + "`", true);
						StringBuilder perms = new StringBuilder();
						perms.append("Permissions").append(System.lineSeparator());
						for (String zz : Config.config.getUsers().get(key).permissions)
							perms.append("- `").append(zz).append("`").append(System.lineSeparator());
						if (Config.config.getUsers().get(key).isadmin) {
							other.addField("Type: SuperUser", perms.toString(), true);
						} else {
							other.addField("Type: User", perms.toString(), true);
						}
						other.addField("Power: " + Config.config.getUsers().get(key).power, "\u200B", true);
					}
					other.setFooter("Config.yml", Bot.jda.getSelfUser().getAvatarUrl());
					e.getChannel().sendMessage(other.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
					if (e.getChannelType().isGuild()) e.getMessage().delete().queue();
					break;
				case "pullconfig":
					Config.loadConfig();
					EventListener.ADMINROLES = Config.getAdminRoles();
					EventListener.WHITELIST = Config.getWhitelist();
					e.getChannel().addReactionById(e.getMessageId(), "👍").queue();
					if (e.getChannelType().isGuild()) e.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
					break;
				case "kill":
					if (e.getChannelType().isGuild()) e.getMessage().delete().complete();
					EmbedBuilder kill = new EmbedBuilder();
					kill.setColor(blurple);
					if (command.hasNext()) {
						switch (command.next().toLowerCase()) {
							case "k":
								Bot.jda.getPresence().setStatus(OnlineStatus.INVISIBLE);
								Bot.jda.shutdown(false);
								break;
							case "r":
								Bot.jda.removeEventListener(Bot.jda.getRegisteredListeners().get(0));
								Bot.restart();
								break;
							default:
								kill.addField("Are you sure you want to kill the bot?", "`❌` to cancel.\n`\uD83C\uDD70` to **kill** the bot.\n`\uD83C\uDD71` to **restart** the bot.", false);
								e.getChannel().sendMessage(kill.build()).queue(msg -> {
									msg.addReaction("❌").queue();
									msg.addReaction("\uD83C\uDD70").queue();
									msg.addReaction("\uD83C\uDD71").queue();
									editCache(msg.getId(), 3);
								});
								break;
						}
					} else {
						kill.addField("Are you sure you want to kill the bot?", "`❌` to cancel.\n`\uD83C\uDD70` to **kill** the bot.\n`\uD83C\uDD71` to **restart** the bot.", false);
						e.getChannel().sendMessage(kill.build()).queue(msg -> {
							msg.addReaction("❌").queue();
							msg.addReaction("\uD83C\uDD70").queue();
							msg.addReaction("\uD83C\uDD71").queue();
							editCache(msg.getId(), 3);
						});
					}
					break;
			}
			}
		}
		if(!e.getChannelType().equals(ChannelType.PRIVATE)&&e.getGuild().getId().equalsIgnoreCase("123527831664852992")){
			if(!isCommand(e.getMessage())&&!isDevCommand(e.getMessage())){
				String newmessage = e.getMessage().getContent().toLowerCase();
				if(newmessage.contains("can")&&newmessage.contains("i")&&newmessage.contains("come")&&newmessage.contains("over")){
					if(e.getAuthor().getId().equalsIgnoreCase("111592329424470016")||e.getAuthor().getId().equalsIgnoreCase("95670775389880320")){
						e.getChannel().sendMessage("You already live here, "+e.getAuthor().getAsMention()).queue(msg -> e.getMessage().delete().queueAfter(45,TimeUnit.SECONDS));
					} else {
						e.getChannel().sendMessage("Absolutely the fuck not, "+e.getAuthor().getAsMention()).queue(msg -> e.getMessage().delete().queueAfter(45,TimeUnit.SECONDS));
					}
				}
			}
		}
    }

}
