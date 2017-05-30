package net.swvn9;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

class Bot{

    static JDA jda;

    //HANDLER BRANCH


    public static void main(String[] args) {
            try{
                BotConfig.loadConfig();
                jda = new JDABuilder(AccountType.BOT).addEventListener(new BotListener()).addEventListener(new ReadyListener()).setStatus(OnlineStatus.INVISIBLE).setToken(BotConfig.getToken()).buildBlocking();
            } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
                System.out.println(e.getMessage());
            }
    }

    static void restart(){
        try{
            jda.getPresence().setStatus(OnlineStatus.INVISIBLE);
            jda.shutdown(false);
            jda=null;
            BotConfig.loadConfig();
            jda = new JDABuilder(AccountType.BOT).addEventListener(new BotListener()).addEventListener(new ReadyListener()).setStatus(OnlineStatus.INVISIBLE).setToken(BotConfig.getToken()).buildBlocking();
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
            System.out.println(e.getMessage());
        }
    }

}
