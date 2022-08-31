package net.zoda.api.command.resolved.group;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ResolvedSubcommandGroup {

    @Getter private final String name;
    @Getter private final String description;

    public String getFullName() {
        if (this instanceof ResolvedChildSubcommandGroup childSubcommandGroup) {
            List<String> names = new ArrayList<>();

            ResolvedSubcommandGroup p = this;
            while (p instanceof ResolvedChildSubcommandGroup child) {
                names.add(child.getName());
                p = child.getParent();
            }

            names.add(childSubcommandGroup.head().name);
            Collections.reverse(names);

            return String.join(" ", names.toArray(new String[0]));
        }

        return name;
    }
}
