package com.procyk.industries.command;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandStoreTest {
    private CommandStore commandStore;
    private Map<String,String> commands;
    private final Command command = new Command("!unit_test_command_store","test value");
    @BeforeEach
    void setup() {
        Injector injector = Guice.createInjector(new BotModule(), new AudioServiceModule(), new CommandServiceModule());
        commandStore = injector.getInstance(CommandStore.class);
        commands = commandStore.getCommands();
        commands.put(command.getKey(), command.getValue());
    }
    @AfterEach
    void tearDown() {
        commandStore=null;
    }
    @Test
    void testStore() {
        commandStore.saveCommands(commands);

        assertEquals(command.getValue(),commandStore.getCommands().get(command.getKey()),
                "Expected command to be added to command store file");
        commandStore.deleteCommand(command);
    }

//
//    @Test
//    void testAllCommands() {
//        System.out.println("insert into commands(name, value) values ");
//        commandStore.getCommands().forEach((k,v)-> {
//            System.out.print("(\""+k.toString()+"\", \""+v.toString()+"\"), ");
//        });
//        System.out.println(");");
//    }


}