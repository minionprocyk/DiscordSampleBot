package com.procyk.industries.command;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.procyk.industries.module.CommandServiceModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandStoreTest {
    CommandStore commandStore;
    Map<String,String> commands;
    Command command = new Command("!unit_test_command_store","test value");
    @BeforeEach
    public void setup() {
        Injector injector = Guice.createInjector(new CommandServiceModule());
        commandStore = injector.getInstance(CommandStore.class);
        commands = Map.of(command.getKey(),command.getValue());
    }
    @AfterEach
    public void tearDown() {
        commandStore=null;
    }
    @Test
    public void testStore() {
        commandStore.saveCommands(commands);

        assertEquals(command.getValue(),commandStore.getCommands().get(command.getKey()),
                "Expected command to be added to command store file");
        commandStore.deleteCommand(command);
    }
}