package com.tritoncubed.utilitiesbot;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class EBSStatusCommand extends Command {

    public static final SubCommand EBS_LIST_SUBCMD = new SubCommand("list", "Lists all EBS deployments.") {
        @Override
        public void execute(SlashCommandEvent event) {
            event.reply("Thank you!").setEphemeral(event.isFromGuild()).queue();
        }
    };

    public EBSStatusCommand() {
        super("ebsstatus", "Queries the current status of an EBS deployment.");
        this.addSubCommand(EBS_LIST_SUBCMD);
    }
}
