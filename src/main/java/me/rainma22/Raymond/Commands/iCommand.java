package me.rainma22.Raymond.Commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.function.BiConsumer;

public interface iCommand extends BiConsumer<MessageReceivedEvent, String[]> {
    @Override
    default void accept(MessageReceivedEvent event, String[] commands) {
        event.getChannel().sendMessage("No Such Command: " + commands[0]).queue();
    }
}
