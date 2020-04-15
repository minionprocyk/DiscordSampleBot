package com.procyk.industries.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.inject.Inject;
import com.procyk.industries.command.Command;
import com.procyk.industries.strings.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class FirestoreCRUD implements CRUDable {
    private final Logger logger = LoggerFactory.getLogger(FirestoreCRUD.class);
    private final Firestore db;
    private static final String COMMANDS_COLLECTION = "commands";

    @Inject
    public FirestoreCRUD(Firestore firestore) {
        this.db = firestore;
    }
    @Override
    public Set<Command> getCommands() {
        try {
            return db.collection(COMMANDS_COLLECTION).get().get().getDocuments().stream()
                    .map(queryDocumentSnapshot ->
                            new Command(
                                    queryDocumentSnapshot.getString("name"),
                                    queryDocumentSnapshot.getString("value"))
                    )
                    .collect(Collectors.toSet());
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to get commands", e);
            Thread.currentThread().interrupt();
        }
        return Collections.emptySet();
    }

    @Override
    public void addCommand(Command command) {
        if(null!=command && Strings.isNotBlank(command.getKey()) && Strings.isNotBlank(command.getValue()))
        {
            Map<String,String> fields = new HashMap<>();
            fields.put("name",command.getKey());
            fields.put("value",command.getValue());

            ApiFuture<WriteResult> future = db.collection(COMMANDS_COLLECTION).document(command.getKey()).create(fields);
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Command: {} may already exist {}", command,e);
                Thread.currentThread().interrupt();
            }
        }
        else {
            String cmdString = (null!=command ? command.toString() : "EMPTY_COMMAND");
            logger.info("Not adding command: {}",cmdString);
        }
    }

    @Override
    public void removeCommand(Command command) {
        logger.info("Removing {}", command);
        try {
            db.collection(COMMANDS_COLLECTION).document(command.getKey()).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to delete command {}", command);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void saveAllCommands(Map<String, String> commands) {
        logger.info("Saving commands: {}", commands);
        commands.forEach((k,v) -> addCommand(new Command(k,v)));
    }
}
