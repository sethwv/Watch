package net.swvn9;

import com.mikebull94.rsapi.hiscores.ClanMate;
import com.mikebull94.rsapi.hiscores.Hiscores;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;
import net.dv8tion.jda.client.events.group.GroupUserJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import com.mikebull94.rsapi.*;
import net.dv8tion.jda.core.managers.GuildController;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;
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
					case "254861442799370240":
						EventListener.logger(EventListener.logPrefix(0) + "I'm in the Zamorak Cult public server");
						MyBackgroudMethod thread = new MyBackgroudMethod();
						thread.setDaemon(true);
						thread.start();

						java.awt.EventQueue.invokeLater(new Runnable() {
							public void run() {
								EventListener.clanRanks(a);
							}
						});
						break;
				}
			}

		}
	}
	public static class MyBackgroudMethod extends Thread {

		@Override
		public void run() {
			while (true) {
				EventListener.logger(EventListener.logPrefix(0) + "Updated Zamorak Cult clan ranks.");
				try {
					Thread.sleep(7200000);
				} catch (InterruptedException e) {
					e.printStackTrace();
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
	private static RuneScapeAPI api = RuneScapeAPI.createHttp();
	private static Hiscores hiscores = api.hiscores();

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
			case 6:
				logType = "Perm";
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
	private static boolean comapare(String a,String b) {
		Levenshtein l = new Levenshtein();
		JaroWinkler jw = new JaroWinkler();
		return a.contains(b) || b.contains(a) || a.equalsIgnoreCase(b) || l.distance(a, b) < 2 || jw.similarity(a, b) > 0.89d;
	}
	static void clanRanks(Guild g){
		String Clan = "Zamorak Cult";
		try{
			java.util.List<ClanMate> clanMates = hiscores.clanInformation(Clan);
			//EmbedBuilder Ranks = new EmbedBuilder();
			StringBuilder One = new StringBuilder();
			StringBuilder Two = new StringBuilder();
			StringBuilder Three = new StringBuilder();
			StringBuilder Four = new StringBuilder();
			StringBuilder Five = new StringBuilder();
			//StringBuilder Six = new StringBuilder();
			GuildController controller = g.getController();
			for(ClanMate a : clanMates){
				boolean found = false;
				String roleIds[] = new String[]{
						"320252998867615765","320252946422038530","320258202069499914","320252736035880960","320253083474984961"
				};
				switch (a.getRank()){
					case "Owner":
						for(Member b:g.getMembers()){
							if(comapare(b.getEffectiveName(),a.getName())){
								for(Role v:b.getRoles()){
									for(String c:roleIds){
										if(v.getId().equals(c)&&!v.getId().equals("320252998867615765")) b.getRoles().remove(b.getGuild().getRoleById(c));
									}
								}
								if(!b.getRoles().contains(b.getGuild().getRoleById("320252998867615765")))controller.addRolesToMember(b,b.getGuild().getRoleById("320252998867615765")).queue();
								if(!One.toString().contains(b.getAsMention())) One.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
								found = true;
								break;
							}
						}
						if(!found) One.append(a.getName());
						One.append(System.lineSeparator());
						break;
					case "Deputy Owner":
						for(Member b:g.getMembers()){
							if(comapare(b.getEffectiveName(),a.getName())){
								for(Role v:b.getRoles()){
									for(String c:roleIds){
										if(v.getId().equals(c)&&!v.getId().equals("320252946422038530")) b.getRoles().remove(b.getGuild().getRoleById(c));
									}
								}
								controller.addRolesToMember(b,b.getGuild().getRoleById("320252946422038530")).queue();
								if(!Two.toString().contains(b.getAsMention())) Two.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
								found = true;
								break;
							}
						}
						if(!found) Two.append(a.getName());
						Two.append(System.lineSeparator());
						break;
					case "Overseer":
						for(Member b:g.getMembers()){
							if(comapare(b.getEffectiveName(),a.getName())){
								for(Role v:b.getRoles()){
									for(String c:roleIds){
										if(v.getId().equals(c)&&!v.getId().equals("320258202069499914")) b.getRoles().remove(b.getGuild().getRoleById(c));
									}
								}
								controller.addRolesToMember(b,b.getGuild().getRoleById("320258202069499914")).queue();
								if(!Three.toString().contains(b.getAsMention())) Three.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
								found = true;
								break;
							}
						}
						if(!found) Three.append(a.getName());
						Three.append(System.lineSeparator());
						break;
					case "Coordinator":
						for(Member b:g.getMembers()){
							if(comapare(b.getEffectiveName(),a.getName())){
								for(Role v:b.getRoles()){
									for(String c:roleIds){
										if(v.getId().equals(c)&&!v.getId().equals("320252736035880960")) b.getRoles().remove(b.getGuild().getRoleById(c));
									}
								}
								controller.addRolesToMember(b,b.getGuild().getRoleById("320252736035880960")).queue();
								if(!Four.toString().contains(b.getAsMention())) Four.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
								found = true;
								break;
							}
						}
						if(!found) Four.append(a.getName());
						Four.append(System.lineSeparator());
						break;
					case "Admin":
						for(Member b:g.getMembers()){
							if(comapare(b.getEffectiveName(),a.getName())){
								for(Role v:b.getRoles()){
									for(String c:roleIds){
										if(v.getId().equals(c)&&!v.getId().equals("320253083474984961")) b.getRoles().remove(b.getGuild().getRoleById(c));
									}
								}
								controller.addRolesToMember(b,b.getGuild().getRoleById("320253083474984961")).queue();
								if(!Five.toString().contains(b.getAsMention())) Five.append(b.getAsMention()).append(" *(").append(a.getName()).append(")*");
								found = true;
								break;
							}
						}
						if(!found) Five.append(a.getName());
						Five.append(System.lineSeparator());
						break;
					default:
						for(Member b:g.getMembers()){
							if(comapare(b.getEffectiveName(),a.getName())){
								for(Role v:b.getRoles()){
									for(String c:roleIds){
										if((One.toString()+Two.toString()+Three.toString()+Four.toString()+Five.toString()).contains(a.getName())) break;
										if(v.getId().equals(c)) controller.removeRolesFromMember(b,b.getGuild().getRoleById(c)).queue();
									}
								}

								break;
							}
						}
						break;
				}
			}
		}catch(java.lang.NoClassDefFoundError|IOException eeeee){
			System.out.println(eeeee.getMessage());
			eeeee.printStackTrace();
		}
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e){
		if(e.getGuild().getId().equals("254861442799370240")) clanRanks(e.getGuild());
		EventListener.logger(EventListener.logPrefix(0) + "Updated Zamorak Cult clan ranks.");
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
	    if((e.getAuthor().isBot()||
		        e.getAuthor().getAsMention().equals(Bot.jda.getSelfUser().getAsMention())||
		        (!channelWhitelisted(e.getChannel().getId()+"")&&!e.getMessage().getContent().contains("pullconfig")||e.getChannelType().equals(ChannelType.PRIVATE)))){
            return;
        }
		BotUser author = new BotUser(e.getAuthor(),e.getGuild());
		if(Home==null) EventListener.Home= e.getGuild();

		String input = e.getMessage().getRawContent();
        if(e.isFromType(ChannelType.TEXT)) logger(logPrefix(2)+"("+e.getGuild().getName()+", #"+e.getTextChannel().getName()+") "+e.getAuthor().getName()+": "+input);
	    if(e.isFromType(ChannelType.PRIVATE)) logger(logPrefix(2)+"(Private Message) "+e.getAuthor().getName()+": "+input);
	    if(isCommand(e.getMessage())){
			input=input.replaceFirst(LITERAL,"");
			Scanner command = new Scanner(input);
			if(command.hasNext()){
				switch(command.next().toLowerCase()){
					default:
						if(e.getChannelType().isGuild()) e.getMessage().delete().queue();
						break;
					case "id":
						if(!author.hasPermission("command.id")&&!author.isIsadmin()){
							if(e.isFromType(ChannelType.TEXT)) logger(logPrefix(6)+""+e.getAuthor().getName()+" was denied access to the command.");
							if(e.getChannelType().isGuild()) e.getMessage().delete().queue();
							return;
						}
						e.getChannel().sendMessage(e.getAuthor().getAsMention()+", Your ID is "+e.getAuthor()+".").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
						e.getChannel().sendMessage(author.getPermissions().toString()).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
						e.getChannel().sendMessage(author.isIsadmin()+"").queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
						if(e.getChannelType().isGuild()) e.getMessage().delete().queue();
						break;
					case "clan":
						if(!author.hasPermission("command.clan")&&!author.isIsadmin()){
							if(e.isFromType(ChannelType.TEXT)) logger(logPrefix(6)+""+e.getAuthor().getName()+" was denied access to the command.");
							if(e.getChannelType().isGuild()) e.getMessage().delete().queue();
							return;
						}
						String Clan = "Zamorak Cult";
						if(command.hasNext()){
							StringBuilder ClanBuilder = new StringBuilder(command.next());
							while(command.hasNext()){
								ClanBuilder.append(" ").append(command.next());
							}
							Clan = ClanBuilder.toString();
						}
						try{
							java.util.List<ClanMate> clanMates = hiscores.clanInformation(Clan);
							EmbedBuilder Ranks = new EmbedBuilder();
							StringBuilder One = new StringBuilder();
							if(e.getGuild().getId().equals("254861442799370240")&&Clan.equals("Zamorak Cult"))One.append(e.getGuild().getRoleById(254883837757227008L).getAsMention()).append(System.lineSeparator());
							StringBuilder Two = new StringBuilder();
							if(e.getGuild().getId().equals("254861442799370240")&&Clan.equals("Zamorak Cult"))Two.append(e.getGuild().getRoleById(268490396617801729L).getAsMention()).append(System.lineSeparator());
							StringBuilder Three = new StringBuilder();
							if(e.getGuild().getId().equals("254861442799370240")&&Clan.equals("Zamorak Cult"))Three.append(e.getGuild().getRoleById(258350529229357057L).getAsMention()).append(System.lineSeparator());
							StringBuilder Four = new StringBuilder();
							if(e.getGuild().getId().equals("254861442799370240")&&Clan.equals("Zamorak Cult"))Four.append(e.getGuild().getRoleById(254881136524656640L).getAsMention()).append(System.lineSeparator());
							StringBuilder Five = new StringBuilder();
							StringBuilder Six = new StringBuilder();
							int admins = StringUtils.countMatches(clanMates.toString(),"Admin")/2+1;
							int admincount = 0;
							for(ClanMate a : clanMates){
								boolean found = false;
								switch (a.getRank()){
									case "Owner":
										for(Member b:e.getGuild().getMembers()){
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
										for(Member b:e.getGuild().getMembers()){
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
										for(Member b:e.getGuild().getMembers()){
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
										for(Member b:e.getGuild().getMembers()){
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
										for(Member b:e.getGuild().getMembers()){
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
							Ranks.setColor(blurple);
							Ranks.setDescription("The bot has matched the accounts with it's best guess of what their discord tag might be. There is still a significant margin for error, so let me know if something goes wrong, or something is omitted that should not be. However if you're running the command for a clan other than that which owns the discord server, things will be matched wrong.");
							String Time = new SimpleDateFormat("MM/dd/YYYY hh:mma zzz").format(new Date());
							Ranks.setFooter("Generated "+Time+" For "+Clan,Bot.jda.getSelfUser().getAvatarUrl());
							e.getChannel().sendMessage(Ranks.build()).queue( msg -> msg.delete().queueAfter(2,TimeUnit.MINUTES));
						}catch(java.lang.NoClassDefFoundError|IOException eeeee){
							System.out.println(eeeee.getMessage());
							eeeee.printStackTrace();
						}
						if (e.getChannelType().isGuild()) e.getMessage().delete().queue();
						break;

				}
			}
		}

		if((isDevCommand(e.getMessage()))){
	    	if(e.getChannelType().equals(ChannelType.PRIVATE)||author.isIsadmin()||isSeth(e.getAuthor())){
			input=input.replaceFirst(DEVLITERAL,"");
			Scanner command = new Scanner(input);
			if(command.hasNext()) switch (command.next().toLowerCase()) {
				case "clanroles":
					clanRanks(e.getGuild());
					e.getChannel().addReactionById(e.getMessageId(), "👍").queue();
					if (e.getChannelType().isGuild()) e.getMessage().delete().queueAfter(10,TimeUnit.SECONDS);
					break;
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
