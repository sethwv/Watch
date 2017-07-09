package net.swvn9.Watch;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.*;

@SuppressWarnings("unused")
class BotUser {
    public String noperms[] ={"none"};
    BotUser(User u, Guild g){
        this.isadmin = false;
        this.id = u.getId();
        if(BotConfig.config.getUsers()!=null)for(String key: BotConfig.config.getUsers().keySet()){
            if(u.getId().equals(BotConfig.config.getUsers().get(key).userId)){
                if(BotConfig.config.getUsers().get(key).permissions==null) this.permissions= new ArrayList<>(Arrays.asList(noperms));
                this.id = u.getId();
                this.isadmin = BotConfig.config.getUsers().get(key).admin;
                if(power< BotConfig.config.getUsers().get(key).power) this.power = BotConfig.config.getUsers().get(key).power;
                if(BotConfig.config.getUsers().get(key).permissions!=null) this.permissions = BotConfig.config.getUsers().get(key).permissions;
            }
        }
        if(BotConfig.config.getGroups()!=null) for(String key: BotConfig.config.getGroups().keySet()) {
            for (String gid : BotConfig.config.getGroups().get(key).groupId) {
                if (g.getMember(u).getRoles().toString().contains(gid)) {
                    if (!isadmin) this.isadmin = BotConfig.config.getGroups().get(key).admin;
                    if(this.permissions==null){
                        this.permissions = BotConfig.config.getGroups().get(key).permissions;
                    } else if(BotConfig.config.getGroups().get(key).permissions!=null) {
                        this.permissions.addAll(BotConfig.config.getGroups().get(key).permissions);
                    }
                    if (power < BotConfig.config.getGroups().get(key).power) this.power = BotConfig.config.getGroups().get(key).power;
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

    @SuppressWarnings("InstanceVariableNamingConvention")
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
