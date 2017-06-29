package net.swvn9;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

import java.util.ArrayList;
import java.util.List;

import static net.swvn9.BotEvent.logPrefix;

class Bot{

    static JDA jda;
    static List<JDA> jdas = new ArrayList<>();
    static int toShard = 2;

    public static void main(String[] args) {
        try{
            Config.loadConfig();
            for(int i=0;i<toShard;i++){
                BotEvent.logger(logPrefix(8)+"Starting Shard "+i+".");
                jdas.add(new JDABuilder(AccountType.BOT).addEventListener(new BotEvent()).addEventListener(new BotReady()).addEventListener(new BotLogging()).setStatus(OnlineStatus.INVISIBLE).setToken(Config.getToken()).useSharding(i,toShard).buildBlocking());
            }
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
            System.out.println(e.getMessage());
        }
    }
    static void restart(int s){
        try{
            BotEvent.logger(logPrefix(8)+"Restarting Shard "+s+".");
            int shard = jdas.get(s).getShardInfo().getShardId();
            int total = jdas.get(s).getShardInfo().getShardTotal();
            jdas.get(s).getPresence().setStatus(OnlineStatus.INVISIBLE);
            jdas.get(s).shutdown(false);
            jdas.remove(jdas.get(s));
            Config.loadConfig();
            BotEvent.logger(logPrefix(7)+"Starting Shard "+shard+".");
            jdas.add(new JDABuilder(AccountType.BOT).addEventListener(new BotEvent()).addEventListener(new BotReady()).addEventListener(new BotLogging()).setStatus(OnlineStatus.INVISIBLE).setToken(Config.getToken()).useSharding(shard,total).buildBlocking());
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
            System.out.println(e.getMessage());
        }
    }
}