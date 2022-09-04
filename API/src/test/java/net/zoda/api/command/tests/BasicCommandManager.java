package net.zoda.api.command.tests;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.resolved.ClassResolvedCommand;
import net.zoda.api.command.resolved.ResolvedSubcommand;
import net.zoda.api.command.wrapper.CommandManager;
import net.zoda.api.command.wrapper.parsing.StringParsingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

public final class BasicCommandManager extends CommandManager<Logger> {

    private static final Scanner input = new Scanner(System.in);

    private final HashMap<String, ClassResolvedCommand> commands;

    public BasicCommandManager() {
        super(Logger.class, DefaultExecutorState.ALLOW_WITHOUT_ARGUMENTS);
        this.commands = new HashMap<>();
    }

    public void registerCommand(ICommand command) {
        ClassResolvedCommand resolvedCommand = resolveCommand(command);
        if (resolvedCommand == null) {
            logger.severe("Couldn't resolve command!");
            return;
        }

        commands.put(resolvedCommand.getName(), resolvedCommand);
    }

    public void bindPermanently(boolean exit) {
        if (exit) return;

        String input = BasicCommandManager.input.nextLine();
        if (input.isEmpty() || input.isBlank()) {
            bindPermanently(false);
            return;
        }

        if (input.equalsIgnoreCase("exit")) return;

        String[] stringArguments = input.split(" ");
        ClassResolvedCommand command = commands.get(stringArguments[0]);

        stringArguments = Arrays.copyOfRange(stringArguments, 1, stringArguments.length);

        if (command == null) {
            System.out.println("Unknown command!");
            bindPermanently(false);
            return;
        }

        try {
            if (command.getSubcommands().size() == 0 || stringArguments.length == 0) {
                StringParsingResult parsedStringArguments = attemptStringParsing(logger, command.getDefaultExecutor(), stringArguments);
                if (parsedStringArguments.wasSuccessful()) {
                    dispatch(command.getInstance(), command.getDefaultExecutor().getMethod(), parsedStringArguments.arguments(), logger, new Object[0]);
                } else {
                    logger.severe(buildGenericFeedbackMessage(parsedStringArguments.feedback()));
                }
                bindPermanently(false);
                return;
            } else {
                ResolvedSubcommand subcommand = parseSubcommand(stringArguments, command);
                if (subcommand == null) {
                    logger.severe("Unknown subcommand!");

                    bindPermanently(false);
                    return;
                }

                String[] newArgs;
                try {
                    newArgs = new String[(stringArguments.length - subcommand.getFullName().split(" ").length)];
                    System.arraycopy(stringArguments, subcommand.getFullName().split(" ").length, newArgs, 0, (stringArguments.length - subcommand.getFullName().split(" ").length));
                } catch (Exception e) {
                    logger.severe("Missing arguments!");
                    bindPermanently(false);
                    return;
                }

                StringParsingResult parsedStringArguments = attemptStringParsing(logger, subcommand.getExecutor(), newArgs);
                if (parsedStringArguments.wasSuccessful()) {
                    dispatch(command.getInstance(), subcommand.getExecutor().getMethod(), parsedStringArguments.arguments(), logger, new Object[0]);
                } else {
                    logger.severe(buildGenericFeedbackMessage(parsedStringArguments.feedback()));
                }
                bindPermanently(false);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        bindPermanently(false);
    }

    @Override
    public boolean commandExists(String name) {
        return commands.containsKey(name);
    }
}
