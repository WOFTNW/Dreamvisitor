package org.woftnw.dreamvisitor.commands;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.woftnw.dreamvisitor.data.Tribe;

import java.util.Arrays;

public class CommandUtils {

    public static Argument<Tribe> customTribeArgument(String nodeName) {

        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            Tribe tribe;
            try {
                tribe = Tribe.valueOf(info.input().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw CustomArgument.CustomArgumentException.fromMessageBuilder(new CustomArgument.MessageBuilder("Unknown tribe: ").appendArgInput());
            }
            return tribe;
        }).replaceSuggestions(ArgumentSuggestions.strings(info -> Arrays.stream(Tribe.values()).map(tribe -> tribe.name().toUpperCase()).toArray(String[]::new)));
    }

}
