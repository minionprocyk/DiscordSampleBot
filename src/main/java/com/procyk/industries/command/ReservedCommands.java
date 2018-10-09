//package com.procyk.industries.command;
//
//import com.google.api.client.repackaged.com.google.common.base.Function;
//import net.dv8tion.jda.core.entities.*;
//import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
//
//import javax.inject.Named;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Builds a dispatcher to the correct command
// */
//public class ReservedCommands{
//    private final CommandExecutor commandExecutor;
//    private final String youtubeApi;
//    private final Map<ReservedCommand, JDAReservedCommandable> functionMap;
//
//    public ReservedCommands(CommandExecutor commandExecutor, @Named("youtube") String youtubeApi) {
//        this.commandExecutor = commandExecutor;
//        this.youtubeApi = youtubeApi;
//        functionMap = new HashMap<ReservedCommand,JDAReservedCommandable>(){{
//            put(ReservedCommand.add, (event, cmd, ctx) -> commandExecutor.addCommand(event.getChannel(),cmd));
//            put(ReservedCommand.user, (event, cmd, ctx) ->  {
//               Action reflexiveAction = (newCmd) -> {
//                   Message message = new Message(newCmd.getValue());
//                   ctx.performUserRequest(event, message);
//               } ;
//               commandExecutor.userCommand(event.getChannel(),cmd,reflexiveAction);
//            });
//            put(ReservedCommand.random, (event, cmd, ctx) ->  {
//                String randomCommand = commandExecutor.randomCommand(event.getChannel());
//                ctx.performUserRequest(event, new Message(randomCommand));
//            });
//            put(ReservedCommand.commands, (event, cmd, ctx) ->  commandExecutor.printCommands(event.getChannel()));
//            put(ReservedCommand.edit, (event, cmd, ctx) ->  commandExecutor.editCommand(
//                    event.getChannel(),
//                    event.getMember(),
//                    cmd
//            ));
//            put(ReservedCommand.delete, (event, cmd, ctx) ->  commandExecutor.deleteCommand(
//                    event.getChannel(),
//                    event.getMember(),
//                    cmd
//            ));
//            put(ReservedCommand.group, (event, cmd, ctx) ->  commandExecutor.groupCommands(
//                    event.getChannel(),
//                    event.getMember(),
//                    cmd
//            ));
//            put(ReservedCommand.rename, (event, cmd, ctx) ->  commandExecutor.renameCommand(
//                    event.getChannel(),
//                    event.getMember(),
//                    cmd
//            ));
//
//        }};
//    }
//
//    public JDAReservedCommandable getCommand(ReservedCommand reservedCommand) {
//        functionMap.get(reservedCommand);
//    }
//}
