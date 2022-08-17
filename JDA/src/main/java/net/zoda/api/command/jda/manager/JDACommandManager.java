package net.zoda.api.command.jda.manager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.zoda.api.command.argument.registry.ArgumentTypeRegistry;
import net.zoda.api.command.jda.argument.UserArgument;
import net.zoda.api.command.wrapper.CommandManager;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class JDACommandManager extends CommandManager<User> {

    private static class SlashCommandListener extends ListenerAdapter {
        @Override
        public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
            if (event.isGuildCommand()) {

            }
        }
    }

    public JDACommandManager(JDA jda) {
        super(User.class);

        ArgumentTypeRegistry registry = ArgumentTypeRegistry.instance();

        registry.register(UserArgument.class,new UserArgument.Impl());
    }

    @Override
    public Class<?>[] extraSignatureClasses() {
        return new Class[] { SlashCommandInteractionEvent.class };
    }

    public Map<String,Object> objectifyArguments(SlashCommandInteractionEvent event) {
        Map<String,Object> map = new HashMap<>();
        event.getOptions().forEach(optionMapping -> {

            Object obj = null;

            switch (optionMapping.getType()) {
                default -> obj = optionMapping.getAsString().split(" ");
            }

            if(obj instanceof String[] array) {

            }
        });
        return map;
    }
}
