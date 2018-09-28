package com.procyk.industries.module;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    @Provides
    Firestore providesFirestore() throws IOException {
        InputStream serviceAccount = getClass().getResourceAsStream("/discordsamplebot-firebase-adminsdk.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setProjectId("discordsamplebot")
                .build();
        FirebaseApp.initializeApp(options);
        FirestoreOptions firestoreOptions =
                FirestoreOptions.newBuilder().setCredentials(credentials).setTimestampsInSnapshotsEnabled(true).build();
        Firestore firestore = firestoreOptions.getService();
        return firestore;
    }

    @Override
    protected void configure() {
    }

}
