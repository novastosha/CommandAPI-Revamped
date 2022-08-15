package net.zoda.api.command.argument.type;

import net.zoda.api.command.ICommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public interface BuiltinCompletionArgumentTypeImpl<T, A extends Annotation> extends ArgumentTypeImpl<T, A> {

    @NotNull List<T> completions(CommandSender sender, A annotation,
                                 Method method, ICommand command);

}
