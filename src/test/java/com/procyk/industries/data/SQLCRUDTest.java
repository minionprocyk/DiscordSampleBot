package com.procyk.industries.data;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.procyk.industries.command.Command;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SQLCRUDTest {
    @Inject
    SQLCRUD SQLCRUD;

    @BeforeEach
    void setup() {
        Guice.createInjector(new BotModule(), new CommandServiceModule(), new AudioServiceModule()).injectMembers(this);

    }
    @Test
    void testGetCommands() {
        assertTrue(SQLCRUD.getCommands().size()>0);
    }
    @Test
    void testConnect() {
        SQLCRUD.connect();
        SQLCRUD.createCommandsTable();
        SQLCRUD.addCommand(new Command("test","me"));
        SQLCRUD.removeCommand(new Command("test","me"));
        System.out.println(SQLCRUD.getCommands());

    }
}
