package com.procyk.industries.command;

import com.procyk.industries.data.CRUDable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class CommandStore {
    private static final Logger logger = LoggerFactory.getLogger(CommandStore.class);
    private final CRUDable crud;

    @Inject
    public CommandStore(CRUDable crud) {
        this.crud=crud;
    }

    public Map<String,String> getCommands() {
        return crud.getCommands().stream().collect(
                Collectors.toMap(Command::getKey, Command::getValue));
    }

    public void saveCommand(Command command) {
        Objects.requireNonNull(command);
        logger.info("Saving command: {}",command);
        crud.addCommand(command);
    }
    public void saveCommands(Map<String,String> commands) {
        logger.info("Saving commands: \n {}",commands);
        crud.saveAllCommands(commands);
    }
    public void deleteCommand(Command command) {
        Objects.requireNonNull(command);
        logger.info("Deleting command: {}", command);
        crud.removeCommand(command);
    }
}
