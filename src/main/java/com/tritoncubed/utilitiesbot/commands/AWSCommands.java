package com.tritoncubed.utilitiesbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@Command(name="aws", description="Interact with AWS elements.")
public class AWSCommands implements ApplicationCommand {

    @CommandGroup("ebs")
    @Subcommand(name="list", description="List all EBS instances.")
    public static void list(SlashCommandEvent event) {
        event.reply("OK!").queue();
    }

    @CommandGroup("ebs")
    @Subcommand(name="status", description="Get status of an EBS instance.")
    public static void status(
            SlashCommandEvent event,

            @CommandOption(description="Name of the EBS instance.")
            String instance
    ) {
        event.reply("OK!").queue();
    }

}
