package com.procyk.industries.command;

import com.google.inject.name.Named;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;

@Singleton
public class CommandStore {
    private static final Logger logger = Logger.getLogger(CommandStore.class.getName());
    private static final Properties properties = new Properties();
    private final Path fileStorePath;
    @Inject
    public CommandStore(@Named("APP_PATH")Path appPath, @Named("commands_store")String fileName) {
        fileStorePath = appPath.resolve(fileName);
    }
    public Map<String,String> getCommands() {
        Map<String,String> content;

        try {
            properties.load(Files.newInputStream(fileStorePath,READ));
        } catch (IOException e) {
            logger.warning("File not found "+fileStorePath.toString()+" but This should be ok...");
        }
        content = properties.stringPropertyNames()
                .stream()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> properties.get(name).toString()
                ));
        return content == null ? new HashMap<>() : content;
    }
    public void saveCommand(Command command) {
        Objects.requireNonNull(command);
        properties.put(command.getKey(),command.getFormattedString());
    }
    public void saveCommands(Map<String,String> commands) {
        Map<String,String> difference = new HashMap<>();
        Map<String,String> currentCommands = getCommands();
        difference.putAll(commands);
        difference.putAll(currentCommands);
        difference.entrySet().removeAll(currentCommands.entrySet());
        properties.putAll(difference);
    }
    public void deleteCommand(Command command) {
        Objects.requireNonNull(command);
        properties.remove(command.getKey());
    }
    public void persist() {
        try {
            Files.createDirectories(fileStorePath.getParent());
            properties.store(Files.newOutputStream(fileStorePath,CREATE),
                    "SYSTEM::Persisting Property Store");
        } catch (IOException e) {
            logger.log(Level.SEVERE,e.getMessage(),e);
        }
    }

}
