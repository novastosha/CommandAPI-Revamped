package net.zoda.api.command.jda.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public non-sealed class GuildJDACommand implements JDACommand {
    @Getter private final long guildId;
}
