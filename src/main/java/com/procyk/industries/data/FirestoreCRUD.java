package com.procyk.industries.data;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.procyk.industries.command.Command;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FirestoreCRUD implements CRUDable {
    private final Logger logger = Logger.getLogger(FirestoreCRUD.class.getName());
    private final Firestore db;

    @Inject
    public FirestoreCRUD(Firestore firestore) {
        this.db = firestore;
    }
    @Override
    public Set<Command> getCommands() {
        try {
            return db.collection("commands").get().get().getDocuments().stream()
                    .map(queryDocumentSnapshot ->
                            new Command(
                                    queryDocumentSnapshot.getString("name"),
                                    queryDocumentSnapshot.getString("value"))
                    )
                    .collect(Collectors.toSet());
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Failed to get commands", e);
        }
        return null;
    }

    @Override
    public void addCommand(Command command) {
        if(null!=command && StringUtils.isNotBlank(command.getKey()) && StringUtils.isNotBlank(command.getValue()))
        {
            Map<String,String> fields = new HashMap<String,String>() {{
                put("name",command.getKey());
                put("value",command.getValue());
            }};
            ApiFuture<WriteResult> future = db.collection("commands").document(command.getKey()).create(fields);
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.log(Level.WARNING,"Command: "+command.toString()+" may already exist",e);
            }
        }
        else {
            logger.info("Not adding command: "+(null!=command ? command.toString() : "EMPTY_COMMAND"));
        }
    }

    @Override
    public void removeCommand(Command command) {
        logger.info("Removing "+command.toString());
        try {
            db.collection("commands").document(command.getKey()).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.WARNING, "Failed to delete command "+command.toString());
        }
    }

    @Override
    public void saveAllCommands(Map<String, String> commands) {
        logger.info("Saving commands: " +commands.toString());
        commands.forEach((k,v) -> {
            addCommand(new Command(k,v));
        });
    }
}
