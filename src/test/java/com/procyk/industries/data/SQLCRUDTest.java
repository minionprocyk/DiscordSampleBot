package com.procyk.industries.data;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.procyk.industries.command.Command;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertTrue;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class SQLCRUDTest {
    @Inject
    SQLCRUD SQLCRUD;

    @BeforeAll
    void setup() {
        Guice.createInjector(new BotModule(), new CommandServiceModule(), new AudioServiceModule()).injectMembers(this);

    }
    @Test
    void testGetCommands() {
        assertTrue(SQLCRUD.getCommands()!=null);
    }
    @Test
    void testConnect() {
        SQLCRUD.createCommandsTable();
        SQLCRUD.addCommand(new Command("test","me"));
        SQLCRUD.removeCommand(new Command("test","me"));
    }
}
