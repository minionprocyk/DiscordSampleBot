package com.procyk.industries.voiceparse;

import com.procyk.industries.DiscoLangLexer;
import com.procyk.industries.DiscoLangParser;
import com.procyk.industries.command.Command;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;


public class DiscoLangVoiceCommandParser implements Parser {

    @Override
    public Command parse(String text) {
        if(!text.contains("music tree"))
            return null;
        text = text.substring(text.indexOf("music tree"));
        CharStream charStream = CharStreams.fromString(text);
        DiscoLangLexer lexer = new DiscoLangLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        DiscoLangParser parser = new DiscoLangParser(tokens);

        DiscoLangVoiceRootListener rootListener = new DiscoLangVoiceRootListener();
        parser.root().enterRule(rootListener);
        return rootListener.getDiscoLangVoiceCommandListener().getCommand();
    }
}
