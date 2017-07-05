package net.swvn9.Watch;

import io.sentry.Sentry;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

import java.util.ArrayList;
import java.util.List;

class Bot{

    static List<JDA> shards = new ArrayList<>();
    static int toShard = 2;

    public static void main(String[] args) {
        BotConfig.loadConfig();
        Sentry.init();
        try{
            for(int i=0;i<toShard;i++){
                BotListeners.logger(BotListeners.logPrefix(8)+"Starting Shard "+i+".");
                shards.add(new JDABuilder(AccountType.BOT).addEventListener(new BotListeners())/*.addEventListener(new BotLogging()).addEventListener(new BotGeneric())*/.setStatus(OnlineStatus.INVISIBLE).setToken(BotConfig.getToken()).useSharding(i,toShard).buildBlocking());
            }
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
            Sentry.capture(e);
            System.out.println(e.getMessage());
        }
    }
    static void restart(int s){
        try{
            BotListeners.logger(BotListeners.logPrefix(8)+"Restarting Shard "+s+".");
            int shard = shards.get(s).getShardInfo().getShardId();
            int total = shards.get(s).getShardInfo().getShardTotal();
            shards.get(s).getPresence().setStatus(OnlineStatus.INVISIBLE);
            shards.get(s).shutdown(false);
            shards.remove(shards.get(s));
            BotConfig.loadConfig();
            BotListeners.logger(BotListeners.logPrefix(7)+"Starting Shard "+shard+".");
            shards.add(new JDABuilder(AccountType.BOT).addEventListener(new BotListeners())/*.addEventListener(new BotLogging()).addEventListener(new BotGeneric())*/.setStatus(OnlineStatus.INVISIBLE).setToken(BotConfig.getToken()).useSharding(shard,total).buildBlocking());
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
            Sentry.capture(e);
            System.out.println(e.getMessage());
        }
    }
}