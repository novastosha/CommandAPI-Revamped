package net.zoda.api.command.argument.timestamp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public enum TimestampType {


    MONTH(new String[]{"m", "month", "months"}, 2628000),
    DAY(new String[]{"d", "day", "days"}, 86400),
    HOUR(new String[]{"h", "hour", "hours"}, 3600),
    MINUTE(new String[]{"min", "minute", "minutes"}, 60),
    SECOND(new String[]{"s", "second", "seconds"}, 1);

    static final String[] TYPES = new String[]{"month", "day", "hour", "minute", "second"};

    private final String[] aliases;
    @Getter
    public final long multiply;

    public static Map<String, TimestampType> getMapped() {
        Map<String, TimestampType> map = new HashMap<>();
        for (TimestampType type : values()) {
            for (String s : type.aliases) {
                map.put(s, type);
            }
        }
        return map;
    }

    public static List<String> getAppropriate(int parsed) {
        List<String> list = new ArrayList<>();

        for (String type : TYPES) {
            list.add(type + (parsed == 1 ? "" : "s"));
        }
        return list;
    }
}

