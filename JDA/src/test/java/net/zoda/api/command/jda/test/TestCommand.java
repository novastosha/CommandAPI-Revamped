package net.zoda.api.command.jda.test;

import net.dv8tion.jda.api.entities.Member;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.zoda.api.command.Command;
import net.zoda.api.command.DefaultCommandRun;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.IntegerArgument;
import net.zoda.api.command.argument.StringArgument;
import net.zoda.api.command.jda.GuildOnly;
import net.zoda.api.command.jda.manager.JDACommandManager;
import net.zoda.api.command.subcommand.Subcommand;
import net.zoda.api.command.wrapper.CommandManager;

@Command(value = "a",manager = JDACommandManager.class)
@GuildOnly
public class TestCommand implements ICommand {

    @Subcommand("test")
    public void e(Member member, SlashCommandInteractionEvent event, @StringArgument(name = "lol") String name) {
    }

    @DefaultCommandRun
    public void t(User user, SlashCommandInteractionEvent event, @StringArgument(name = "n") String a) {

    }

}
