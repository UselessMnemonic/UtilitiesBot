package com.tritoncubed.utilitiesbot;

import com.tritoncubed.utilitiesbot.commands.AWSCommands;
import com.tritoncubed.utilitiesbot.commands.Commands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Main {

    private static final String ENV_FILE_PATH = "env.properties";
    private static final String PROP_TOKEN = "discord-token";

    public static void main(String[] args) throws IOException, LoginException, InterruptedException {

        final Properties ENV = new Properties();
        ENV.load(new FileInputStream(ENV_FILE_PATH));
        final String TOKEN = ENV.getProperty(PROP_TOKEN);

        final JDA jda = JDABuilder.createLight(TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .build();

        Commands.registerCommands(AWSCommands.class, jda);
        jda.awaitReady();
    }
}
