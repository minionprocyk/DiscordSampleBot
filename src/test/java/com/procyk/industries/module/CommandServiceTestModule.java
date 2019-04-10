package com.procyk.industries.module;

import com.google.api.services.youtube.YouTube;
import com.google.cloud.firestore.Firestore;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.procyk.industries.command.CommandStore;
import com.procyk.industries.data.CRUDable;
import com.procyk.industries.data.FirestoreCRUD;

import javax.inject.Named;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandServiceTestModule extends AbstractModule {
@Provides
    Firestore providesFirestore() {
        return mock(Firestore.class);
    }
    @Provides @Named("youtube") String providesYoutubeApiString() {
    return "";
    }
    @Provides @com.google.inject.name.Named("commands_store") String providesCommandsStoreFileName() {
        return "commands.data";
    }
    @Provides @com.google.inject.name.Named("APP_PATH")
    Path providesAPPPath(){
        return Paths.get(System.getProperty("user.home")).resolve("SampleDiscord");
    }
    @Provides
    YouTube providesYoutube() {
        return mock(YouTube.class);
    }

    @Provides
    CommandStore providesCommandStore() {
        HashMap<String,String> commands = new HashMap<>();
        commands.put("!test","test");
        CommandStore commandStore = mock(CommandStore.class);
        when(commandStore.getCommands()).thenReturn(commands);
        return commandStore;
    }
    @Override
    protected void configure() {
        bind(CRUDable.class).to(FirestoreCRUD.class).in(Scopes.SINGLETON);
    }
}