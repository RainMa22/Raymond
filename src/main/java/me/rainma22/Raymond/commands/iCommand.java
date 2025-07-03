package me.rainma22.Raymond.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.function.BiConsumer;

public interface iCommand extends BiConsumer<MessageReceivedEvent, String[]> {
    @Override
    default void accept(MessageReceivedEvent event, String[] cmds) {
            event.getChannel().sendMessage("No Such Command: " + cmds[0]).queue();
    }
    default String getDescription(){
        return getDescription(System.lineSeparator());
    }
    default String getDescription(String separator){
        return "This command does not have a description yet.";
    }
}
