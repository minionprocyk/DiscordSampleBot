package com.procyk.industries.voiceparse;

import com.procyk.industries.DiscoLangBaseListener;
import com.procyk.industries.DiscoLangParser;
import com.procyk.industries.command.Command;
import com.procyk.industries.command.ReservedCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DiscoLangVoiceCommandListener extends DiscoLangBaseListener {
    private Command command;

    @Override
    public void enterPlayer(DiscoLangParser.PlayerContext ctx) {
        String value = ctx.WORD().stream()
                .map(terminalNode -> terminalNode.getSymbol().getText())
                .collect(Collectors.joining(" "));
        command = new Command(ReservedCommand.search, "", value);
    }

    private static final Map<String,String> directionToOperation = new HashMap<>()
    {{
        put("UP","+");
        put("INCREASE","+");
        put("DOWN","-");
        put("DECREASE","-");

    }};
    @Override
    public void enterVolume(DiscoLangParser.VolumeContext ctx) {
        String nextWord = ctx.getChild(1).getText();
        String operand = directionToOperation.get(nextWord.toUpperCase());
        if (operand != null) {
            command = new Command(ReservedCommand.player,"!volume","");
            switch (operand) {
                case "+":
                    command.setValue("+20");
                    break;
                case "-":
                    command.setValue("-20");
                    break;
                default:
                    command=null;
            }
        }
    }

    @Override
    public void enterRandom(DiscoLangParser.RandomContext ctx) {
        command = new Command(ReservedCommand.random,"!random","");
    }

    @Override
    public void enterStop(DiscoLangParser.StopContext ctx) {
       command = new Command(ReservedCommand.player, "!stop","");
    }

    public Command getCommand() {
        return command;
    }
}
