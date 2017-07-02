package net.swvn9;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

import java.util.ArrayList;
import java.util.List;

import static net.swvn9.BotListeners.logPrefix;

class Bot{

    static List<JDA> jdas = new ArrayList<>();
    static int toShard = 2;

    public static void main(String[] args) {
        try{
            BotConfig.loadConfig();
            for(int i=0;i<toShard;i++){
                BotListeners.logger(logPrefix(8)+"Starting Shard "+i+".");
                jdas.add(new JDABuilder(AccountType.BOT).addEventListener(new BotListeners()).addEventListener(new BotLogging()).setStatus(OnlineStatus.INVISIBLE).setToken(BotConfig.getToken()).useSharding(i,toShard).buildBlocking());
            }
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
            System.out.println(e.getMessage());
        }
    }
    static void restart(int s){
        try{
            BotListeners.logger(logPrefix(8)+"Restarting Shard "+s+".");
            int shard = jdas.get(s).getShardInfo().getShardId();
            int total = jdas.get(s).getShardInfo().getShardTotal();
            jdas.get(s).getPresence().setStatus(OnlineStatus.INVISIBLE);
            jdas.get(s).shutdown(false);
            jdas.remove(jdas.get(s));
            BotConfig.loadConfig();
            BotListeners.logger(logPrefix(7)+"Starting Shard "+shard+".");
            jdas.add(new JDABuilder(AccountType.BOT).addEventListener(new BotListeners()).addEventListener(new BotLogging()).setStatus(OnlineStatus.INVISIBLE).setToken(BotConfig.getToken()).useSharding(shard,total).buildBlocking());
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
            System.out.println(e.getMessage());
        }
    }
}