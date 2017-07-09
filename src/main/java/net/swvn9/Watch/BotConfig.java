package net.swvn9.Watch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.sentry.Sentry;

import java.io.*;
import java.util.*;

@SuppressWarnings("unused")
class BotConfig {
	private static final File Ldir = new File("Logs");
	private static final File Cdir = new File("Commands");
	private static final File Config = new File("Config.yml");
	private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	private static final List<String> Whitetemp = new ArrayList<>();
	private static String Whitelist[];
	private static final List<String> AdminTemp = new ArrayList<>();
	private static String AdminRoles[];
	static YamlBean config;
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private static Map<String,String[]> Groups;

	static void loadConfig(){
		try{
			if(!Cdir.exists()) //noinspection ResultOfMethodCallIgnored
				Cdir.mkdir();
			if(!Ldir.exists()) //noinspection ResultOfMethodCallIgnored
				Ldir.mkdir();
			BotConfig.config = mapper.readValue(new File("Config.yml"), YamlBean.class);
		} catch(Exception ex){
			System.out.println("There was an error with the configuration file."+System.lineSeparator()+"Please ensure that you copy the \"example_Config.yml\""+System.lineSeparator()+"fill it with your configuration choices"+System.lineSeparator()+"and rename it to \"Config.yml\"");
			System.out.println("Below is the error message.\u001B[34m"+System.lineSeparator()+ex.getLocalizedMessage()+"\u001B[0m");
			Sentry.capture(ex);
			Runtime.getRuntime().exit(0);
		}
		Scanner a = new Scanner(config.getChannelWhitelist());
		while(a.hasNext()){
			Whitetemp.add(a.next());
		}
		BotConfig.Whitelist = Whitetemp.toArray(new String[0]);
	}

	static String getToken(){
		return config.getBotToken();
	}
	static String getrebrandlyToken(){
		return config.getRebrandlyToken();
	}
	static String getrebrandlyURL(){
		return config.getRebrandlyURL();
	}
	static String[] getWhitelist(){
		return Whitelist;
	}
	static String[] getAdminRoles(){
		return AdminRoles;
	}
	static String[] getPerms(String key){
		return Groups.get(key);
	}
	static String[] getGroups(){
		String[] keys = new String[Groups.keySet().toArray().length];
		for(int i = 0; i<Groups.keySet().toArray().length;i++){
			keys[i] = Groups.keySet().toArray()[i].toString();
		}
		return keys;
	}
}
@SuppressWarnings("unused")
class UserBean {
	public String userId = "";
	public boolean admin = false;
	public int power = 0;
	public List<String> permissions = new ArrayList<>(Collections.singletonList("group.all#default"));
}
@SuppressWarnings("unused")
class GroupBean {
	public List<String> groupId = new ArrayList<>(Collections.singletonList(""));
	public boolean admin = false;
	public int power = 0;
	public List<String> permissions = new ArrayList<>(Collections.singletonList("group.all#default"));
}
@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
class YamlBean { //this is my yaml bean thingamahooza

	private String botToken = "";
	private String rebrandlyToken = "";
	private String rebrandlyURL = "";
	private String channelWhitelist = "";
	private Map<String,UserBean> users;
	private Map<String,GroupBean> groups;

	String getBotToken() {
		return botToken;
	}
	void setBotToken(String botToken) {
		this.botToken = botToken;
	}

	String getChannelWhitelist() {
		return channelWhitelist;
	}
	void setChannelWhitelist(String channelWhitelist) {
		this.channelWhitelist = channelWhitelist;
	}

	Map<String,GroupBean> getGroups(){
		return groups;
	}
	void setGroups(Map<String,GroupBean> groups){
		this.groups = groups;
	}

	Map<String,UserBean> getUsers(){
		return users;
	}
	void setUsers(Map<String,UserBean> users){
		this.users = users;
	}

	public String getRebrandlyToken() {
		return rebrandlyToken;
	}

	public void setRebrandlyToken(String rebrandlyToken) {
		this.rebrandlyToken = rebrandlyToken;
	}

	public String getRebrandlyURL() {
		return rebrandlyURL;
	}

	public void setRebrandlyURL(String rebrandlyURL) {
		this.rebrandlyURL = rebrandlyURL;
	}
}