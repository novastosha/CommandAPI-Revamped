package net.zoda.api.command.jda.test;


import net.zoda.api.command.jda.manager.JDACommandManager;

public class Main {

    public static void main(String[] args) {

        JDACommandManager commandManager = new JDACommandManager(null);

        commandManager.resolveCommand(new TestCommand());
    }

}
