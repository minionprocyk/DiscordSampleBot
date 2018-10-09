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
import com.google.firebase.cloud.FirestoreClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.apache.http.auth.AUTH;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Properties;

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
    @Provides @Named("youtube")
    String providesYoutubeAPIKey() {
        InputStream in = getClass().getResourceAsStream("/token");
        Properties properties = new Properties();
        String result="";
        try {
            properties.load(in);
            result = properties.getProperty("youtube");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    @Provides
    YouTube providesYoutube() {
        try {
            YouTube youTube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) throws IOException {
                        }
            }).setApplicationName("youtube-discordsamplebot-cmdline")
                    .build();
            return youTube;
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void configure() {
    }

}
