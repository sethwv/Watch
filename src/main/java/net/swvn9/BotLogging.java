package net.swvn9;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;


import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;

class BotLogging extends ListenerAdapter {

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e){
        TextChannel send = null;
        for(TextChannel c:e.getGuild().getTextChannels()){
            if(c.getName().contains("bot-log")){
                send = c;
            }
        }
        if(send!=null){
            EmbedBuilder log = new EmbedBuilder();
            StringBuilder roles = new StringBuilder();
            e.getRoles().forEach(msg->roles.append("`- ").append(msg.getName()).append("`\n"));
            log.setColor(new Color(148,168,249));
            log.addField(e.getMember().getEffectiveName()+"#"+e.getMember().getUser().getDiscriminator(),e.getMember().getUser().getId(),true);
            log.addField("Roles Removed",roles.toString(),true);
            send.sendMessage(log.build()).queue();
        }
    }
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e){
        TextChannel send = null;
        for(TextChannel c:e.getGuild().getTextChannels()){
            if(c.getName().contains("bot-log")){
                send = c;
            }
        }
        if(send!=null){
            EmbedBuilder log = new EmbedBuilder();
            StringBuilder roles = new StringBuilder();
            e.getRoles().forEach(msg->roles.append("`- ").append(msg.getName()).append("`\n"));
            log.setColor(new Color(148,168,249));
            log.addField(e.getMember().getEffectiveName()+"#"+e.getMember().getUser().getDiscriminator(),e.getMember().getUser().getId(),true);
            log.addField("Roles Added",roles.toString(),true);
            send.sendMessage(log.build()).queue();
        }
    }
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e){
        TextChannel send = null;
        for(TextChannel c:e.getGuild().getTextChannels()){
            if(c.getName().contains("bot-log")){
                send = c;
            }
        }
        if(send!=null){
            EmbedBuilder log = new EmbedBuilder();
            log.setColor(new Color(148,168,249));
            log.addField(e.getMember().getEffectiveName()+"#"+e.getMember().getUser().getDiscriminator(),e.getMember().getUser().getId(),true);
            log.addField("Has joined the server!","\u200B",true);
            send.sendMessage(log.build()).queue();
        }
    }
    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent e){
        TextChannel send = null;
        for(TextChannel c:e.getGuild().getTextChannels()){
            if(c.getName().contains("bot-log")){
                send = c;
            }
        }
        if(send!=null){
            EmbedBuilder log = new EmbedBuilder();
            log.setColor(new Color(148,168,249));
            log.addField(e.getMember().getEffectiveName()+"#"+e.getMember().getUser().getDiscriminator(),e.getMember().getUser().getId(),true);
            log.addField("Has left the server!","\u200B",true);
            send.sendMessage(log.build()).queue();
        }
    }
    @Override
    public void onGuildMemberNickChange(GuildMemberNickChangeEvent e){
        TextChannel send = null;
        for(TextChannel c:e.getGuild().getTextChannels()){
            if(c.getName().contains("bot-log")){
                send = c;
            }
        }
        if(send!=null){
            EmbedBuilder log = new EmbedBuilder();
            log.setColor(new Color(148,168,249));
            log.addField(e.getMember().getEffectiveName()+"#"+e.getMember().getUser().getDiscriminator(),e.getMember().getUser().getId(),true);
            if(e.getNewNick()==null){
                log.addField("Removed nickname","Old Name: "+e.getPrevNick(),true);
            } else if(e.getPrevNick()==null){
                log.addField("Added nickname","New Name: "+e.getNewNick(),true);
            } else {
                log.addField("Changed nickname","Old Name: "+e.getPrevNick()+"\nNew Name: "+e.getNewNick(),true);
            }
            send.sendMessage(log.build()).queue();
        }
    }
    public void onGuildMessageUpdate(GuildMessageUpdateEvent e){
        TextChannel send = null;
        for(TextChannel c:e.getGuild().getTextChannels()){
            if(c.getName().contains("bot-log")){
                send = c;
            }
        }
        if(send!=null){
            EmbedBuilder log = new EmbedBuilder();
            log.setColor(new Color(148,168,249));
            log.addField(e.getMember().getEffectiveName()+"#"+e.getMember().getUser().getDiscriminator(),e.getMember().getUser().getId(),true);
            log.addField("Edited Message",e.getMessage().getId()+" in "+e.getChannel().getAsMention(),true);
            send.sendMessage(log.build()).queue();
        }
    }
}


