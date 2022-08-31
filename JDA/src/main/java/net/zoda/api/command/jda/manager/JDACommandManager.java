package net.zoda.api.command.jda.manager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.IntegerArgument;
import net.zoda.api.command.argument.NumberArgument;
import net.zoda.api.command.argument.registry.ArgumentTypeRegistry;
import net.zoda.api.command.argument.type.BuiltinCompletionArgumentTypeImpl;
import net.zoda.api.command.jda.argument.ArgumentTypeInJDA;
import net.zoda.api.command.jda.argument.ChannelArgument;
import net.zoda.api.command.jda.argument.UserArgument;
import net.zoda.api.command.jda.command.GuildJDACommand;
import net.zoda.api.command.jda.command.JDACommand;
import net.zoda.api.command.jda.command.annotations.CommandPermissions;
import net.zoda.api.command.resolved.ClassResolvedCommand;
import net.zoda.api.command.resolved.ResolvedSubcommand;
import net.zoda.api.command.resolved.argument.ResolvedArgument;
import net.zoda.api.command.resolved.argument.ResolvedCompletionArgument;
import net.zoda.api.command.resolved.group.ResolvedChildSubcommandGroup;
import net.zoda.api.command.resolved.group.ResolvedSubcommandGroup;
import net.zoda.api.command.utils.Pair;
import net.zoda.api.command.utils.TriPair;
import net.zoda.api.command.wrapper.CommandManager;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class JDACommandManager extends CommandManager<User> {

    private static final Map<Class<? extends Annotation>, OptionType> MAPPED_OPTION_TYPES = Map.of(
            IntegerArgument.class, OptionType.INTEGER,
            NumberArgument.class, OptionType.NUMBER
    );

    private final JDA jda;
    private final Map<String, TriPair<JDACommand, JDAClassResolvedCommand, SlashCommandData>> registeredCommands;

    private static class SlashCommandListener extends ListenerAdapter {
        @Override
        public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
            if (event.isGuildCommand()) {

            }
        }
    }

    public JDACommandManager(JDA jda) {
        super(User.class, DefaultExecutorState.DISALLOW_WITH_SUBCOMMANDS);

        this.jda = jda;

        this.registeredCommands = new HashMap<>();

        ArgumentTypeRegistry registry = ArgumentTypeRegistry.instance();

        registry.register(UserArgument.class, new UserArgument.Impl());
    }

    @Override
    public Class<?>[] extraSignatureClasses() {
        return new Class[]{SlashCommandInteractionEvent.class};
    }

    public void registerSlashCommand(JDACommand command) {
        ClassResolvedCommand resolvedCommand = this.resolveCommand(command);

        if (resolvedCommand == null) {
            logger.severe("Couldn't resolve command, read logs above!");
            return;
        }

        SlashCommandData action = Commands.slash(resolvedCommand.getName(), resolvedCommand.getDescription());
        if (command instanceof GuildJDACommand guildJDACommand) {
            Guild guild = jda.getGuildCache().getElementById(guildJDACommand.getGuildId());
            if (guild == null) {
                logger.severe("Invalid GuildID: " + guildJDACommand.getGuildId());
                return;
            }

        }

        JDAClassResolvedCommand jdaCommand = postResolveCommand(resolvedCommand);
        populateCommand(action, jdaCommand);

        try {
            registeredCommands.put(action.getName(), new TriPair<>(command, jdaCommand, action));
            updateCommandsBulk();
        } catch (Exception e) {
            logger.severe("There was an error sending the command, read stacktrace or the logs above!");
            e.printStackTrace();
        }
    }

    private void updateCommandsBulk() {
        List<SlashCommandData> globalCommands = new ArrayList<>();
        Map<Long, List<SlashCommandData>> guildCommands = new HashMap<>();

        for (TriPair<JDACommand, JDAClassResolvedCommand, SlashCommandData> pair : registeredCommands.values()) {
            if (pair.a() instanceof GuildJDACommand guildJDACommand) {

                List<SlashCommandData> dataList = guildCommands.getOrDefault(guildJDACommand.getGuildId(), new ArrayList<>());
                dataList.add(pair.c());

                guildCommands.put(guildJDACommand.getGuildId(), dataList);
            } else {
                globalCommands.add(pair.c());
            }
        }

        jda.updateCommands().addCommands(globalCommands.toArray(new CommandData[0])).queue();
        for (Map.Entry<Long, List<SlashCommandData>> dataEntry : guildCommands.entrySet()) {
            Guild guild = jda.getGuildCache().getElementById(dataEntry.getKey());
            if (guild == null) continue;

            guild.updateCommands().addCommands(dataEntry.getValue().toArray(globalCommands.toArray(new CommandData[0]))).queue();
        }
    }

    private JDAClassResolvedCommand postResolveCommand(ClassResolvedCommand resolvedCommand) {
        return new JDAClassResolvedCommand(resolvedCommand, resolvedCommand.getInstance().getClass().isAnnotationPresent(CommandPermissions.class) ?
                resolvedCommand.getInstance().getClass().getAnnotation(CommandPermissions.class).value()
                : new Permission[0]);
    }

    private void populateSubcommand(SubcommandData data, JDAClassResolvedCommand resolvedCommand,
                                    SlashCommandData action, Map.Entry<String, ResolvedSubcommand> entry) {
        for (ResolvedArgument argument : entry.getValue().getExecutor().getArguments()) {
            data.addOptions(makeOption(argument));
        }

        if (entry.getValue().getGroup() != null) {

            System.out.println("added to group");
            //In this case it'll always be 0 but used this in case of any changes
            ResolvedSubcommandGroup group = entry.getValue().getGroup();
            SubcommandGroupData groupData = getGroup(group.getName(), action);
            groupData.addSubcommands(data);

        } else {
            System.out.println("added to action!");
            action.addSubcommands(data);
        }
    }

    private SubcommandGroupData getGroup(String name, SlashCommandData action) {
        for (SubcommandGroupData subcommandGroupData : action.getSubcommandGroups()) {
            if (subcommandGroupData.getName().equals(name)) return subcommandGroupData;
        }

        return null;
    }

    private void populateCommand(SlashCommandData action, JDAClassResolvedCommand resolvedCommand) {
        //action.setGuildOnly(resolvedCommand.getBaseCommand().getInstance() instanceof GuildJDACommand);
        if (!resolvedCommand.getBaseCommand().getSubcommands().isEmpty()) {

            for (Map.Entry<String, ResolvedSubcommandGroup> groupEntry : resolvedCommand.getBaseCommand().getGroups().entrySet()) {
                System.out.println("looped through group");
                SubcommandGroupData groupData = new SubcommandGroupData(groupEntry.getKey(), groupEntry.getValue().getDescription());
                action.addSubcommandGroups(groupData);
            }

            for (Map.Entry<String, ResolvedSubcommand> subcommandEntry : resolvedCommand.getBaseCommand().getSubcommands().entrySet()) {
                System.out.println("looped through subcommand");
                SubcommandData subcommandData = new SubcommandData(subcommandEntry.getValue().getName(), subcommandEntry.getValue().getDescription());
                populateSubcommand(subcommandData, resolvedCommand, action, subcommandEntry);

            }
        } else {
            System.out.println("else called");
            for (ResolvedArgument argument : resolvedCommand.getBaseCommand().getDefaultExecutor().getArguments()) {
                action.addOptions(makeOption(argument));
            }
        }

        //action.setDefaultPermissions(DefaultMemberPermissions.enabledFor(resolvedCommand.getPermissions()));
    }

    private OptionData makeOption(ResolvedArgument argument) {

        OptionType type = getOptionType(argument);

        //TODO: Implement autocompletion
        OptionData data = new OptionData(type, argument.getName(),
                argument.getDescription(), argument.isRequired(),
                false);

        if (argument.getAnnotation().annotationType().equals(ChannelArgument.class)) {
            data.setChannelTypes(((ChannelArgument) argument.getAnnotation()).channelTypes());
        }

        if (argument.getAnnotation().annotationType().equals(NumberArgument.class)) {
            NumberArgument numberArgument = (NumberArgument) argument.getAnnotation();

            if (!(numberArgument.range().min() == 0 && numberArgument.range().max() == 0)) {
                data.setRequiredRange(numberArgument.range().min(), numberArgument.range().max());
            }
        }

        if (argument.getAnnotation().annotationType().equals(IntegerArgument.class)) {
            IntegerArgument integerArgument = (IntegerArgument) argument.getAnnotation();

            if (!(integerArgument.range().min() == 0 && integerArgument.range().max() == 0)) {
                data.setRequiredRange(integerArgument.range().min(), integerArgument.range().max());
            }
        }

        data.setAutoComplete(argument instanceof ResolvedCompletionArgument completionArgument &&
                !(completionArgument.getCompleter().isBlank() || completionArgument.getCompleter().isEmpty()));
        return data;
    }

    private OptionType getOptionType(ResolvedArgument argument) {
        ArgumentTypeInJDA jdaArgumentType = argument.getAnnotation().annotationType().getAnnotation(ArgumentTypeInJDA.class);
        if (jdaArgumentType != null) {
            return jdaArgumentType.value();
        }

        return MAPPED_OPTION_TYPES.getOrDefault(argument.getAnnotation().annotationType(), OptionType.STRING);
    }

    @Override
    public Class<?> actorClassClass(ICommand command) {
        return command instanceof GuildJDACommand ? Member.class : User.class;
    }

    @Override
    public Class<?> actorClassSubcommand(Method method, ICommand command) {
        return command instanceof GuildJDACommand ? Member.class : User.class;
    }

    @Override
    protected @Nullable ClassResolvedCommand resolveCommand(ICommand instance) {
        ClassResolvedCommand command = super.resolveCommand(instance);
        if (command == null) return null;

        for (ResolvedSubcommandGroup group : command.getGroups().values()) {
            if (group instanceof ResolvedChildSubcommandGroup childSubcommandGroup) {
                logger.severe("Discord slash commands do not allow nested subcommand groups! (" + childSubcommandGroup.getName() + ")");
                return null;
            }
        }

        return command;
    }

    @Override
    public boolean commandExists(String name) {
        return registeredCommands.containsKey(name);
    }
}
