package net.swvn9;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class BotConfig {
	private static File Ldir = new File("Logs");
	private static File Config = new File("Config.yml");
	private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	private static List<String> Whitetemp = new ArrayList<String>();
	private static String Whitelist[];
	private static List<String> AdminTemp = new ArrayList<String>();
	private static String AdminRoles[];
	private static BotYaml config;

	static void loadConfig(){
		try{
			if(!Ldir.exists()) Ldir.mkdir();
			if(!Config.exists()){
				FileWriter newFile = new FileWriter(Config);
				newFile.write("#This file contains all of the configuration options available (for now)"+System.lineSeparator()+"token: Bots Token Here"+System.lineSeparator()+System.lineSeparator()+"adminrole: The admin role ID(s) separated by spaces"+System.lineSeparator()+"whitelist: Channel ID(s) Separated by spaces");
				newFile.close();
				System.out.println("The Config.yml file has been created, fill it in with the relevant information.");
				System.exit(0);
			}
			BotConfig.config = mapper.readValue(new File("Config.yml"), BotYaml.class);
		} catch(IOException ee){
			ee.printStackTrace();
		}
		Scanner a = new Scanner(config.getWhitelist());
		while(a.hasNext()){
			Whitetemp.add(a.next());
		}
		BotConfig.Whitelist = Whitetemp.toArray(new String[0]);
		Scanner b = new Scanner(config.getAdminrole());
		while(b.hasNext()){
			AdminTemp.add(b.next());
		}
		BotConfig.AdminRoles = AdminTemp.toArray(new String[0]);
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
