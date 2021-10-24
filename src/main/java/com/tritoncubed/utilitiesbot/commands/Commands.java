package com.tritoncubed.utilitiesbot.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Commands {

    public static void registerCommands(Class<? extends ApplicationCommand> command, JDA jda) {

        List<Method> methods = Arrays.stream(command.getMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .collect(Collectors.toList());

        Command rootCommand = command.getAnnotation(Command.class);

        if (rootCommand == null) {

        } else {
            CommandData commandData = new CommandData(rootCommand.name(), rootCommand.description());
            for (Method m : methods) {

                Subcommand subcommand = m.getAnnotation(Subcommand.class);
                if (subcommand == null) continue;
                SubcommandData subcommandData = new SubcommandData(subcommand.name(), subcommand.description());

                for (Parameter p : m.getParameters()) {
                    p.getAnnotation(CommandOption.class);
                }
            }
        }
    }

}
