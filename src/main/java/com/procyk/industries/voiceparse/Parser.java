package com.procyk.industries.voiceparse;

import com.procyk.industries.command.Command;


public interface Parser {
    Command parse(String text);
}
