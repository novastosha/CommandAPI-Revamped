package net.zoda.api.command.jda.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.zoda.api.command.resolved.ClassResolvedCommand;

@RequiredArgsConstructor
public final class JDAClassResolvedCommand {

    @Getter private final ClassResolvedCommand baseCommand;

    @Getter
    private final Permission[] permissions;

}
