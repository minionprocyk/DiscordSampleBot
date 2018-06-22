package com.procyk.industries.command;

public class Message {
    private final StringBuilder content;
//    private final long created;
    public Message(String content) {
        this.content= new StringBuilder(content);
//        created = System.currentTimeMillis();
    }
    public StringBuilder getContent() {
        return this.content;
    }
}
