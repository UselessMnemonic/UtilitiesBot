package com.tritoncubed.utilitiesbot;

import com.tritoncubed.utilitiesbot.commands.*;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.List;
import java.util.Objects;

@Command(name="aws", description="Interact with AWS elements.")
@SubcommandGroup(name="ebs", description="Interact with AWS EBS.")
public class AWSCommands implements ApplicationCommand {

    @Subcommand(name="list", description="List all EBS instances.", group="ebs")
    public static void list(SlashCommandEvent event) {
        var hook = event.deferReply(true).complete();
        List<Instance> instances = AwsConnector.AwsEbsList();
        MessageBuilder builder = new MessageBuilder();

        for (Instance i : instances) {
            builder.appendFormat("Id: %s\tName: %s", i.instanceId(), i.publicDnsName());
        }

        hook.editOriginal(builder.build()).queue();
    }

    @Subcommand(name="status", description="Get status of an EBS instance.", group="ebs")
    public static void status(
        SlashCommandEvent event,

        @CommandOption(name="instance", description="Id of the EBS instance.", required = true)
        String id
    ){
        Instance instance = AwsConnector.AwsEbsStatus(id);
        MessageBuilder builder = new MessageBuilder();

        builder.append(Objects.toString(instance).substring(0, 1500));

        event.reply(builder.build()).queue();
    }
}
