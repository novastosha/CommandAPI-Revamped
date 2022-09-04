package net.zoda.api.command.bukkit.manager;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.bukkit.PlayerOnly;
import net.zoda.api.command.bukkit.command.annotations.permission.CommandPermissions;
import net.zoda.api.command.bukkit.command.annotations.permission.group.RSubcommandGroupPermissions;
import net.zoda.api.command.bukkit.command.annotations.permission.group.SubcommandGroupPermissions;
import net.zoda.api.command.bukkit.command.resolved.BukkitClassResolvedCommand;
import net.zoda.api.command.bukkit.wrapper.BukkitCommandWrapper;
import net.zoda.api.command.bukkit.wrapper.version.ServerVersion;
import net.zoda.api.command.resolved.ClassResolvedCommand;
import net.zoda.api.command.resolved.ResolvedSubcommand;
import net.zoda.api.command.resolved.group.ResolvedChildSubcommandGroup;
import net.zoda.api.command.resolved.group.ResolvedSubcommandGroup;
import net.zoda.api.command.wrapper.CommandManager;
import net.zoda.api.command.wrapper.PostResolverCommandManager;
import net.zoda.api.command.wrapper.parsing.StringParsingResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class BukkitCommandManager extends CommandManager<CommandSender> implements PostResolverCommandManager<BukkitClassResolvedCommand> {

    private static BukkitCommandManager instance;
    public static BukkitCommandManager instance() {
        if(instance == null) instance = new BukkitCommandManager();
        return instance;
    }

    private BukkitCommandManager() {
        super(CommandSender.class, DefaultExecutorState.ALLOW_WITHOUT_ARGUMENTS);
    }


    @Override
    public Class<? extends CommandSender> actorClassSubcommand(Method method, ICommand command) {
        return method.isAnnotationPresent(PlayerOnly.class) ? Player.class : getDefaultActorClass();
    }

    @Override
    public Class<? extends CommandSender> actorClassClass(ICommand command) {
        return command.getClass().isAnnotationPresent(PlayerOnly.class) ? Player.class : getDefaultActorClass();
    }

    @Override
    public boolean commandExists(String name) {
        return false;
    }

    @SuppressWarnings("unchecked")
    public void registerCommand(ICommand command, String fallbackPrefix) {
        BukkitClassResolvedCommand resolvedCommand = postResolve(super.resolveCommand(command));
        if(resolvedCommand == null) {
            logger.severe("Couldn't resolve command, read logs above!");
            return;
        }

        ServerVersion version = ServerVersion.getVersion();
        Map<String, Command> map;
        try {
            Server server = Bukkit.getServer();
            Field field = server.getClass().getDeclaredField("commandMap");
            field.setAccessible(true);

            CommandMap commandMap = (CommandMap) field.get(server);
            org.bukkit.command.Command cmd = commandMap.getCommand(resolvedCommand.getBaseCommand().getName());

            if (version.equals(ServerVersion.MODERN)) {
                map = (Map<String, org.bukkit.command.Command>) commandMap.getClass().getDeclaredMethod("getKnownCommands").invoke(commandMap);
            } else {
                Field commandField = commandMap.getClass().getDeclaredField("knownCommands");
                commandField.setAccessible(true);
                map = (Map<String, org.bukkit.command.Command>) commandField.get(commandMap);
            }

            if (cmd != null) {
                cmd.unregister(commandMap);
                map.remove(resolvedCommand.getBaseCommand().getName());
                Arrays.stream(resolvedCommand.getBaseCommand().getAliases()).forEach(map::remove);
            }

            CommandExecutor executor = buildLogic(resolvedCommand);

            BukkitCommandWrapper bukkitCmd = new BukkitCommandWrapper(resolvedCommand.getBaseCommand(), executor);
            commandMap.register(fallbackPrefix, bukkitCmd);
            bukkitCmd.register(commandMap);

            logger.info("Command: " + resolvedCommand.getBaseCommand().getName() + " has successfully been registered!");
        } catch (Exception exception) {
            logger.severe("An unexpected error occurred while registering the command!");
            exception.printStackTrace();
        }
    }

    private CommandExecutor buildLogic(BukkitClassResolvedCommand command) {
        return (sender, bukkitCommand, label, stringArguments) -> {
            try {
                if (command.isPlayerOnly() && !(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can execute this command!");
                    return true;
                }

                if (checkPermissions(command.getPermissions(), sender)) return true;

                if (command.getBaseCommand().getSubcommands().size() == 0 || stringArguments.length == 0) {
                    StringParsingResult parsedStringArguments = attemptStringParsing(sender, command.getBaseCommand().getDefaultExecutor(), stringArguments);
                    if (parsedStringArguments.wasSuccessful()) {
                        dispatch(command.getBaseCommand().getInstance(), command.getBaseCommand().getDefaultExecutor().getMethod(), parsedStringArguments.arguments(), sender,new Object[0]);
                    }else{
                        sender.sendMessage(ChatColor.RED+buildGenericFeedbackMessage(parsedStringArguments.feedback()));
                    }
                    return true;
                } else {
                    ResolvedSubcommand subcommand = parseSubcommand(stringArguments, command.getBaseCommand());
                    if (subcommand == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown subcommand!");
                        return true;
                    }

                    if (subcommand.getGroup() != null && checkPermissions(collectPermissions(command, subcommand.getGroup()), sender))
                        return true;

                    String[] newArgs;
                    try {
                        newArgs = new String[(stringArguments.length - subcommand.getFullName().split(" ").length)];
                        System.arraycopy(stringArguments, subcommand.getFullName().split(" ").length, newArgs, 0, (stringArguments.length - subcommand.getFullName().split(" ").length));
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Missing arguments!");
                        return true;
                    }

                    StringParsingResult parsedStringArguments = attemptStringParsing(sender, subcommand.getExecutor(), newArgs);
                    if (parsedStringArguments.wasSuccessful()) {
                        dispatch(command.getBaseCommand().getInstance(), subcommand.getExecutor().getMethod(), parsedStringArguments.arguments(), sender,new Object[0]);
                    }else{
                        sender.sendMessage(ChatColor.RED+buildGenericFeedbackMessage(parsedStringArguments.feedback()));
                    }
                    return true;
                }

            } catch (Exception exception) {
                sender.sendMessage(ChatColor.RED + "An error occurred while executing the command!");
                exception.printStackTrace();
            }
            return true;
        };
    }

    private String[] collectPermissions(BukkitClassResolvedCommand command, ResolvedSubcommandGroup group) {
        if (!(group instanceof ResolvedChildSubcommandGroup child))
            return command.getSubcommandGroupPermissions().getOrDefault(group, new String[0]);

        ResolvedSubcommandGroup newGroup = child;
        List<String> list = new ArrayList<>(List.of(command.getSubcommandGroupPermissions().getOrDefault(child.head(), new String[0])));

        while (newGroup instanceof ResolvedChildSubcommandGroup rChild) {
            list.addAll(List.of(command.getSubcommandGroupPermissions().getOrDefault(newGroup, new String[0])));
            newGroup = rChild.getParent();
        }

        return list.toArray(new String[0]);
    }

    @Override
    public BukkitClassResolvedCommand postResolve(ClassResolvedCommand baseCommand) {
        if (baseCommand == null) return null;
        Class<? extends ICommand> commandClass = baseCommand.getInstance().getClass();

        String[] basePermissions = new String[0];
        if (commandClass.isAnnotationPresent(CommandPermissions.class)) {
            basePermissions = commandClass.getAnnotation(CommandPermissions.class).value();
        }

        Map<ResolvedSubcommandGroup, String[]> groupPermissions = new HashMap<>();
        Map<ResolvedSubcommand, String[]> subcommandPermissions = new HashMap<>();

        List<SubcommandGroupPermissions> rawGroupPermissionList = new ArrayList<>();

        if (commandClass.isAnnotationPresent(RSubcommandGroupPermissions.class)) {
            rawGroupPermissionList.addAll(
                    List.of(commandClass.getAnnotation(RSubcommandGroupPermissions.class).value())
            );
        }

        if (commandClass.isAnnotationPresent(SubcommandGroupPermissions.class)) {
            rawGroupPermissionList.addAll(
                    List.of(commandClass.getAnnotationsByType(SubcommandGroupPermissions.class))
            );
        }

        for (SubcommandGroupPermissions loopSubcommandPermissions : rawGroupPermissionList) {
            for (String group : loopSubcommandPermissions.targets()) {
                if (!baseCommand.getGroups().containsKey(group)) {
                    logger.warning("Unknown subcommand group permission target: " + group);
                    continue;
                }
                groupPermissions.put(baseCommand.getGroups().get(group), loopSubcommandPermissions.permissions());
            }
        }

        for(ResolvedSubcommand subcommand : baseCommand.getSubcommands().values()) {
            Method method = subcommand.getExecutor().getMethod();

            if(!method.isAnnotationPresent(CommandPermissions.class)) continue;
            subcommandPermissions.put(subcommand,method.getAnnotation(CommandPermissions.class).value());
        }

        return new BukkitClassResolvedCommand(baseCommand, basePermissions, subcommandPermissions, groupPermissions, commandClass.isAnnotationPresent(PlayerOnly.class));
    }

    private boolean checkPermissions(String[] permissions, CommandSender sender) {
        for (String s : permissions) {
            if (!sender.hasPermission(s)) {
                sender.sendMessage(ChatColor.RED + "Not enough permissions");
                return true;
            }
        }
        return false;
    }
}
