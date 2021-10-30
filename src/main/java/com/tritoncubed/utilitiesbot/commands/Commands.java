package com.tritoncubed.utilitiesbot.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Discord Command Tree
 * (1) Command
 *
 * (2) Command [name = dothis]
 *     |_ Subcommand [name = doelse]   /dothis doelse
 *     |_ Subcommand
 *     |_...
 *
 * (3) Command [name = command]
 *     |_ SubcommandGroups [name = group]
 *     |  |_ Subcommand [name = dothis]      /command group dothis
 *     |  |_ ...
 *     |_ SubcommandGroup
 *        |_ ...
 *
 *  (4) Command
 *      (with groups and subcommands)
 */

public final class Commands {
    public static void registerCommands(Class<? extends ApplicationCommand> commandClass, JDA jda) {
        ClassCommand commandImpl = new ClassCommand(commandClass);
        List<CommandData> commandData = commandImpl.compile();

        for (CommandData c : commandData) {
            jda.upsertCommand(c).complete();
        }

        jda.addEventListener(commandImpl);
    }

    public static void invoke(SlashCommandEvent event, Method method) {
        List<OptionMapping> options = event.getOptions();
        Parameter[] params = method.getParameters();

        Object[] args = new Object[params.length];
        args[0] = event;

        for (int i = 1; i < args.length; i++) {
            Parameter param = params[i];
            CommandOption a = param.getAnnotation(CommandOption.class);
            OptionMapping mapping = options.stream()
                    .filter(o -> o.getName().equals(a.name()))
                    .findFirst()
                    .orElse(null);
            args[i] = Commands.castOptionAsType(param.getType(), mapping);
        }

        try {
            method.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Logger.getLogger("DEBUG").log(Level.SEVERE, "Exception when invoking command.", e);
        }
    }

    static Object castOptionAsType(Class<?> type, OptionMapping option) {
        if (option == null) {
            return null;
        } else if (type == Boolean.class | type == boolean.class) {
            return option.getAsBoolean();
        } else if (type == Integer.class | type == int.class) {
            return (int) option.getAsLong();
        } else if (type == Long.class | type == long.class) {
            return option.getAsLong();
        } else if (type == MessageChannel.class) {
            return option.getAsMessageChannel();
        } else if (type == Role.class) {
            return option.getAsRole();
        } else if (type == User.class) {
            return option.getAsUser();
        } else if (type == Member.class) {
            return option.getAsMember();
        } else if (type == IMentionable.class) {
            return option.getAsMentionable();
        } else if (type == String.class) {
            return option.getAsString();
        } else {
            throw new IllegalArgumentException("Parameter of type %s is unsupported.".formatted(type.toGenericString()));
        }
    }

    static OptionData[] getOptionData(Method method) {

        // Convert method parameters to command options
        Parameter[] params = method.getParameters();
        OptionData[] optionData = new OptionData[params.length - 1];

        for (int i = 1; i < params.length; i++) {
            CommandOption a = params[i].getAnnotation(CommandOption.class);
            Class<?> paramType = params[i].getType();
            OptionType optionType = Commands.getOptionType(paramType);

            optionData[i - 1] = new OptionData(optionType, a.name(), a.description(), a.required());
        }

        return optionData;
    }

    static OptionType getOptionType(Class<?> type) {
        if (type == Boolean.class) {
            return OptionType.BOOLEAN;
        } else if (type == boolean.class) {
            return OptionType.BOOLEAN;
        } else if (type == Integer.class | type == Long.class) {
            return OptionType.INTEGER;
        } else if (type == int.class | type == long.class) {
            return OptionType.INTEGER;
        } else if (type == MessageChannel.class | type == GuildChannel.class) {
            return OptionType.CHANNEL;
        } else if (type == Role.class) {
            return OptionType.ROLE;
        } else if (type == User.class | type == Member.class) {
            return OptionType.USER;
        } else if (type == IMentionable.class) {
            return OptionType.MENTIONABLE;
        } else if (type == String.class) {
            return OptionType.STRING;
        } else if (type.isEnum()) {
            return OptionType.STRING;
        }
        else {
            throw new IllegalArgumentException("Parameter of type %s is unsupported.".formatted(type.toGenericString()));
        }
    }
}

class ClassCommand extends ListenerAdapter {
    public final String rootCommandName, rootCommandDescription;
    private final HashMap<String, MethodSubcommandGroup> subcommandGroups = new HashMap<>();
    private final HashMap<String, MethodSubcommand> subcommands = new HashMap<>();

    private final HashMap<String, Method> commands = new HashMap<>();

    public ClassCommand(Class<? extends ApplicationCommand> clazz) {
        // Get root command name
        Command rootCommandAnnotation = clazz.getAnnotation(Command.class);
        if (rootCommandAnnotation == null) {
            rootCommandName = "";
            rootCommandDescription = "";
        } else {
            rootCommandName = rootCommandAnnotation.name();
            rootCommandDescription = rootCommandAnnotation.description();
        }

        // Get all subcommand groups
        SubcommandGroup[] groupAnnotations = clazz.getAnnotationsByType(SubcommandGroup.class);
        for (SubcommandGroup a : groupAnnotations) {
            subcommandGroups.put(a.name(), new MethodSubcommandGroup(a.name(), a.description()));
        }

        // Collect all static methods from the class
        List<Method> staticMethods = Arrays.stream(clazz.getMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers())).toList();

        // Find all subcommands
        staticMethods.stream()
                .filter(m -> m.isAnnotationPresent(Subcommand.class))
                .forEach(m -> {
                    Subcommand a = m.getAnnotation(Subcommand.class);

                    // subcommand has no group
                    if (a.group().isEmpty()) {
                        subcommands.put(a.name(), new MethodSubcommand(m));
                    }

                    // subcommand has a group
                    else {
                        subcommandGroups.get(a.group()).addSubcommand(new MethodSubcommand(m));
                    }
                });

        // Finally, find all simple commands
        staticMethods.stream()
                .filter(m -> m.isAnnotationPresent(Command.class))
                .forEach(m -> {
                    Command commandAnnotation = m.getAnnotation(Command.class);
                    commands.putIfAbsent(commandAnnotation.name(), m);
                });
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        // command is the root command
        if (Objects.equals(event.getName(), rootCommandName)) {
            if (subcommandGroups.containsKey(event.getSubcommandGroup())) {
                MethodSubcommandGroup group = subcommandGroups.get(event.getSubcommandGroup());
                MethodSubcommand subcommand = group.getSubcommand(event.getSubcommandName());
                Commands.invoke(event, subcommand.method);
            }
            else if (subcommands.containsKey(event.getSubcommandName())) {
                MethodSubcommand subcommand = subcommands.get(event.getSubcommandName());
                Commands.invoke(event, subcommand.method);
            }
        }

        // command is not the root command
        else if (commands.containsKey(event.getName())) {
            Commands.invoke(event, commands.get(event.getName()));
        }
    }

    public List<CommandData> compile() {
        ArrayList<CommandData> result = new ArrayList<>();

        // compile the simple command methods
        for (Method m : commands.values()) {
            Command a = m.getAnnotation(Command.class);
            CommandData data = new CommandData(a.name(), a.description());
            data.addOptions(Commands.getOptionData(m));
            data.setDefaultEnabled(a.permission());
            result.add(data);
        }

        // leave here if class has no root command
        if (rootCommandName.isEmpty() || rootCommandDescription.isEmpty()) {
            return result;
        }

        // process the root command
        CommandData rootData = new CommandData(this.rootCommandName, this.rootCommandDescription);
        for (MethodSubcommandGroup g : subcommandGroups.values()) {
            SubcommandGroupData data = g.compile();
            rootData.addSubcommandGroups(data);
        }
        for (MethodSubcommand c : subcommands.values()) {
            SubcommandData data = c.compile();
            rootData.addSubcommands(data);
        }
        result.add(rootData);

        return result;
    }
}

/**
 * Class that represents subcommand
 */
class MethodSubcommand {
    public final String name, description;
    public final Method method;

    public MethodSubcommand(Method method) {
        Subcommand a = method.getAnnotation(Subcommand.class);
        name = a.name();
        description = a.description();
        this.method = method;
    }

    public SubcommandData compile() {
        SubcommandData data = new SubcommandData(name, description);
        data.addOptions(Commands.getOptionData(method));
        return data;
    }
}

/**
 * Class that represents subcommand group
 */
class MethodSubcommandGroup {
    public final String name, description;
    private final HashMap<String, MethodSubcommand> subcommands = new HashMap<>();

    public MethodSubcommandGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addSubcommand(MethodSubcommand methodSubcommand) {
        subcommands.put(methodSubcommand.name, methodSubcommand);
    }

    public MethodSubcommand getSubcommand(String name) {
        return subcommands.get(name);
    }

    public SubcommandGroupData compile() {
        SubcommandGroupData data = new SubcommandGroupData(name, description);
        for (MethodSubcommand c : subcommands.values()) {
            data.addSubcommands(c.compile());
        }
        return data;
    }
}
