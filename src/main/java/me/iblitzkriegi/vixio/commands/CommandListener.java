package me.iblitzkriegi.vixio.commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandListener extends ListenerAdapter {

    private final Pattern commandPattern = Pattern.compile("(\\S+)(\\s+(.+))?");

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) {
            return;
        }

        String content = e.getMessage().getContentRaw();
        Matcher m = commandPattern.matcher(content);
        if (!m.matches()) {
            return;
        }

        String commandLabel = m.group(1);
        String args = m.group(3);

        for (DiscordCommand command : DiscordCommandFactory.getInstance().getCommands()) {

            for (String prefix : command.getPrefixes()) {

                for (String alias : command.getUsableAliases()) {

                    if (commandLabel.equalsIgnoreCase(prefix + alias)) {

                        command.execute(prefix, alias, args, e.getGuild(), e.getChannel(), e.getMessage(),
                                e.getAuthor(), e.getMember(), e.getJDA());
                        return;

                    }

                }

            }

        }
    }

}