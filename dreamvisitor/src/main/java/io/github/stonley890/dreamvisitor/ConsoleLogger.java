package io.github.stonley890.dreamvisitor;

import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Formatter;
// other imports that you need here

public class ConsoleLogger extends AbstractAppender {

    public static StringBuilder messageBuilder = new StringBuilder();
    public static List<String> overFlowMessages = new ArrayList<>();

    public ConsoleLogger() {
        // do your calculations here before starting to capture
        super("MyLogAppender", null, null);
        start();
    }

    protected ConsoleLogger(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout);
    }

    @Override
    public void append(LogEvent event) {
        // if you don`t make it immutable, then you may have some unexpected behaviours
        LogEvent log = event.toImmutable();

        String message = log.getMessage().getFormattedMessage();

        // and you can construct your whole log message like this:
        message = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " " + event.getLevel().toString() + "] " + message;

        // Truncate messages over 2000 characters
        if (message.length() >= 2000) {
            String tooLongMessage = "**This message was too long! Here is the shorter version:**\n";
            message = message.substring(0, 1999 - tooLongMessage.length());
        }

        // Pause adding strings if new message will be > 2000
        if (messageBuilder.length() + message.length() + "\n".length() <= 2000) {

            if (messageBuilder.length() != 0) {
                messageBuilder.append("\n");
            }
            messageBuilder.append(message);

        } else {
            overFlowMessages.add(message);
        }


    }

}
