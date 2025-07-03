package me.rainma22.Raymond.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EverythingCommand implements iCommand{
    public void accept(MessageReceivedEvent event, String[] cmds){
        if (cmds[0].toLowerCase().contains("everything")) {
            event.getMessage().reply("https://tenor.com/view/404-not-found-error-20th-century-fox-gif-24907780").queue();
        }
    }

    @Override
    public String getDescription(String separator) {
        return "What does this do?";
    }
}
