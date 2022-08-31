package net.zoda.api.command.subcommand.group;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(SubcommandGroups.class)
public @interface SubcommandGroup {
    String value();
    String description() default "No description provided";
}
