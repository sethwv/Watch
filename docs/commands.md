---
permalink: /commands/
---
![Watch](https://u.swvn9.net/2017/SQAne.png)[![Repo](https://u.swvn9.net/2017/Ziaoe.png)](https://github.com/swvn9/Watch/)
[![Home](https://u.swvn9.net/2017/KNDW9.png)](https://botwat.ch)

---

This page has some sections that say "self-host only", I don't currently host this bot for anyone, those sections are only there in case I choose to host the bot publicly in the future, so I don't need to change any site assets.

If you self-host the bot, everything will 'work' for you, so long as you set up the permissions and configuration properly.

---

![Help](https://u.swvn9.net/2017/gouXP.png)

#### Help
```markdown
#Node: 
command.help
#Usage: 
;;help <keyword> <-a,c>
#Description: 
See all of the commands associated with the bot that you can use, sent to you in a dm unless specified otherwise.
#Flags:
<-a> All commands
<-c> In current channel
```

---

![Moderation](https://u.swvn9.net/2017/VDTx9.png)

#### Kick
```markdown
#Node: 
command.kick
#Usage: 
;;kick <mention(s)> <reason>
#Description: 
Kick user(s) with an optional Message
```

#### Ban
```markdown
#Node: 
command.ban
#Usage: 
;;ban <mention(s)> <reason>
#Description: 
Ban user(s) with an optional Message
```

#### Purge
```markdown
#Node: 
command.purge
#Usage: 
;;purge <mentions> <number>
#Description: 
Mentions are optional, if a number of messages to purge is not specified, it will be 10. Pinned messages will not be deleted.
```

#### Watch
```markdown
#Node: 
command.watch
#Usage: 
;;watch <list/del/add> <keyword>
#Description: 
Have the bot "watch" for certain keywords in chat, and log any occurrences to a channel called #logs
Keywords are not case-sensitive
```

#### Say
```markdown
#Node: 
command.say
#Usage: 
;;say <Message>
#Description: 
Send a Message as the bot
```

---

![Info](https://u.swvn9.net/2017/oOB6u.png)

#### Invite
```markdown
#Node: 
command.inv
#Usage: 
;;inv
#Description: 
Generate a one-time-use invite that is valid for 24 hours
```

#### ID
```markdown
#Node: 
command.id
#Usage: 
;;id
#Description: 
Grab the ID and any permissions associated with your user ID.
```

#### Bot Utility
```markdown
#Node: 
command.bot
#Usage: 
;;bot <-s/-a/-k> <?listener>
#Description: 
You should probably stay away from everything this command does except for ;;bot -s
```

#### Roles
```markdown
#Node: 
command.roles
#Usage: 
;;roles
#Description: 
Get all of the role-names and IDs associated with the current discord guild.
```

---

![Music](https://u.swvn9.net/2017/dks8T.png)

### Music: Queue
```markdown
#Node: 
command.queue
#Usage: 
;;queue
#Description: 
Display the current music queue.
```

#### Music: Skip
```markdown
#Node: 
command.skip
#Usage: 
;;skip <pos>
#Description: 
Skip the current song. If the queue is empty, the bot will stop playing. If you specify a position, the song will be removed from the queue.
```

#### Music: Play
```markdown
#Node: 
command.play
#Usage: 
;;play <youtube/playlist-link/watch-link/soundcloud>
#Description: 
If the queue is empty, play the linked song. If not empty, add to the queue.
```

---

![RuneScape](https://u.swvn9.net/2017/cj8zA.png)

#### RS Adventurer's Log
```markdown
#Node: 
command.alog
#Usage: 
;;alog <RunescapeName>
#Description: 
Fetch the RuneScape adventurer's log for the specified player name.
```

#### RS Clan Ranks
```markdown
#Node: 
command.clan
#Usage: 
;;clan
#Description: 
(for now) Pull the upper ranks of the RuneScape clan Zamorak Cult, and match any names with those on the current discord guild.
```

---

![Utility](https://u.swvn9.net/2017/VK9rw.png)

#### Stop
```markdown
#Node: 
command.stop
#Usage: 
;;stop
#Description: 
Stop the shard that the guild is running on. (There is no way to restart)
```
#### Kill
```markdown
#Node: 
command.kill
#Usage: 
;;kill
#Description: 
Kill the bot and return the host machine to the command line/desktop.
```
#### Show Config
```markdown
#Node: 
command.config
#Usage: 
;;showconfig
#Description: 
Spit out the contents of the Config.yml file to a rich embed.
```
#### Pull Config
```markdown
#Node: 
command.pullconfig
#Usage: 
;;pullconfig
#Description: 
Pull the latest configuration from the Config.yml file.
```

#### Eval
```markdown
#Node: 
command.eval
#Usage: 
;;eval <line>
#Description: 
Evaluate a line of code
```

#### Rebrandly Link
```markdown
#Node: 
command.link
#Usage: 
;;link <url> <redirect>
#Description: 
Using the rebrandly api token defined in the config file, create redirect links.
```

#### Enable/Disable Input
```markdown
Enable/Disable Input
#Node: 
command.input
#Usage: 
;;input
#Description: 
Enable or disable input for the bot so that the bot can stay running while testing.
```
