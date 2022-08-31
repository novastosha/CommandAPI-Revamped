package net.zoda.api.command.bukkit.command.resolved;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.zoda.api.command.resolved.ClassResolvedCommand;
import net.zoda.api.command.resolved.PostResolvedCommand;
import net.zoda.api.command.resolved.ResolvedSubcommand;
import net.zoda.api.command.resolved.group.ResolvedSubcommandGroup;

import java.util.Map;

@RequiredArgsConstructor
public final class BukkitClassResolvedCommand implements PostResolvedCommand {

    @Getter private final ClassResolvedCommand baseCommand;

    @Getter private final String[] permissions;

    @Getter private final Map<ResolvedSubcommand,String[]> subcommandPermissions;
    @Getter private final Map<ResolvedSubcommandGroup,String[]> subcommandGroupPermissions;

    @Getter private final boolean playerOnly;


}
