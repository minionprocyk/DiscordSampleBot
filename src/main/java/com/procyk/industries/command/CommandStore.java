package com.procyk.industries.command;

import com.google.inject.name.Named;
import com.procyk.industries.data.CRUDable;
import com.procyk.industries.data.FirestoreCRUD;
import com.procyk.industries.data.SQLCRUD;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class CommandStore {
    private static final Logger logger = Logger.getLogger(CommandStore.class.getName());
    private static final Properties properties = new Properties();
    private final Path fileStorePath;
    private final CRUDable crud;

    @Inject
    public CommandStore(@Named("APP_PATH")Path appPath, @Named("commands_store")String fileName, FirestoreCRUD crud) {
        fileStorePath = appPath.resolve(fileName);
        this.crud=crud;
    }


    public Map<String,String> getCommands() {
        return crud.getCommands().stream().collect(
                Collectors.toMap(Command::getKey, Command::getValue));
    }

    public void saveCommand(Command command) {
        Objects.requireNonNull(command);
        crud.addCommand(command);
       // properties.put(command.getKey(),command.getFormattedString());
    }
    public void saveCommands(Map<String,String> commands) {
        crud.saveAllCommands(commands);
    }
    public void deleteCommand(Command command) {
        Objects.requireNonNull(command);
        crud.removeCommand(command);
    }
    public void persist() {
    }

}
