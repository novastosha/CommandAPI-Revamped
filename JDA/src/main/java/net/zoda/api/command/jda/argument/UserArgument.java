package net.zoda.api.command.jda.argument;

import net.dv8tion.jda.api.entities.User;
import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.ArgumentTypeImpl;
import net.zoda.api.command.argument.type.TargetManager;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.jda.manager.JDACommandManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)

@TargetManager(JDACommandManager.class)
@ArgumentType(typeClass = User.class, name = "user", completionType = CompletionType.REQUIRED_AUTOMATIC)
public @interface UserArgument {

    String name();
    boolean required() default true;

    class Impl implements ArgumentTypeImpl<User,UserArgument> {
        @Override
        public String stringify(User value, UserArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public User fromString(String[] args, UserArgument annotation, Method method, ICommand command) {
            return null;
        }

        @Override
        public int maximumArgs(UserArgument annotation, Method method, ICommand command) {
            return 0;
        }
    }
}
