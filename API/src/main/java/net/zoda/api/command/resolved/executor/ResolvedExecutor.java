package net.zoda.api.command.resolved.executor;

import lombok.Getter;
import net.zoda.api.command.resolved.argument.ResolvedArgument;

import java.lang.reflect.Method;

public final class ResolvedExecutor {

    @Getter private final Method method;
    @Getter private final ResolvedArgument[] arguments;

    public ResolvedExecutor(Method method, ResolvedArgument[] arguments) {
        this.method = method;
        this.arguments = arguments;
    }

}
