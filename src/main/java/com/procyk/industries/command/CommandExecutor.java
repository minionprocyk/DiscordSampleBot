package com.procyk.industries.command;

import com.procyk.industries.audio.playback.AudioServiceManager;
import com.procyk.industries.audio.playback.TrackScheduler;
import com.procyk.industries.bot.util.MessageHandler;
import com.procyk.industries.strings.Strings;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


@Singleton
public class CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
    private Map<String,String> commands;
    private CommandStore commandStore;
    private final AudioServiceManager audioServiceManager;
    private final Strings specialStringsUtil;
    @Inject
    public CommandExecutor(AudioServiceManager audioServiceManager, CommandStore commandStore, Strings strings) {
        commands = commandStore.getCommands();
        this.commandStore=commandStore;
        this.audioServiceManager = audioServiceManager;
        this.specialStringsUtil = strings;
    }
    public void shutdown(MessageChannel messageChannel, Member member) {
        if(member.isOwner())
            messageChannel.getJDA().shutdown();
        else
            MessageHandler.sendMessage(messageChannel, member.getUser().getName()+" tried to fuck me up.... HAHA");
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
            MessageHandler.sendMessage(messageChannel, "I do not have permission to join "+channel.getName());
        }
    }

    public void playerCommands(MessageChannel messageChannel, Command command) {
        AudioPlayer player = audioServiceManager.getAudioPlayer();
        ReservedCommand.PlayerCommands playerCommand = ReservedCommand.PlayerCommands.parse(command.getKey());
        switch(playerCommand){
            case localmusic:
                /**
                 * Allow an argument to be passed, this will be the folder you want to access.
                 * i.e. !player !localmusic pnbajamclips
                 *      !player !localmusic
                 *      !player !localmusic pnbajamclips/PS2Jam/Ps2Jam
                 */
                //if the command value contains nothing list the root directory
                StringBuilder stringBuilder = new StringBuilder(50);
                List<Path> knownMusic = null;
                try {
                    knownMusic = audioServiceManager.getSongsInDirectory(command.getValue());
                    knownMusic.forEach(path->
                        stringBuilder
                                .append(audioServiceManager.trimRootPath(path.toString()))
                                .append("\n")
                    );
                } catch (IOException e) {
                    logger.warn("Could not list songs",e);
                    MessageHandler.sendMessage(messageChannel,"The directory path did not match exactly");
                }

                MessageHandler.sendMessage(messageChannel,stringBuilder.toString());
                break;
            case commands:
                List<ReservedCommand.PlayerCommands> playerCommands = Arrays.asList(ReservedCommand.PlayerCommands.values());
                Collections.sort(playerCommands);
                MessageHandler.sendMessage(messageChannel,playerCommands.toString());
                break;
            case clear:
                audioServiceManager.clearPlaylist();
                MessageHandler.sendMessage(messageChannel,"Playlist has been cleared");
                break;
            case skip:
            case next:
                audioServiceManager.next();
                MessageHandler.sendMessage(messageChannel, audioServiceManager.getPlayList());
                break;
            case pause:
                audioServiceManager.pause();

                MessageHandler.sendMessage(messageChannel, "Audio Player: ".concat(
                        player.isPaused() ? "off" : "on"
                ));
                break;
            case stop:
            case end:
                audioServiceManager.endCurrentSong();

                break;
            case last:
            case previous:
                audioServiceManager.last();
                MessageHandler.sendMessage(messageChannel, audioServiceManager.getPlayList());
                break;
            case playlist:
                MessageHandler.sendMessage(messageChannel, audioServiceManager.getPlayList());
                break;
            case resume:
                audioServiceManager.resume();
                break;
            case playlocal:
                int index;
                try {
                    index = Integer.parseInt(command.getValue());
                    String songPath = audioServiceManager.getKnownMusic().get(index).toString();
                    command.setValue(songPath);
                } catch (NumberFormatException e) {
                    //could be valid song path, but will need the root path attached
                    command.setValue(audioServiceManager.getLocalMusicRootPath().resolve(command.getValue()).toString());
                }

                audioServiceManager.loadWithArgs(command);
                break;
            case add:
            case play:
            case queue :
                if(StringUtils.isBlank(command.getValue())) {
                    MessageHandler.sendMessage(messageChannel, 
                            String.format("I can't read [!player %s]. Try [!player %s <youtube_link>]",command.getKey())
                    );
                }
                else {
                    audioServiceManager.loadWithArgs(command);
                }
                break;
            case seek:

                break;
            case random:
                List<Path> music = audioServiceManager.getKnownMusic();
                Random random = new Random();
                int rIndex = random.nextInt(music.size());
                String songPath = music.get(rIndex).toString();
                command.setValue(songPath);
                audioServiceManager.loadWithArgs(command);
                break;

            case repeat:
                String arg = command.getValue();
                TrackScheduler trackScheduler = audioServiceManager.getTrackScheduler();
                switch(arg.toLowerCase()) {
                    case "on":
                    case "true":
                        trackScheduler.setRepeat(true);
                        break;
                    case "off":
                    case "false":
                        trackScheduler.setRepeat(false);
                        break;
                    default:
                        boolean currVal = trackScheduler.getRepeat();
                        trackScheduler.setRepeat(!currVal);
                        MessageHandler.sendMessage(messageChannel,
                                String.format("Repeat mode was %s but is now %s.",
                                        currVal ? "on" : "off",
                                        currVal ? "off" : "on"));
                }

                break;
            case volume:
                /**
                 * !player !volume +10, !player !volume 10 !player !volume -20
                 */
                Integer currVolume = player.getVolume();
                boolean addVolume=false;
                boolean setVolume=true;

                if(StringUtils.isBlank(command.getValue())) {

                    MessageHandler.sendMessage(messageChannel, "Audio Player Volume: ".concat(currVolume.toString()));
                    return;
                }
                char firstChar = command.getValue().charAt(0);


                if( firstChar=='+') {
                    addVolume=true;
                    setVolume=false;
                }
                else if(firstChar=='-') {
                    addVolume=false;
                    setVolume=false;
                }
                else {
                    try {
                        currVolume = Integer.parseInt(command.getValue().trim());
                    } catch (NumberFormatException e) {
                        MessageHandler.sendMessage(messageChannel, "I can't interpret: ".concat(command.getValue()));
                        MessageHandler.sendMessage(messageChannel, "Try something like, [!player !volume +10]"
                        +", [!player !volume -10], or [!player !volume 10]");

                    }
                }

                if(setVolume==false) {
                    //get all chars after the operator to parse as int
                    char[] volume = new char[command.getValue().length()-1];
                    command.getValue().getChars(1,command.getValue().length(),volume,0);

                    int val = 0;
                    try {
                        val = Integer.parseInt(new String(volume));
                    } catch (NumberFormatException e) {
                        MessageHandler.sendMessage(messageChannel, "I can't interpret: ".concat(command.getValue()));
                        MessageHandler.sendMessage(messageChannel, "Try something like, [!player !volume +10]"
                                +", [!player !volume -10], or [!player !volume 10]");                    }

                    if(addVolume)
                        currVolume+=val;
                    else
                        currVolume-=val;

                }
                audioServiceManager.setVolume(currVolume);

                break;
            default:
                MessageHandler.sendMessage(messageChannel, 
                        String.format("!player %s is wrong. Try !player !queue <youtube_link>",StringUtils.defaultIfBlank(command.getKey(),""))
                );
        }
    }
    public boolean addCommand(MessageChannel messageChannel,Command command) {
        //get the key and value from this string
        String returnString = this.commands.putIfAbsent(command.getKey(),command.getFormattedString());
        if(Objects.isNull(returnString)) {
            if(command.getValue().contains(ReservedCommand.player.name())
                    && command.getValue().contains(ReservedCommand.PlayerCommands.playlocal.name())
                    ) {
                String songPath = CommandParser.searchAndReplace(command.getValue(),CommandParser.replaceDigitsAfterPlayLocalCommandPattern,
                        (str)-> audioServiceManager.getSavableLocalTrackAsString(Integer.parseInt(str)));
                    command.setValue(songPath);
            }
            logger.info("Command added "+command.getKey());
            commandStore.saveCommand(command);
            logger.info("Command saved to file "+command.getKey());
            MessageHandler.sendMessage(messageChannel, "Added ".concat(command.getKey()));
            return true;
        }
        return false;
    }
    public void printCommands(MessageChannel messageChannel) {
        List<String> commandList = new ArrayList<>(commands.keySet());
        Collections.sort(commandList);
        MessageHandler.sendMessage(messageChannel, commandList.toString());
    }
    /**
     * Expects a {@code Command} formatted as !edit !usercommand !changes. This method will lookup the existence of
     * !usercommand and if it exists and the requesting user has permission to edit the command, will swap the text
     * contents of the command with what is provided.
     * e.g. Existing command = !tickle I tickle you! -> !edit !tickle get tickled! -> !tickle get tickled!
     * @param command Container of command information
     */
    public void editCommand(MessageChannel messageChannel, Member member,Command command) {
        if(member.hasPermission(Permission.ADMINISTRATOR)
                || member.hasPermission(Permission.BAN_MEMBERS)) {
            String keyCommand = commands.get(command.getKey());
            if(keyCommand!=null) {
                deleteCommand(messageChannel,member,command);
                addCommand(messageChannel,command);
            }
            else {
                MessageHandler.sendMessage(messageChannel, "Command "+command.getKey()+" does not exist");
            }
        }
        else {
            MessageHandler.sendMessage(messageChannel, 
                    member.getUser().getName()+" doesn't have permission to do that :stuck_out_tongue_winking_eye: "
            );
        }

    }
    public String randomCommand(MessageChannel messageChannel) {
        Random random = new Random();
        String[] keys = commands.keySet().toArray(new String[0]);
        String randomKey = keys[random.nextInt(keys.length)];
        MessageHandler.sendMessage(messageChannel, "Preparing command: ".concat(randomKey));
        return randomKey;
    }
    public void deleteCommand(MessageChannel messageChannel, Member member, Command command) {
        if(member.hasPermission(Permission.ADMINISTRATOR)
                || member.hasPermission(Permission.BAN_MEMBERS)) {
            commandStore.deleteCommand(command);
            commands.remove(command.getKey());
            MessageHandler.sendMessage(messageChannel, command.getKey()+" has been deleted");
        }
        else {
            MessageHandler.sendMessage(messageChannel, member.getUser().getName()+" doesn't have permission to do that :stuck_out_tongue_winking_eye: ");
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
            String strCommand = commands.getOrDefault(command.getKey(),"Could not find command "+command.getKey());
            //if a command isn't found. suggest something that was close to it if possible
            if(strCommand.startsWith("Could not find")) {
                try {
                    strCommand = strCommand
                            .concat(". Did you mean one of these? ")
                            .concat(suggestCommands(command.getKey()).toString());
                }
                catch (NoSuchElementException nsee) {
                    //keep original strCommand
                }
            }

            command.setValue(strCommand);
        }
        //todo: add circular reflexive commands. This current resolves one layer of user commands
        //nested reflection check
        if(command.isReflexive()) {
            reflexiveAction.perform(command);
        }
        else
            MessageHandler.sendMessage(messageChannel, command.getValue());
    }
    public List<String> suggestCommands(String strCommand) {
        return commands.entrySet()
                .stream()
                .filter(entry ->
                    specialStringsUtil.almostMatches(entry.getKey(),strCommand,2)
                )
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }


    public void groupCommands(MessageChannel messageChannel, Member member, Command command) {
        //todo fill in command
    }
    //[name][event][action]
    public void notifyme(MessageChannel messageChannel, Member member, Command command) {
        //!notifyme berge gamefinished !player !play <link> !whyme
    }
}
