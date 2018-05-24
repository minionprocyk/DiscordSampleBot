package com.procyk.industries.command;

import com.procyk.industries.audio.playback.AudioServiceManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.*;

@Singleton
public class CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
    private Map<String,String> commands;
    private CommandStore commandStore;
    private final AudioServiceManager audioServiceManager;


    @Inject
    public CommandExecutor(AudioServiceManager audioServiceManager, CommandStore commandStore) {
        commands = commandStore.getCommands();
        this.commandStore=commandStore;
        this.audioServiceManager = audioServiceManager;
    }
    public void shutdown(MessageChannel messageChannel, Member member) {
        if(member.isOwner())
            messageChannel.getJDA().shutdown();
        else
            messageChannel.sendMessage(member.getUser().getName()+" tried to fuck me up.... HAHA").queue();
    }
    public void leaveVoiceChannel(Guild guild) {
        guild.getAudioManager().closeAudioConnection();
    }
    public void joinVoiceChannel(MessageChannel messageChannel, Channel channel, Member member, Guild guild) {
        if(guild.getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT)) {
            VoiceChannel voiceChannel = member.getVoiceState().getChannel();
            AudioManager audioManager = guild.getAudioManager();
            audioServiceManager.joinRequestedChannel(voiceChannel,audioManager);
        }
        else {
            messageChannel.sendMessage("I do not have permission to join "+channel.getName()).queue();
        }
    }

    public void playerCommands(MessageChannel messageChannel, Command command) {
        ReservedCommand.PlayerCommands playerCommand = ReservedCommand.PlayerCommands.parse(command.getKey());
        switch(playerCommand){
            case localmusic:
                StringBuilder stringBuilder = new StringBuilder(250);
                List<Path> knownMusic = audioServiceManager.getKnownMusic();
                for(int i=0;i<knownMusic.size();i++) {
                    Path musicFile = knownMusic.get(i);
                    if(musicFile.toFile().isFile()) {
                        stringBuilder.append("Track: "+i+" - "+ FilenameUtils.getBaseName(musicFile.getFileName().toString())+"\n");
                    }
                }
                messageChannel.sendMessage(stringBuilder.toString().substring(0,Math.min(stringBuilder.length(),1999))).queue();
                break;
            case commands:
                List<ReservedCommand.PlayerCommands> playerCommands = Arrays.asList(ReservedCommand.PlayerCommands.values());
                Collections.sort(playerCommands);
                messageChannel.sendMessage(playerCommands.toString()).queue();
                break;
            case clear:
                audioServiceManager.clearPlaylist();
                break;
            case skip:
            case next:
                audioServiceManager.next();
                break;
            case pause:
            case stop:
                audioServiceManager.pause();
                break;
            case last:
            case previous:
                audioServiceManager.last();
                break;
            case playlist:
                messageChannel.sendMessage(audioServiceManager.getPlayList()).queue();
                break;
            case resume:
                audioServiceManager.resume();
                break;
            case playlocal:
                //todo add translation step and change ability to playlocal by filename in addition to index
                int index = Integer.parseInt(command.getValue());
                String songPath = audioServiceManager.getKnownMusic().get(index).toString();
                command.setValue(songPath);
                audioServiceManager.loadWithArgs(command);
                break;
            case add:
            case play:
            case queue :
                if(StringUtils.isBlank(command.getValue())) {
                    messageChannel.sendMessage(
                            String.format("I can't read [!player %s]. Try [!player %s <youtube_link>]",command.getKey())
                    ).queue();
                }
                else {
                    audioServiceManager.loadWithArgs(command);
                }
                break;
            case seek:

                break;
            case volume:
                if(StringUtils.isBlank(command.getValue())) {
                    messageChannel.sendMessage("Player Volume: ???").queue();
                }
                else {
                    int volume = Integer.parseInt(command.getValue().trim());
                    audioServiceManager.setVolume(volume);
                }
                break;
            default:
                messageChannel.sendMessage(
                        String.format("!player %s is wrong. Try !player !queue <youtube_link>",StringUtils.defaultIfBlank(command.getKey(),""))
                ).queue();
        }
    }
    public boolean addCommand(MessageChannel messageChannel,Command command) {
        //get the key and value from this string
        String returnString = this.commands.putIfAbsent(command.getKey(),command.getFormattedString());
        if(Objects.isNull(returnString)) {
            logger.info("Command added "+command.getKey());
            commandStore.saveCommand(command);
            logger.info("Command saved to file "+command.getKey());
            messageChannel.sendMessage("Added ".concat(command.getKey())).queue();
            return true;
        }
        return false;
    }
    public void printCommands(MessageChannel messageChannel) {
        List<String> commandList = new ArrayList<>(commands.keySet());
        Collections.sort(commandList);
        messageChannel.sendMessage(commandList.toString()).queue();
    }
    /**
     * Expects a {@code Command} formatted as !edit !usercommand !changes. This method will lookup the existence of
     * !usercommand and if it exists and the requesting user has permission to edit the command, will swap the text
     * contents of the command with what is provided.
     * e.g. Existing command = !tickle I tickle you! -> !edit !tickle get tickled! -> !tickle get tickled!
     * @param command Container of command information
     */
    public void editCommand(MessageChannel messageChannel, Member member,Command command) {
        if(member.hasPermission(Permission.ADMINISTRATOR)) {
            String keyCommand = commands.get(command.getKey());
            if(keyCommand!=null) {
                deleteCommand(messageChannel,member,command);
                addCommand(messageChannel,command);
            }
            else {
                messageChannel.sendMessage("Command "+command.getKey()+" does not exist").queue();
            }
        }
        else {
            messageChannel.sendMessage(
                    member.getUser().getName()+" doesn't have permission to do that :stuck_out_tongue_winking_eye: "
            ).queue();
        }

    }
    public void deleteCommand(MessageChannel messageChannel, Member member, Command command) {
        if(member.hasPermission(Permission.ADMINISTRATOR)) {
            commandStore.deleteCommand(command);
            commands.remove(command.getKey());
            messageChannel.sendMessage(command.getKey()+" has been deleted").queue();
        }
        else {
            messageChannel.sendMessage(member.getUser().getName()+" doesn't have permission to do that :stuck_out_tongue_winking_eye: ")
                    .queue();
        }

    }
    /**
     * Check if user command contains a key and value. A key only reference will search a map of commands for an
     * existing command to execute. A key value reference will attempt to add the command using the value provided.
     * @param command
     */
    public void userCommand(MessageChannel messageChannel, Command command, Action reflexiveAction) {
        Objects.requireNonNull(command);
        if(StringUtils.isNotBlank(command.getKey())
           && StringUtils.isNotBlank(command.getValue())) {
            //addCommand(messageChannel,command);
        } else if(StringUtils.isNotBlank(command.getKey())
                && StringUtils.isBlank(command.getValue())) {
            command.setValue(
                    commands.getOrDefault(command.getKey(),"Could not find command "+command.getKey())
            );
        }
        //todo: add circular reflexive commands. This current resolves one layer of user commands
        //nested reflection check
        if(command.isReflexive()) {
            reflexiveAction.perform(command);
        }
        else
            messageChannel.sendMessage(command.getValue()).queue();
    }

    public void groupCommands(MessageChannel messageChannel, Member member, Command command) {
        //todo fill in command
    }
    //[name][event][action]
    public void notifyme(MessageChannel messageChannel, Member member, Command command) {
        //!notifyme berge gamefinished !player !play <link> !whyme
    }
}
