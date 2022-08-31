package net.zoda.api.command.bukkit.command.annotations.permission.group;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RSubcommandGroupPermissions {
    SubcommandGroupPermissions[] value();
}
