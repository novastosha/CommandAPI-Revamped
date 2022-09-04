package net.zoda.api.command.tests.command;

import net.zoda.api.command.Command;
import net.zoda.api.command.DefaultCommandRun;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.StringArgument;
import net.zoda.api.command.argument.timestamp.TimestampArgument;
import net.zoda.api.command.subcommand.Subcommand;
import net.zoda.api.command.tests.BasicCommandManager;

import java.util.logging.Logger;

@Command(manager = BasicCommandManager.class, value = "a")
public class TestCommand implements ICommand {

    @DefaultCommandRun
    public void defaultRun(Logger logger) {
        System.out.println("ok");
    }

    @Subcommand("a")
    public void subcommand(Logger logger, @StringArgument(name = "test") String la, @TimestampArgument(name = "test1") Long l) {
        System.out.println(la);
        System.out.println(l);
    }


}
