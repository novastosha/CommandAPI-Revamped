package net.zoda.api.command.wrapper.parsing;

import net.zoda.api.command.resolved.argument.ResolvedArgument;

public record ParsedArgument(ResolvedArgument baseArgument, Object value) { }
