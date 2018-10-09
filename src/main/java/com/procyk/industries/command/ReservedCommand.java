package com.procyk.industries.command;


public enum ReservedCommand {
    add ,edit,delete,group,leave,join,shutdown,commands,record,rename,search,play,notify,notifyme,player,random, user,test,none ;

    public enum PlayerCommands{
        commands,skip,next,pause,stop,last,previous,playlist,resume,add,clear,play,queue,seek,volume, localmusic,
        playlocal, end, error, repeat, random;
        public static PlayerCommands parse(String key) {
            return CommandParser.parsePlayerCommand(key);
        }
    }


    /**
     * A command that is a reserved command created in this code.
     * @return
     */
    public  boolean isNonUserCommand() {
        return this.equals(none)==false && this.equals(user)==false;
    }

    /**
     * A single line command parses the whole string as one command after the reserved keyword name
     * @return
     */
    public boolean isSingleLineCommand() {
        return this == add || this == delete || this == edit || this == join || this == leave ||
                this == commands || this == shutdown || this == group || this == rename;
    }

    /**
     * A Single line command that must be parsed with the reserved command as part of the string
     * @return
     */
    public boolean isFullLineCommand() {
        return this == play || this == search;
    }
}
