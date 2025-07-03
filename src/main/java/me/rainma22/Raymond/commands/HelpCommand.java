package me.rainma22.Raymond.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;
import java.util.StringJoiner;

public class HelpCommand implements iCommand{
    private Map<String, iCommand> supportedCommands;

    public HelpCommand(Map<String, iCommand> supportedCommands){
        setSupportedCommands(supportedCommands);
    }

    @Override
    public void accept(MessageReceivedEvent event, String[] cmds) {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add("List of available commands:");
        joiner.add("");
        for (Map.Entry<String, iCommand> entry : supportedCommands.entrySet()){
            joiner.add(" - "+entry.getKey() + ": ");
            joiner.add("\t > " + entry.getValue().getDescription("\n\t > "));
        }
        event.getChannel().asTextChannel().sendMessage(joiner.toString()).queue();
    }

    @Override
    public String getDescription(String separator) {
        return "Display this help message.";
    }

    public Map<String, iCommand> getSupportedCommands() {
        return supportedCommands;
    }

    public void setSupportedCommands(Map<String, iCommand> supportedCommands) {
        this.supportedCommands = supportedCommands;
    }
}
