package me.rainma22.Raymond.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.function.BiConsumer;

public interface iCommand extends BiConsumer<MessageReceivedEvent, String[]> {
    @Override
    default void accept(MessageReceivedEvent event, String[] cmds) {
        if (cmds[0].toLowerCase().contains("everything")) {
            event.getMessage().reply("https://tenor.com/view/404-not-found-error-20th-century-fox-gif-24907780").queue();
        } else {
            event.getChannel().sendMessage("No Such Command: " + cmds[0]).queue();
        }
    }
}
