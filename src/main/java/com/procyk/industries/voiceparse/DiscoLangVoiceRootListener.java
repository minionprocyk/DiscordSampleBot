package com.procyk.industries.voiceparse;

import com.procyk.industries.DiscoLangBaseListener;
import com.procyk.industries.DiscoLangParser;

public class DiscoLangVoiceRootListener extends DiscoLangBaseListener {
    private final DiscoLangVoiceCommandListener discoLangVoiceCommandListener = new DiscoLangVoiceCommandListener();
    @Override
    public void enterRoot(DiscoLangParser.RootContext ctx) {
        ctx.command().enterRule(discoLangVoiceCommandListener);
    }

    @Override
    public void exitRoot(DiscoLangParser.RootContext ctx) {
        super.exitRoot(ctx);
    }
    public DiscoLangVoiceCommandListener getDiscoLangVoiceCommandListener() {
        return this.discoLangVoiceCommandListener;
    }
}
