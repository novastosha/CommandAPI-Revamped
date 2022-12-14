package net.zoda.api.command.bukkit.command.annotations.permission.group;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Repeatable;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@Repeatable(RSubcommandGroupPermissions.class)
public @interface SubcommandGroupPermissions {

    String[] targets();
    String[] permissions();

}
