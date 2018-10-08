package com.procyk.industries.command;


public enum ReservedCommand {
    add ,edit,delete,group,leave,join,shutdown,commands,record,rename,notify,notifyme,player,random, user,test,none ;

    public enum PlayerCommands{
        commands,skip,next,pause,stop,last,previous,playlist,resume,add,clear,play,queue,seek,volume, localmusic,
        playlocal, end, error, repeat, random;
        public static PlayerCommands parse(String key) {
            return CommandParser.parsePlayerCommand(key);
        }
    }


    public  boolean isNonUserCommand() {
        return this.equals(none)==false && this.equals(user)==false;
    }
    public boolean isSingleLineCommand() {
        return this == add || this == delete || this == edit || this == join || this == leave ||
                this == commands || this == shutdown || this == group;
    }
}
