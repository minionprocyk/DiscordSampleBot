package com.procyk.industries.data;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.procyk.industries.module.AudioServiceModule;
import com.procyk.industries.module.BotModule;
import com.procyk.industries.module.CommandServiceModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FirestoreCRUDTest {

    @Inject
    FirestoreCRUD firestoreCRUD;

    @Inject
    SQLCRUD SQLCRUD;
    @BeforeEach
    void setup() {
        Guice.createInjector(new BotModule(), new CommandServiceModule(), new AudioServiceModule()).injectMembers(this);

    }
    //OK - 9/27/2018
//    @Test
//    void testFirestoreGetCommands() {
//       Set<Command> commands =  firestoreCRUD.getCommands();
//       assertTrue(commands.size()>0);
//    }
//    @Test
//    void testSingleCommandAddRemoveGet() {
//        String key="!atrocious" , value = "!player !play youtube.link";
//        Command newCommand = new Command(key,value);
//        firestoreCRUD.addCommand(newCommand);
//        assertTrue(firestoreCRUD.getCommands().contains(newCommand));
//        firestoreCRUD.removeCommand(newCommand);
//        assertTrue(firestoreCRUD.getCommands().contains(newCommand)==false);
//    }
//    @Test
//    void testSaveAllCommands() {
//        String k1 = "sampleCommand", k2="sampleCommand2", v1="some sample of a command", v2="some sample of a second command";
//        Map<String,String> someMoreCommands = new HashMap<String,String>() {{
//            put(k1,v1);
//            put(k2,v2);
//        }};
//        firestoreCRUD.saveAllCommands(someMoreCommands);
//        assertTrue(firestoreCRUD.getCommands().contains(new Command(k1,v1)));
//        assertTrue(firestoreCRUD.getCommands().contains(new Command(k2,v2)));
//
//        firestoreCRUD.removeCommand(new Command(k1,v1));
//        firestoreCRUD.removeCommand(new Command(k2,v2));
//
//    }


    //used to import all commands 9/27/2018
//    @Test
//    void testGetAllCommandsFromDbIntoFirestore() {
//        SQLCRUD.getCommands().forEach(cmd-> {
//            System.out.println(cmd.getKey()+" "+cmd.getValue());
//            firestoreCRUD.addCommand(cmd);
//        });
//    }
}
