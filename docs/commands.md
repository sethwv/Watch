---
layout: page
permalink: /commands/
---
#### Invite
```markdown
#Node: 
command.inv
#Usage: 
;;inv
#Description: 
Generate a one-time-use invite that is valid for 24 hours
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
#### Kick
```markdown
#Node: 
command.kick
#Usage: 
;;kick <mention(s)> <reason>
#Description: 
Kick user(s) with an optional Message
```
#### Eval
#Node: 
command.eval
#Usage: 
;;eval <line>
#Description: 
Evaluate a line of code
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
#### Purge
```markdown
#Node: 
command.purge
#Usage: 
;;purge <mentions> <number>
#Description: 
Mentions are optional, if a number of messages to purge is not specified, it will be 10. Pinned messages will not be deleted.
```
#### RS Adventurer's Log
```markdown
#Node: 
command.alog
#Usage: 
;;alog <RunescapeName>
#Description: 
Fetch the RuneScape adventurer's log for the specified player name.
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
Music: Queue
```markdown
#Node: 
command.queue
#Usage: 
;;queue
#Description: 
Display the current music queue.
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
#### Music: Skip
```markdown
#Node: 
command.skip
#Usage: 
;;skip <pos>
#Description: 
Skip the current song. If the queue is empty, the bot will stop playing. If you specify a position, the song will be removed from the queue.
```
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
#### Music: Play
```markdown
#Node: 
command.play
#Usage: 
;;play <youtube/playlist-link/watch-link/soundcloud>
#Description: 
If the queue is empty, play the linked song. If not empty, add to the queue.
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
#### RS Clan Ranks
```markdown
#Node: 
command.clan
#Usage: 
;;clan
#Description: 
(for now) Pull the upper ranks of the RuneScape clan Zamorak Cult, and match any names with those on the current discord guild.
```
