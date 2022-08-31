package net.zoda.bukkit.tests.commands;

import net.zoda.api.command.Command;
import net.zoda.api.command.CommandAliases;
import net.zoda.api.command.DefaultCommandRun;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.EnumArgument;
import net.zoda.api.command.bukkit.PlayerOnly;
import net.zoda.api.command.bukkit.command.annotations.permission.CommandPermissions;
import net.zoda.api.command.bukkit.manager.BukkitCommandManager;
import net.zoda.api.command.subcommand.Subcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAliases("a")
@CommandPermissions({"tests.example","tests.example1"})
@Command(value = "test",manager = BukkitCommandManager.class)
public final class TestCommand implements ICommand {

    public enum TestEnum {
        EXAMPLE_CONSTANT,
        EXAMPLE_CONSTANT2
    }

    @DefaultCommandRun
    @PlayerOnly
    public void defaultRun(Player player) {
        player.sendMessage("Default executor!");
    }

    @Subcommand("subcommand")
    public void testSubcommand(CommandSender sender, @EnumArgument(name = "example_enum",targetClass = TestEnum.class) TestEnum testEnum) {
        sender.sendMessage(testEnum.name());
    }
}
