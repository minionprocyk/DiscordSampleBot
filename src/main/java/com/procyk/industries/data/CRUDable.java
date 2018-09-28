package com.procyk.industries.data;

import com.procyk.industries.command.Command;

import java.util.Map;
import java.util.Set;

public interface CRUDable {
    Set<Command> getCommands();
    void addCommand(Command command);
    void removeCommand(Command command);
    void saveAllCommands(Map<String,String> commands);
}
