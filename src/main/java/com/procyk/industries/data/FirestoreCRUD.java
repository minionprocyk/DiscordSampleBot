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
            logger.severe(e.getMessage());
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        else {
            logger.info("Not adding command: "+(null!=command ? command.toString() : "EMPTY_COMMAND"));
        }
    }

    @Override
    public void removeCommand(Command command) {
        db.collection("commands").document(command.getKey()).delete();
    }

    @Override
    public void saveAllCommands(Map<String, String> commands) {
        commands.forEach((k,v) -> {
            addCommand(new Command(k,v));
        });
    }
}
