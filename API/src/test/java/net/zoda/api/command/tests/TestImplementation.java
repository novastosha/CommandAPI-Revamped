package net.zoda.api.command.tests;

import net.zoda.api.command.tests.command.TestCommand;

public class TestImplementation{

    public static void main(String[] args) {
        BasicCommandManager basicCommandManager = new BasicCommandManager();

        basicCommandManager.registerCommand(new TestCommand());

        basicCommandManager.bindPermanently(false);
    }

}
