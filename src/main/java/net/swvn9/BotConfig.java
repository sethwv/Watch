package net.swvn9;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.util.*;

@SuppressWarnings("unused")
class Config {
	private static final File Ldir = new File("Logs");
	private static final File Cdir = new File("Commands");
	private static final File Config = new File("Config.yml");
	private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	private static final List<String> Whitetemp = new ArrayList<>();
	private static String Whitelist[];
	private static final List<String> AdminTemp = new ArrayList<>();
	private static String AdminRoles[];
	static Yaml config;
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private static Map<String,String[]> Groups;

	static void loadConfig(){
		try{
			if(!Cdir.exists()) //noinspection ResultOfMethodCallIgnored
				Cdir.mkdir();
			if(!Ldir.exists()) //noinspection ResultOfMethodCallIgnored
				Ldir.mkdir();
			/*if(!Config.exists()){
				FileWriter newFile = new FileWriter(Config);
				newFile.write("#This file contains all of the configuration options available (for now)"+System.lineSeparator()+"token: Bots Token Here"+System.lineSeparator()+System.lineSeparator()+"adminrole: The admin role ID(s) separated by spaces"+System.lineSeparator()+"whitelist: Channel ID(s) Separated by spaces");
				newFile.close();
				System.out.println("The Config.yml file has been created, fill it in with the relevant information.");
				System.exit(0);
			}*/
			net.swvn9.Config.config = mapper.readValue(new File("Config.yml"), Yaml.class);
		} catch(IOException | NullPointerException ee){
			System.out.println("There was an error with the configuration file."+System.lineSeparator()+"Please ensure that you copy the \"example_Config.yml\""+System.lineSeparator()+"fill it with your configuration choices"+System.lineSeparator()+"and rename it to \"Config.yml\"");
			System.out.println("Below is the error commandMessage.\u001B[34m"+System.lineSeparator()+ee.getLocalizedMessage()+"\u001B[0m");
			System.exit(0);
		}
		Scanner a = new Scanner(config.getWhitelist());
		while(a.hasNext()){
			Whitetemp.add(a.next());
		}
		net.swvn9.Config.Whitelist = Whitetemp.toArray(new String[0]);
		/*
		Scanner b = new Scanner(config.getAdminrole());
		while(b.hasNext()){
			AdminTemp.add(b.next());
		}
		net.swvn9.Config.AdminRoles = AdminTemp.toArray(new String[0]);
		*/
	}

	static String getToken(){
		return config.getToken();
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
	static String[] getPerms(String Key){
		return Groups.get(Key);
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
class configUser {
	public String id;
	public boolean admin;
	public int power;
	public List<String> permissions;
}
@SuppressWarnings("unused")
class configGroup {
	public List<String> id;
	public boolean admin;
	public int power;
	public List<String> permissions;
}
@SuppressWarnings("unused")
class Yaml { //this is my yaml bean thingamahooza

	private String Token;
	//private String Adminrole;
	private String rebrandlyToken;
	private String rebrandlyURL;
	private String Whitelist;
	private Map<String,configUser> Users;
	private Map<String,configGroup> Groups;

	String getToken() {
		return Token;
	}
	void setToken(String Token) {
		this.Token = Token;
	}
/*
	String getAdminrole() {
		return Adminrole;
	}
	void setAdminrole(String AdminRole) {
		this.Adminrole = AdminRole;
	}
*/
	String getWhitelist() {
		return Whitelist;
	}
	void setWhitelist(String Whitelist) {
		this.Whitelist = Whitelist;
	}

	Map<String,configGroup> getGroups(){
		return Groups;
	}
	void setGroups(Map<String,configGroup> Groups){
		this.Groups = Groups;
	}

	Map<String,configUser> getUsers(){
		return Users;
	}
	void setUsers(Map<String,configUser> Users){
		this.Users = Users;
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