package com.procyk.industries.module;


import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.auth.oauth2.GoogleCredentials;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
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
    @Provides @DBName
    String providesCommandsStoreFileName() {
        return "commands.data";
    }
    @Provides @ApplicationPath
    Path providesAPPPath(){
        return Paths.get(System.getProperty("user.home")).resolve("SampleDiscord");
    }
    @Provides @DeepspeechModelsPath
    Path providesDeepspeechModelsPath(@ApplicationPath Path appPath) {
        return appPath.resolve("lang");
    }
    @Provides
    Firestore providesFirestore() throws IOException {
        InputStream serviceAccount = getClass().getResourceAsStream("/discordsamplebot-firebase-adminsdk.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions firestoreOptions = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setProjectId("discordsamplebot")
                .build();
        FirebaseApp.initializeApp(firestoreOptions);
        return FirestoreClient.getFirestore();
    }
    @Provides
    YouTube providesYoutube() {
        try {
            return new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    request -> {
                    }).setApplicationName("youtube-discordsamplebot-cmdline")
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Failed to establish a connection to youtube",e);
        }
        return null;
    }
    @Provides @JDAToken
    String providesJDBCToken(Properties properties) {
        return properties.getProperty("token");
    }
    @Provides @YouTubeToken
    String providesYouTubeApiToken(Properties properties) {
        return properties.getProperty("youtube");
    }
    @Provides
    Properties providesApplicationProperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/token"));
        } catch (IOException e) {
            logger.error("Could not load application properties",e);
        }
        return properties;
    }

    @Override
    protected void configure() {
        bind(CRUDable.class).to(FirestoreCRUD.class).in(Scopes.SINGLETON);
    }
}
