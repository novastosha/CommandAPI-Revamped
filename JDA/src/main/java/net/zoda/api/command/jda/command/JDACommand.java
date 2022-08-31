package net.zoda.api.command.jda.command;

import net.zoda.api.command.ICommand;

public sealed interface JDACommand extends ICommand permits
        GlobalJDACommand,
        GuildJDACommand
{}
