package com.procyk.industries.command;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class CommandService {
    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);
    private final CommandExecutor commandExecutor;

    @Inject
    @Named("youtube")
    private String youtubeApi;

    @Inject
    public CommandService(CommandExecutor commandExecutor) {
        this.commandExecutor=commandExecutor;
    }

    /**
     * Takes a {@code Command} object and forwards it to the appropriate method provided by the {@code CommandExecutor}
     * @param reservedCommand A preset command built into the bot
     * @param command The user Command
     * @param event The event received by JDA
     */
    private void performReservedCommand(ReservedCommand reservedCommand, Command command, MessageReceivedEvent event) {
        MessageChannel messageChannel = event.getChannel();
        TextChannel textChannel = event.getTextChannel();
        Member member = event.getMember();
        Guild guild = event.getGuild();
        User author = event.getAuthor();

        logger.info(String.format("Performing Reserved Command: [%s] using Command Key: [%s] Value: [%s] Opt: [%s]",
                reservedCommand,command.getKey(),command.getValue(),command.getOptionalArgsToValue()));
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

        switch(reservedCommand) {
            case add:
                commandExecutor.addCommand(messageChannel,command);
                break;
            case user:
                //if user command is a reflexive command then perform that action instead of printing the string
                Action reflexiveAction = (cmd) -> { {
                        Message message = new Message(cmd.getValue());
                        performUserRequest(event,message);
                    }
                };
                commandExecutor.userCommand(messageChannel,command,reflexiveAction);
                break;
            case random:
                //forward a user-request with a random user-created command
                String randomCommand = commandExecutor.randomCommand(messageChannel);
                performUserRequest(event,new Message(randomCommand));
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
                commandExecutor.searchCommand(messageChannel, member, command, youtubeApi);
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
     * Performs a user request determined by a message string provided in the discord channel this instance is attached to.
     * For non bot authored messages, parse the command and execute a corresponding sequence of actions.
     * @param event Event signaling a message was received
     * @param message The Message
     */
    public void performUserRequest(MessageReceivedEvent event, Message message) {
        String messageContent = message.getContent().toString();
        CommandParser.parseCommands(messageContent)
                .forEach(command ->
                    performReservedCommand(command.getReservedCommand(),command,event)
                );
    }
}
