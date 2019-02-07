package com.procyk.industries.command;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.procyk.industries.audio.playback.AudioServiceManager;
import com.procyk.industries.audio.playback.TrackScheduler;
import com.procyk.industries.bot.util.MessageHandler;
import com.procyk.industries.strings.Strings;
import com.procyk.industries.strings.YoutubeLinkBuilder;
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
    private final Map<String,String> commands;
    private final CommandStore commandStore;
    private final AudioServiceManager audioServiceManager;
    private final Strings specialStringsUtil;
    private final YouTube youtube;
    private final MessageHandler messageHandler;
    private static final Random random = new Random();
    private boolean voiceShutdownConfirm=false;
    @Inject
    public CommandExecutor(AudioServiceManager audioServiceManager, CommandStore commandStore, Strings strings,
                           YouTube youTube, MessageHandler messageHandler) {
        commands = commandStore.getCommands();
        this.commandStore=commandStore;
        this.audioServiceManager = audioServiceManager;
        this.specialStringsUtil = strings;
        this.youtube=youTube;
        this.messageHandler=messageHandler;
    }
    private static final Map<String,String> directionToOperation = new HashMap<String,String>()
    {{
        put("UP","+");
        put("INCREASE","+");
        put("DOWN","-");
        put("DECREASE","-");

    }};
    public void handleVoiceCommands(String strCommand, User user) {
        Optional<TextChannel> optionalTextChannel= user.getJDA().getTextChannels().stream()
                .filter(TextChannel::canTalk)
                .filter(channel-> channel.getName().equalsIgnoreCase("General"))
                .findFirst();

        TextChannel textChannel;
        Member member = user.getMutualGuilds().get(0).getMember(user);

        if(optionalTextChannel.isPresent()) {
            textChannel = optionalTextChannel.get();
            strCommand = strCommand.toUpperCase();
            Command command = new Command();

            if (strCommand.contains("VOLUME")) {
                //parse next string for the operand, then apply the proper command
                String nextWord = strCommand.substring(strCommand.indexOf("VOLUME") + "VOLUME".length() + 1).split("\\s")[0];
                String operand = directionToOperation.get(nextWord);
                if (operand != null) {
                    command.setKey("!volume");
                    switch (operand) {
                        case "+":
                            command.setValue("+20");
                            break;
                        case "-":
                            command.setValue("-20");
                            break;
                        default:
                            logger.info("Operand not supported: {}", operand);
                    }
                    playerCommands(textChannel, command);

                }
            } else if (strCommand.startsWith("MUSIC TREE")) {
//                messageHandler.sendMessage(String.format("[%s] - Did you say: %s?",
//                        user.getName(),
//                        strCommand.substring("MUSIC TREE ".length()))
//                );
                if (strCommand.endsWith("SHUTDOWN")) {
                    if (!voiceShutdownConfirm) {
                        voiceShutdownConfirm = true;
                        messageHandler.sendMessage("Are you sure you want to shutdown?");
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                voiceShutdownConfirm=!voiceShutdownConfirm;
                                messageHandler.sendMessage("Shutdown Terminated");
                            }
                        },5000L);
                    } else {
                        shutdown(textChannel, member);
                    }
                } else if (strCommand.endsWith("PLAY NEXT")) {
                    command.setKey("!next");
                    playerCommands(textChannel, command);
                } else if (strCommand.endsWith("PAUSE") || strCommand.endsWith("PAWS")) {
                    command.setKey("!pause");
                    playerCommands(textChannel, command);
                } else if (strCommand.endsWith("RESUME")) {
                    command.setKey("!resume");
                    playerCommands(textChannel, command);
                } else if(strCommand.endsWith("RANDOM")) {
                    command.setKey("!random");
                    playerCommands(textChannel,command);
                }

            }
        }
    }

    void shutdown(MessageChannel messageChannel, Member member) {
        if(member.isOwner())
            messageChannel.getJDA().shutdown();
        else
            messageHandler.sendMessage(member.getUser().getName()+" tried to fuck me up.... HAHA");
    }
    void leaveVoiceChannel(Guild guild) {
        guild.getAudioManager().closeAudioConnection();
    }
    void joinVoiceChannel(MessageChannel messageChannel, Channel channel, Member member, Guild guild) {
        if(guild.getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT)) {
            VoiceChannel voiceChannel = member.getVoiceState().getChannel();
            AudioManager audioManager = guild.getAudioManager();
            audioServiceManager.joinRequestedChannel(voiceChannel,audioManager);
        }
        else {
            messageHandler.sendMessage("I do not have permission to join "+channel.getName());
        }
    }

    void playerCommands(MessageChannel messageChannel, Command command) {
        AudioPlayer player = audioServiceManager.getAudioPlayer();
        ReservedCommand.PlayerCommands playerCommand = ReservedCommand.PlayerCommands.parse(command.getKey());
        switch(playerCommand){
            case localmusic:
                /*
                 * Allow an argument to be passed, this will be the folder you want to access.
                 * i.e. !player !localmusic pnbajamclips
                 *      !player !localmusic
                 *      !player !localmusic pnbajamclips/PS2Jam/Ps2Jam
                 */
                //if the command value contains nothing list the root directory
                StringBuilder stringBuilder = new StringBuilder(50);
                List<Path> knownMusic;
                try {
                    knownMusic = audioServiceManager.getSongsInDirectory(command.getValue());
                    knownMusic.forEach(path->
                        stringBuilder
                                .append(audioServiceManager.trimRootPath(path.toString()))
                                .append("\n")
                    );
                } catch (IOException e) {
                    logger.warn("Could not list songs",e);
                    messageHandler.sendMessage("The directory path did not match exactly");
                }

                messageHandler.sendMessage(stringBuilder.toString());
                break;
            case commands:
                List<ReservedCommand.PlayerCommands> playerCommands = Arrays.asList(ReservedCommand.PlayerCommands.values());
                Collections.sort(playerCommands);
                messageHandler.sendMessage(playerCommands.toString());
                break;
            case clear:
                audioServiceManager.clearPlaylist();
               messageHandler.sendMessage("Playlist has been cleared");
                break;
            case skip:
            case next:
                audioServiceManager.next();
                messageHandler.sendMessage( audioServiceManager.getPlayList());
                break;
            case pause:
                audioServiceManager.pause();

               messageHandler.sendMessage( "Audio Player: ".concat(player.isPaused() ? "off" : "on"));
                break;
            case stop:
            case end:
                audioServiceManager.endCurrentSong();

                break;
            case last:
            case previous:
                audioServiceManager.last();
                messageHandler.sendMessage( audioServiceManager.getPlayList());
                break;
            case playlist:
                messageHandler.sendMessage( audioServiceManager.getPlayList());
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
                    messageHandler.sendMessage(
                            String.format("I can't read [!player %s]. Try [!player %<s <youtube_link>]",command.getKey())
                    );
                }
                else {
                    audioServiceManager.loadWithArgs(command);
                    messageHandler.sendMessage( audioServiceManager.getPlayList());
                }
                break;
            case seek:

                break;
            case random:
                List<Path> music = audioServiceManager.getKnownMusic();
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
                        messageHandler.sendMessage(
                                String.format("Repeat mode was %s but is now %s.",
                                        currVal ? "on" : "off",
                                        currVal ? "off" : "on"));
                }

                break;
            case volume:
                /*
                 * !player !volume +10, !player !volume 10 !player !volume -20
                 */
                Integer currVolume = player.getVolume();
                boolean addVolume=false;
                boolean setVolume=true;

                if(StringUtils.isBlank(command.getValue())) {

                    messageHandler.sendMessage( "Audio Player Volume: ".concat(currVolume.toString()));
                    return;
                }
                char firstChar = command.getValue().charAt(0);


                if( firstChar=='+') {
                    addVolume=true;
                    setVolume=false;
                }
                else if(firstChar=='-') {
                    setVolume=false;
                }
                else {
                    try {
                        currVolume = Integer.parseInt(command.getValue().trim());
                    } catch (NumberFormatException e) {
                        messageHandler.sendMessage( "I can't interpret: ".concat(command.getValue()));
                        messageHandler.sendMessage( "Try something like, [!player !volume +10]"
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
                        messageHandler.sendMessage( "I can't interpret: ".concat(command.getValue()));
                        messageHandler.sendMessage( "Try something like, [!player !volume +10]"
                                +", [!player !volume -10], or [!player !volume 10]");                    }

                    if(addVolume)
                        currVolume+=val;
                    else
                        currVolume-=val;

                }
                //bound volume to level constraints 0-100
                currVolume = Math.max(0,currVolume);
                currVolume = Math.min(100,currVolume);

                audioServiceManager.setVolume(currVolume);
                messageHandler.sendMessage( "Audio Player Volume: ".concat(currVolume.toString()));
                break;
            default:
                messageHandler.sendMessage(
                        String.format("!player %s is wrong. Try !player !queue <youtube_link>",StringUtils.defaultIfBlank(command.getKey(),""))
                );
        }
    }

    /**
     * Takes the supplied {@code Command} and attempts to save it to file. If the command contains is a local player
     * command, or !player !playlocal command that uses an index, it will search for the path of the song and use that
     * to store the song.
     * @param messageChannel The MessageChannel
     * @param command The command
     */
    void addCommand(MessageChannel messageChannel,Command command) {
        //get the key and value from this string
        String returnString = this.commands.putIfAbsent(command.getKey(),command.getFormattedString());
        if(Objects.isNull(returnString)) {
            if(command.getValue().contains(ReservedCommand.player.name())
                    && command.getValue().contains(ReservedCommand.PlayerCommands.playlocal.name())
                    ) {
                String songPath = CommandParser.searchAndReplace(
                        command.getValue(),
                        CommandParser.replaceDigitsAfterPlayLocalCommandPattern,
                        (str)-> audioServiceManager.getSavableLocalTrackAsString(Integer.parseInt(str)));
                    command.setValue(songPath);
            }
            logger.info("Command added {}",command.getKey());
            commandStore.saveCommand(command);
            logger.info("Command saved to file {}",command.getKey());
            messageHandler.sendMessage( "Added ".concat(command.getKey()));
        }
    }
    void printCommands(MessageChannel messageChannel) {
        List<String> commandList = new ArrayList<>(commands.keySet());
        Collections.sort(commandList);
        messageHandler.sendMessage( commandList.toString());
    }
    /**
     * Expects a {@code Command} formatted as !edit !usercommand !changes. This method will lookup the existence of
     * !usercommand and if it exists and the requesting user has permission to edit the command, will swap the text
     * contents of the command with what is provided.
     * e.g. Existing command = !tickle I tickle you! -> !edit !tickle get tickled! -> !tickle get tickled!
     * @param command Container of command information
     */
    void editCommand(MessageChannel messageChannel, Member member,Command command) {
        if(member.hasPermission(Permission.ADMINISTRATOR)
                || member.hasPermission(Permission.BAN_MEMBERS)) {
            String keyCommand = commands.get(command.getKey());
            if(keyCommand!=null) {
                deleteCommand(messageChannel,member,command);
                addCommand(messageChannel,command);
            }
            else {
                messageHandler.sendMessage( "Command "+command.getKey()+" does not exist");
            }
        }
        else {
            messageHandler.sendMessage(
                    member.getUser().getName()+" doesn't have permission to do that :stuck_out_tongue_winking_eye: "
            );
        }

    }

    /**
     * Expects two arguments where the name of the second takes the place of the first. For instance !rename !aw_jeez !awjeez
     * will replace the name of the argument !aw_jeez with the new alias !awjeez.
     * @param messageChannel
     * @param command
     */
    void renameCommand(MessageChannel messageChannel, Member member, Command command) {
        String cmd=null;
        if(StringUtils.isNotEmpty(command.getKey())
        && StringUtils.isNotEmpty(command.getValue())
        && !StringUtils.containsWhitespace(command.getValue().trim())
        && (cmd = commands.get(command.getKey()))!=null) {
            deleteCommand(messageChannel, member, command);
            addCommand(messageChannel, new Command(command.getValue(), cmd));
            messageHandler.sendMessage( "Renamed "+command.getKey()+" to "+command.getValue());
        }
        else {
            messageHandler.sendMessage( "Could not rename command. It should follow this format: "
            + "[!rename !original !new].");
        }
    }

    /**
     * Selects a random user-created command and performs that action as if a user requested it.
     * @param messageChannel Discord message channel
     * @return A string representing a user requested available command
     */
    String randomCommand(MessageChannel messageChannel) {
        String[] keys = commands.keySet().toArray(new String[0]);
        String randomKey = keys[random.nextInt(keys.length)];
        messageHandler.sendMessage( "Preparing command: ".concat(randomKey));
        return randomKey;
    }
    void deleteCommand(MessageChannel messageChannel, Member member, Command command) {
        if(member.hasPermission(Permission.ADMINISTRATOR)
                || member.hasPermission(Permission.BAN_MEMBERS)) {
            commandStore.deleteCommand(command);
            commands.remove(command.getKey());
            messageHandler.sendMessage( command.getKey()+" has been deleted");
        }
        else {
            messageHandler.sendMessage( member.getUser().getName()+" doesn't have permission to do that :stuck_out_tongue_winking_eye: ");
        }

    }

    /**
     * Searches youtube using the provided text and plays from the resulting list.
     * @param command
     */
    void searchCommand(Command command, String apiKey) {
        String query = command.getValue();

        try {
            YouTube.Search.List search = youtube.search().list("id, snippet");
            search.setQ(query);
            search.setKey(apiKey);
            search.setType("video");
            search.setFields("items(id/videoId)");
            search.setMaxResults(5L);

            SearchListResponse searchListResponse = search.execute();

            //execute player command if we have search results
            if(searchListResponse!=null) {
                Command playerCommand = new Command(
                        ReservedCommand.play,
                        "!play",
                        YoutubeLinkBuilder.makeYoutubeLinkFromVideoId(
                                searchListResponse.getItems().get(0).getId().getVideoId())
                );
                playerCommands(null, playerCommand);
            }
            else {
                messageHandler.sendMessage("Could not find any search results with that");
            }

        } catch (IOException e) {
            logger.warn("Failed to query search term: "+query, e);
        }
    }
    /**
     * Check if user command contains a key and value. A key only reference will search a map of commands for an
     * existing command to execute. A key value reference will attempt to add the command using the value provided.
     * @param command The Command
     */
    void userCommand(MessageChannel messageChannel, Command command, Action reflexiveAction) {
        Objects.requireNonNull(command);
        if(StringUtils.isNotBlank(command.getKey())
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
            messageHandler.sendMessage( command.getValue());
    }

    /**
     * Determines a list of possible outcomes that the user was trying to do.
     * @param strCommand User attempted command string
     * @return A list of possible outcomes that the user was trying to perform
     */
    private List<String> suggestCommands(String strCommand) {
        return commands.entrySet()
                .stream()
                .filter(entry ->
                    specialStringsUtil.almostMatches(entry.getKey(),strCommand,2)
                )
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Groups a set of commands out of the root node and into some child node
     * @param messageChannel Discord Message Channel
     * @param member User who requested
     * @param command Command
     */
    void groupCommands(MessageChannel messageChannel, Member member, Command command) {
        messageHandler.sendMessage(
                String.format("Hello %s I don't know how to group using [%s] just yet.",
                member!=null ? member.getUser().getName() : "person",
                command.getValue())
        );
    }

    /**
     * Registers an event to perform an action when the event occurs
     * @param messageChannel Discord Message Channel
     * @param member User who requested
     * @param command Command
     */
    void notifyme(MessageChannel messageChannel, Member member, Command command) {
        //!notifyme berge gamefinished !player !play <link> !whyme
        messageHandler.sendMessage(
                String.format("Hello %s I don't know how to notify using [%s] just yet.",
                        member!=null ? member.getUser().getName() : "person",
                        command.getValue())
        );
    }
}
