package net.zoda.api.command.wrapper;

import net.zoda.api.command.resolved.ClassResolvedCommand;
import net.zoda.api.command.resolved.PostResolvedCommand;

public interface PostResolverCommandManager<T extends PostResolvedCommand> {
    T postResolve(ClassResolvedCommand baseCommand);
}
