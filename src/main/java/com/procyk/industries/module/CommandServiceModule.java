package com.procyk.industries.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandServiceModule extends AbstractModule{
    @Inject
    public CommandServiceModule() {
    }

    @Provides @Named("commands_store") String providesCommandsStoreFileName() {
        return "commands.data";
    }
    @Provides @Named("APP_PATH") Path providesAPPPath(){
        return Paths.get(System.getProperty("user.home")).resolve("SampleDiscord");
    }

    @Override
    protected void configure() {
    }

}
