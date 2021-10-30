package com.tritoncubed.utilitiesbot;

import com.tritoncubed.utilitiesbot.commands.Commands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {

    private static final String ENV_FILE_PATH = "env.properties";
    private static final String PROP_TOKEN = "discord.token";

    public static void main(String[] args) throws IOException, LoginException, InterruptedException {

        // loads environment variables
        System.getProperties().load(new FileInputStream(ENV_FILE_PATH));

        // set up aws
        AwsConnector.setup();

        // log in to discord
        final String TOKEN = System.getProperty(PROP_TOKEN);
        final JDA jda = JDABuilder.createLight(TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .build();
        jda.awaitReady();

        // register all commands
        Commands.registerCommands(AWSCommands.class, jda);
    }
}
