package com.procyk.industries.command;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class CommandService {
    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);
    private final CommandExecutor commandExecutor;
    private final String youtubeApi;
    private final JDA jda;

    @Inject
    public CommandService(CommandExecutor commandExecutor, @Named("youtube") String youtubeApi, JDA jda) {
        this.commandExecutor=commandExecutor;
        this.youtubeApi=youtubeApi;
        this.jda=jda;
    }


    public void performCustomJDAEvent(String strCommand, User user) {
        Optional<TextChannel> optionalTextChannel= jda.getTextChannels().stream()
        .filter(TextChannel::canTalk)
        .filter(channel-> channel.getName().equalsIgnoreCase("General"))
        .findFirst();

        TextChannel textChannel;
        if(optionalTextChannel.isPresent()) {
            textChannel = optionalTextChannel.get();
            strCommand = strCommand.toUpperCase();
            if(strCommand.contains("VOLUME") && strCommand.contains("UP")) {
                Command command = new Command("!volume","+20");
                commandExecutor.playerCommands(textChannel, command);
            }
            else if(strCommand.contains("VOLUME") && strCommand.contains("DOWN")) {
                Command command = new Command("!volume","-20");
                commandExecutor.playerCommands(textChannel,command);
            }
            else if(strCommand.startsWith("SEARCH FOR")) {
                Command command = new Command("!search",strCommand.substring(strCommand.indexOf("SEARCH")));
                commandExecutor.searchCommand(command,youtubeApi);
            }
            //todo cannot do shutdown without verifying the user via a Member object
//            else if(strCommand.contains("COMMENCE") && strCommand.contains("SHUTDOWN")) {
//                Command command = new Command("!shutdown","");
//            }
            else {
                logger.info("Unsupported custom command: "+strCommand);
            }

        }

    }
    /**
     * Takes a {@code Command} object and forwards it to the appropriate method provided by the {@code CommandExecutor}
     * @param reservedCommand A preset command built into the bot
     * @param command The user Command
     */
    private void performReservedCommand(ReservedCommand reservedCommand, Command command,
                                        MessageChannel messageChannel, Member member,
                                        TextChannel textChannel, Guild guild, User author) {
        switch(reservedCommand) {
            case add:
                commandExecutor.addCommand(messageChannel,command);
                break;
            case user:
                //if user command is a reflexive command then perform that action instead of printing the string
                Action reflexiveAction = (cmd) -> { {
                    performAction(
                            cmd.getValue(),
                            author,
                            messageChannel,
                            member,
                            textChannel,
                            guild);
                    }
                };
                commandExecutor.userCommand(messageChannel,command,reflexiveAction);
                break;
            case random:
                //forward a user-request with a random user-created command
                String randomCommand = commandExecutor.randomCommand(messageChannel);
                performAction(
                        randomCommand,
                        author,
                        messageChannel,
                        member,
                        textChannel,
                        guild);
                break;
            case commands:
                commandExecutor.printCommands(messageChannel);
                break;
            case edit:
                commandExecutor.editCommand(messageChannel,member,command);
                break;
            case delete:
                commandExecutor.deleteCommand(messageChannel,member,command);
                break;
            case group:
                commandExecutor.groupCommands(messageChannel, member, command);
                break;
            case record:
                break;
            case rename:
                commandExecutor.renameCommand(messageChannel, member, command);
                break;
            case notify:
            case notifyme:
                commandExecutor.notifyme(messageChannel,member,command);
                break;
            case play:
                command.setReservedCommand(ReservedCommand.player);
                command.setKey("!"+ReservedCommand.PlayerCommands.play.name());
            case player:
                commandExecutor.playerCommands(messageChannel, command);
                break;
            case search:
                commandExecutor.searchCommand(command, youtubeApi);
                break;
            case join:
                commandExecutor.joinVoiceChannel(messageChannel, textChannel, member, guild);
                break;
            case leave:
                commandExecutor.leaveVoiceChannel(guild);
                break;
            case shutdown:
               commandExecutor.shutdown(messageChannel, member);
                break;
            case test:
                break;
            case none:

            default:
        }
    }

    /**
     * Accepts all required JDA elements and a parsable string to perform commands
     * @param content
     * @param author
     * @param messageChannel
     * @param member
     * @param textChannel
     * @param guild
     */
    public void performAction(String content, User author,
                        MessageChannel messageChannel, Member member,
                              TextChannel textChannel, Guild guild) {
        CommandParser.parseCommands(content)
                .forEach(command -> {
                    logger.info(String.format("Performing Reserved Command: [%s] using Command Key: [%s] Value: [%s] Opt: [%s]",
                            command.getReservedCommand(),command.getKey(),command.getValue(),command.getOptionalArgsToValue()));
                    if(member!=null)
                        logger.info(String.format("For User: " +
                                        "AuthorName: %s " +
                                        "MemberNickName: %s " +
                                        "MemberName: %s " +
                                        "Playing: %s " +
                                        "With Roles: %s",
                                author.getName(),
                                member.getNickname(),
                                member.getEffectiveName(),
                                member.getGame()==null ? "Nothing" : member.getGame().getName(),
                                member.getRoles().toString()));

                    performReservedCommand(command.getReservedCommand(),command,
                            messageChannel,member,textChannel,guild,author);
                });
    }
    /**
     * Performs a user request determined by a message string provided in the discord channel this instance is attached to.
     * For non bot authored messages, parse the command and execute a corresponding sequence of actions.
     * @param event Event signaling a message was received
     * @param message The Message
     */
    public void performUserRequest(MessageReceivedEvent event, Message message) {
        //handle reserved commands
        if(event.getAuthor().isBot())return;
        String messageContent = message.getContent().toString();

        MessageChannel messageChannel = event.getChannel();
        TextChannel textChannel = event.getTextChannel();
        Member member = event.getMember();
        Guild guild = event.getGuild();
        User author = event.getAuthor();

        performAction(messageContent, author, messageChannel, member, textChannel, guild);
    }
}
