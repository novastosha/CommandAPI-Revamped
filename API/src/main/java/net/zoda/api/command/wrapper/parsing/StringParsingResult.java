package net.zoda.api.command.wrapper.parsing;

import net.zoda.api.command.resolved.argument.ResolvedArgument;

public record StringParsingResult(
        ParsingResultFeedback feedback,
        ParsedArgument[] arguments) {

    public enum EnumArgumentParsingResult {
        SUCCESS,
        FAILURE
    }

    public enum EnumParsingResultType {

        SUCCESS,

        INCOMPLETE_ARGUMENT,
        INVALID_ARGUMENT,
        MISSING_ARGUMENT,

        UNKNOWN_GROUP,
        UNKNOWN_SUBCOMMAND,

        OTHER,
        EMPTY_ARGUMENT_LIST
    }

    public record ParsingResultFeedback(EnumParsingResultType resultType, String message,
                                        ResolvedArgument argument) {}

    public record ArgumentParsingResult(EnumArgumentParsingResult result,String message) {}

    public boolean wasSuccessful() {
        return feedback == null;
    }
}

