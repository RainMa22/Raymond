package me.rainma22.Raymond.Commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.function.BiConsumer;

public interface iCommand extends BiConsumer<MessageReceivedEvent, String[]> {
    @Override
    default void accept(MessageReceivedEvent event, String[] cmds) {
        if (cmds[0].toLowerCase().contains("everything")) {
            event.getMessage().reply("https://tenor.com/view/" +
                    "everything-everywhere-all-at-once-daniel-kwan-daniel-scheinert-quote-life-gif-17047606721800614267").queue();
        } else {
            event.getChannel().sendMessage("No Such Command: " + cmds[0]).queue();
        }
    }
}
