package net.swvn9;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class Config {
	private static final File Ldir = new File("Logs");
	private static final File Config = new File("Config.yml");
	private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	private static final List<String> Whitetemp = new ArrayList<>();
	private static String Whitelist[];
	private static final List<String> AdminTemp = new ArrayList<>();
	private static String AdminRoles[];
	private static Yaml config;

	static void loadConfig(){
		try{
			if(!Ldir.exists()) //noinspection ResultOfMethodCallIgnored
				Ldir.mkdir();
			if(!Config.exists()){
				FileWriter newFile = new FileWriter(Config);
				newFile.write("#This file contains all of the configuration options available (for now)"+System.lineSeparator()+"token: Bots Token Here"+System.lineSeparator()+System.lineSeparator()+"adminrole: The admin role ID(s) separated by spaces"+System.lineSeparator()+"whitelist: Channel ID(s) Separated by spaces");
				newFile.close();
				System.out.println("The Config.yml file has been created, fill it in with the relevant information.");
				System.exit(0);
			}
			net.swvn9.Config.config = mapper.readValue(new File("Config.yml"), Yaml.class);
		} catch(IOException ee){
			ee.printStackTrace();
		}
		Scanner a = new Scanner(config.getWhitelist());
		while(a.hasNext()){
			Whitetemp.add(a.next());
		}
		net.swvn9.Config.Whitelist = Whitetemp.toArray(new String[0]);
		Scanner b = new Scanner(config.getAdminrole());
		while(b.hasNext()){
			AdminTemp.add(b.next());
		}
		net.swvn9.Config.AdminRoles = AdminTemp.toArray(new String[0]);
		System.out.println(config.getGroups().toString());
	}

	static String getToken(){
		return config.getToken();
	}

	static String[] getWhitelist(){
		return Whitelist;
	}

	static String[] getAdminRoles(){
		return AdminRoles;
	}
}

class Yaml { //this is my yaml bean thingamahooza
	private String Token;
	private String Adminrole;
	private String Whitelist;
	private Map<String,String[]> Groups;

	String getToken() {
		return Token;
	}
	void setToken(String Token) {
		this.Token = Token;
	}

	String getAdminrole() {
		return Adminrole;
	}
	void setAdminrole(String AdminRole) {
		this.Adminrole = AdminRole;
	}

	String getWhitelist() {
		return Whitelist;
	}
	void setWhitelist(String Whitelist) {
		this.Whitelist = Whitelist;
	}

	Map<String,String[]> getGroups(){
		return Groups;
	}
	void setGroups(Map<String,String[]> Groups){
		this.Groups = Groups;
	}
}