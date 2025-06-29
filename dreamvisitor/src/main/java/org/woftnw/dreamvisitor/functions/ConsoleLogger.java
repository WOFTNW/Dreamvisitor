package org.woftnw.dreamvisitor.functions;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.jetbrains.annotations.NotNull;

public class ConsoleLogger extends AbstractAppender {

    public ConsoleLogger() {
        super("MyLogAppender", null, null, false, null);
        start();
    }

    @Override
    public void append(@NotNull LogEvent event) {
        // if you don't make it immutable, then you may have some unexpected behaviors
        LogEvent log = event.toImmutable();

        StringBuilder builder = new StringBuilder(log.getMessage().getFormattedMessage());

        if (log.getThrown() != null) {
            builder.append("\n").append(log.getThrown().getMessage());
            for (StackTraceElement stackTraceElement : log.getThrown().getStackTrace()) builder.append("\n").append(stackTraceElement.toString());
        }

        String message = builder.toString();

        // TODO: Send message.

    }

}
