package com.procyk.industries.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservedCommandTest {

    @Test
    void isNonUserCommand() {

        assertAll(()-> {
            ReservedCommand.none.isNonUserCommand();
            ReservedCommand.user.isNonUserCommand();
            for (ReservedCommand reservedCommand : ReservedCommand.values()) {
                if(reservedCommand.equals(ReservedCommand.none) || reservedCommand.equals(ReservedCommand.user)) {
                    assertTrue(reservedCommand.isNonUserCommand()==false);
                }
                else {
                    assertTrue(reservedCommand.isNonUserCommand());
                }
            }
        });
    }

    @Test
    void parse() {
        int v=850;
        for(int i=0;i<50;i++) {
            System.out.print(" !player !playlocal "+(v+i));
        }
    }
}