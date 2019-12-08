package com.procyk.industries.command;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

public class Command implements Serializable {
    private ReservedCommand reservedCommand;
    private String key;
    private String value;
    private Map<String,String> optionalArgsToValue;
    public Command(ReservedCommand reservedCommand, Map<String,String> optionalArgsToValue, String key, String value) {
        this.reservedCommand=reservedCommand;
        this.key=key;
        this.value=value;
        this.optionalArgsToValue = optionalArgsToValue;
    }
    public Command(ReservedCommand reservedCommand, String key, String value) {
        this(reservedCommand,new HashMap<>(),key,value);
    }
    public Command(String key, String value) {
        this(ReservedCommand.none,key,value);
    }
    public Command(String key, String value, Map<String,String> optionalArgsToValue) {
        this(ReservedCommand.none, optionalArgsToValue, key, value);
    }
    public Command() {
        this(ReservedCommand.none,"","");
    }

    public void setOptionalArgsToValue(Map<String, String> optionalArgsToValue) {
        this.optionalArgsToValue = optionalArgsToValue;
    }

    public Map<String, String> getOptionalArgsToValue() {
        return optionalArgsToValue;
    }

    public String getOptionalArg(String key) {
        return optionalArgsToValue.get(key);
    }
    public boolean hasOptionalArgs() {
        return optionalArgsToValue.size()>0;
    }

    /**
     * Checks if the key value matches the name of a {@link ReservedCommand} and is not a {@link ReservedCommand#user}
     * command.
     * @return True if this command contains a reference to another command, otherwise false
     */
    public boolean isReflexive() {
        Matcher matcher = CommandParser.userCommandPattern.matcher(value);
        //noinspection CatchMayIgnoreException
        try {
            if(matcher.find()) {
                String command = matcher.group(CommandParser.capture_command);
                return ReservedCommand.valueOf(command.substring(1)).isNonUserCommand();
            }
        }
        catch(Exception e) {
        }
        return false;
    }

    public String getKey() {
        return key;
    }

    public ReservedCommand getReservedCommand() {
        return reservedCommand;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setReservedCommand(ReservedCommand reservedCommand) {
        this.reservedCommand = reservedCommand;
    }


    public String getValue() {
        return value;
    }


    public String setValue(String value) {
        return this.value=value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Command)) return false;
        Command command = (Command) o;
        return
                Objects.equals(getKey(), command.getKey()) &&
                Objects.equals(getValue(), command.getValue());// &&
    }

    @Override
    public int hashCode() {

        return Objects.hash(getReservedCommand(), getKey(), getValue(), getOptionalArgsToValue());
    }

    /**
     * String representation to store in file
     * @return A string representation of this object that is to be used for file storing and loading
     */
    public String getFormattedString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(value);
        optionalArgsToValue.forEach((k,v)->
            stringBuilder.append(" ").append(k).append("=").append(v)
        );
        return stringBuilder.toString();
    }
    @Override
    public String toString() {
        return String.format("[%s] [%s]=[%s] Opt=[%s]",reservedCommand,key,value,optionalArgsToValue.toString());
    }
}
