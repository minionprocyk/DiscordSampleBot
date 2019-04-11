package com.procyk.industries.bot.event;
import com.procyk.industries.command.Action;
import com.procyk.industries.command.Command;
import com.procyk.industries.module.Application;

import java.util.concurrent.TimeUnit;

public class MemberEvent {
    private final String member;
    private final Action command;
    private long created;

    public MemberEvent(String member, final Action command) {
        this.member = member;
        this.command = command;
        this.created = System.currentTimeMillis();
    }
    public boolean isExpired() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()-created) >= Application.MEMBER_EVENT_TIMEOUT_IN_SECONDS;
    }
    public void fire(String data) {
        if(!isExpired())
            command.perform(new Command(member, data));
    }
}
