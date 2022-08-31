package net.zoda.api.command.jda.argument;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.ArgumentTypeImpl;
import net.zoda.api.command.argument.type.TargetManager;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.jda.command.GuildJDACommand;
import net.zoda.api.command.jda.manager.JDACommandManager;
import net.zoda.api.command.utils.Pair;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

@ArgumentTypeInJDA(OptionType.CHANNEL)

@TargetManager(JDACommandManager.class)

@Retention(RetentionPolicy.RUNTIME)
@ArgumentType(name = "channel", typeClass = Channel.class, completionType = CompletionType.REQUIRED_AUTOMATIC)
public @interface ChannelArgument {

    ChannelType[] DISALLOWED_CHANNEL_TYPES = new ChannelType[]{
            ChannelType.UNKNOWN,
            ChannelType.GROUP
    };

    String name();

    String description() default "No description provided";

    boolean required() default true;

    ChannelType[] channelTypes() default {};

    class Impl implements ArgumentTypeImpl<Channel, ChannelArgument> {

        @Override
        public String stringify(Object value, ChannelArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public Pair<Channel, String> fromString(String[] args, ChannelArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public int maximumArgs(ChannelArgument annotation, Method method, ICommand command) {
            return 0;
        }

        @Override
        public boolean verifyAnnotation(ChannelArgument annotation, Logger logger, Method method, ICommand command) {
            if (!(command instanceof GuildJDACommand) && hasGuildType(annotation.channelTypes())) {
                logger.severe("You cannot use Guild Channel types on a Global command!");
                return false;
            }

            if(Collections.disjoint(Arrays.asList(DISALLOWED_CHANNEL_TYPES), Arrays.asList(annotation.channelTypes()))) {
                logger.severe("You cannot use: "+Arrays.toString(DISALLOWED_CHANNEL_TYPES)+" channel types!");
                return false;
            }
            return true;
        }

        private boolean hasGuildType(ChannelType[] channelTypes) {
            for (ChannelType type : channelTypes) {
                if(type.isGuild()) return true;
            }
            return false;
        }
    }
}
