package com.procyk.industries.sql;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.procyk.industries.command.Command;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CRUDTest {
    @Inject
    CRUD crud;

    @BeforeEach
    void setup() {
        Guice.createInjector(new BotModule(), new CommandServiceModule(), new AudioServiceModule()).injectMembers(this);

    }
    @Test
    void testGetCommands() {
        assertTrue(crud.getCommands().size()>0);
    }
    @Test
    void testConnect() {
        crud.connect();
        crud.createCommandsTable();
        crud.addCommand(new Command("test","me"));
        crud.removeCommand(new Command("test","me"));
        System.out.println(crud.getCommands());

    }
}
