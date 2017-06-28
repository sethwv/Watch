package net.swvn9;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.*;

class BotUser {
    BotUser(User u, Guild g){
        String noperms[] ={"none"};
        this.isadmin = false;
        this.id = u.getId();
        for(String key:Config.config.getUsers().keySet()){
            if(u.getId().equals(Config.config.getUsers().get(key).id)){
                if(Config.config.getUsers().get(key).permissions==null) this.permissions= new ArrayList<>(Arrays.asList(noperms));
                this.id = u.getId();
                this.isadmin = Config.config.getUsers().get(key).admin;
                if(power<Config.config.getUsers().get(key).power) this.power = Config.config.getUsers().get(key).power;
                if(Config.config.getUsers().get(key).permissions!=null) this.permissions = Config.config.getUsers().get(key).permissions;
            }
        }
        for(String key:Config.config.getGroups().keySet()) {
            for (String gid : Config.config.getGroups().get(key).id) {
                if (g.getMember(u).getRoles().toString().contains(gid)) {
                    if (!isadmin) this.isadmin = Config.config.getGroups().get(key).admin;
                    if(this.permissions==null){
                        this.permissions = Config.config.getGroups().get(key).permissions;
                    } else {
                        this.permissions.addAll(Config.config.getGroups().get(key).permissions);
                    }
                    this.permissions.addAll(Config.config.getGroups().get(key).permissions);
                    if (power < Config.config.getGroups().get(key).power) this.power = Config.config.getGroups().get(key).power;
                    break;
                }
            }
        }
        if(this.permissions==null) this.permissions= new ArrayList<>(Arrays.asList(noperms));
        Set<String> hs = new LinkedHashSet<>();
        hs.addAll(permissions);
        this.permissions = null;
        this.permissions = new ArrayList<>(hs);
        //this.permissions.addAll(hs);
    }

    private String id;
    private boolean isadmin;
    private int power;
    private List<String> permissions = new ArrayList<>();

    String getid(){
        return id;
    }
    boolean isadmin(){
        return isadmin;
    }
    int getPower(){
        return power;
    }
    List<String> getPermissions(){
        return permissions;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean hasPermission(String perm){
        for(String a:permissions){
            if(a.equalsIgnoreCase(perm))return true;
        }
        return false;
    }
}