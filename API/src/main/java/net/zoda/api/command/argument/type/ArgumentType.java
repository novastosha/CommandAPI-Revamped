package net.zoda.api.command.argument.type;

import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.argument.type.completion.QuotesState;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Any argument type annotation with no {@link TargetManager} is considered manager-independent (universal)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ArgumentType {

    Class<?> typeClass();
    String name();
    QuotesState quotesState() default QuotesState.OFF;

    CompletionType completionType();


}
