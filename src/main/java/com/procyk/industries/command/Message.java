package com.procyk.industries.command;

public class Message {
    private final StringBuilder content;
    public Message(String content) {
        this.content= new StringBuilder(content);
    }
    public StringBuilder getContent() {
        return this.content;
    }
}
