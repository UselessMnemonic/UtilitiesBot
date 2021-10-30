package com.tritoncubed.utilitiesbot;

import com.tritoncubed.utilitiesbot.commands.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@Command(name="aws", description="Interact with AWS elements.")
@SubcommandGroup(name="ebs", description="Interact with AWS EBS.")
public class AWSCommands implements ApplicationCommand {

    @Subcommand(name="list", description="List all EBS instances.", group="ebs")
    public static void list(SlashCommandEvent event) {
        event.reply("OK!").queue();
    }

    @Subcommand(name="status", description="Get status of an EBS instance.", group="ebs")
    public static void status(
            SlashCommandEvent event,

            @CommandOption(name="instance", description="Name of the EBS instance.")
            String instance
    ) {
        event.reply("Thank you!").setEphemeral(event.isFromGuild()).queue();
    }

}
