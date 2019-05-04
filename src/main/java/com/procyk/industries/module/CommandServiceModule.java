package com.procyk.industries.module;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.procyk.industries.data.CRUDable;
import com.procyk.industries.data.FirestoreCRUD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Properties;

public class CommandServiceModule extends AbstractModule{
    private final Logger logger = LoggerFactory.getLogger(CommandServiceModule.class);
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
        return firestoreOptions.getService();
    }
    @Provides
    YouTube providesYoutube() {
        try {
            return new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) throws IOException {
                        }
            }).setApplicationName("youtube-discordsamplebot-cmdline")
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Failed to establish a connection to youtube",e);
        }
        return null;
    }

    @Override
    protected void configure() {
        bind(CRUDable.class).to(FirestoreCRUD.class).in(Scopes.SINGLETON);
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/token"));
        } catch (IOException e) {
            logger.error("Could not find youtube api key",e);
        }
        Names.bindProperties(binder(),properties);
    }
}
