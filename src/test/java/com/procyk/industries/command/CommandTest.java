package com.procyk.industries.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    @Test
    void isReflexiveWithAdditionalParams() {
        String key = "!linkme";
        String reflexiveCommand = "!player !play link.html";
        Command command = new Command(key,reflexiveCommand);
        assertTrue(command.isReflexive(),"Expected "+reflexiveCommand+" to be reflexive due to !player being reserved");
    }
    @Test
    void isNotReflexiveWithAdditionalParams() {
        String key = "!linkme";
        String reflexiveCommand = "!learner !learn link.html";
        Command command = new Command(key,reflexiveCommand);
        assertTrue(command.isReflexive()==false,"Expected "+reflexiveCommand+" to not contain ReservedCommand");
    }
    @Test
    void isReflexiveWithSingleWordCommand() {
        String key = "!linkme";
        String reflexiveCommand = "!join";
        Command command = new Command(key,reflexiveCommand);
        assertTrue(command.isReflexive(),"Expected "+reflexiveCommand+" to be reflexive due to !join being reserved");
    }
    @Test
    void isNotReflexiveWithSingleWordCommand() {
        String key = "!linkme";
        String reflexiveCommand = "!telephone";
        Command command = new Command(key,reflexiveCommand);
        assertTrue(command.isReflexive()==false,"Expected "+reflexiveCommand+" to not contain ReservedCommand");
    }
    @Test
    void testMultiStepReflexiveCommand() {
        String key = "!linkme";
        String reflexiveCommand = "!player !volume 50 !player !play youtube.netfast";
        Command command = new Command(key,reflexiveCommand);
        assertTrue(command.isReflexive(),"Expected "+reflexiveCommand+" to contain ReservedCommand");
    }
}