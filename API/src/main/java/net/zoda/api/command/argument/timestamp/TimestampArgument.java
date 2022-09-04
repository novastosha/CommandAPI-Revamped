package net.zoda.api.command.argument.timestamp;

import net.zoda.api.command.ICommand;
import net.zoda.api.command.argument.target.TargetType;
import net.zoda.api.command.argument.type.ArgumentType;
import net.zoda.api.command.argument.type.ArgumentTypeImpl;
import net.zoda.api.command.argument.type.completion.CompletionType;
import net.zoda.api.command.argument.type.completion.QuotesState;
import net.zoda.api.command.utils.Pair;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)

@ArgumentType(name = "timestamp", typeClass = Long.class, quotesState = QuotesState.REQUIRED, completionType = CompletionType.OPTIONALLY_AUTOMATIC)
public @interface TimestampArgument {

    String name();

    String description() default "No description provided";

    boolean required() default true;

    String completer() default "";

    TargetType completerType() default TargetType.FIELD;
    boolean enforceCompletions() default true;

    class Impl implements ArgumentTypeImpl<Long, TimestampArgument> {

        @Override
        public String stringify(Object value, TimestampArgument annotation, Method method, ICommand command) {
            return '"' + millisToTime((Long) value) + '"';
        }


        public String millisToTime(long time) {
            long seconds = time / 1000;
            long sec = seconds % 60;
            long minutes = seconds % 3600 / 60;
            long hours = seconds % 86400 / 3600;
            long days = seconds / 86400;
            long months = seconds / (86400*30);

            StringBuilder builder = new StringBuilder();

            if (months > 0) {
                String length = months > 1 ? "months" : "month";
                builder.append(months).append(" ").append(length).append(" ");
            }

            if (days > 0) {
                String length = days > 1 ? "days" : "day";
                builder.append(days).append(" ").append(length).append(" ");
            }

            if (hours > 0) {
                String length = hours > 1 ? "hours" : "hour";
                builder.append(hours).append(" ").append(length).append(" ");
            }
            if (minutes > 0) {
                String length = minutes > 1 ? "minutes" : "minute";
                builder.append(minutes).append(" ").append(length).append(" ");
            }
            if (sec > 0) {
                String length = sec > 1 ? "seconds" : "second";
                builder.append(sec).append(" ").append(length).append(" ");
            }

            return builder.substring(0, builder.length() - 1);
        }

        @Override
        public Pair<Long, String> fromString(String[] args, TimestampArgument annotation, Method method, ICommand command) {

            Map<TimestampType, Integer> timeMap = new HashMap<>();
            
            Map<String, TimestampType> typeMap = TimestampType.getMapped();

            for (int j = 0; j < args.length; j++) {
                String raw = args[j];

                try {
                    int parsed = Integer.parseInt(raw);
                    try {
                        String type = args[j + 1];

                        if (!typeMap.containsKey(type)) {
                            return new Pair<>(null,"Unknown timestamp type: " + type);
                        }

                        TimestampType timeStampType = typeMap.get(type);

                        if (timeMap.containsKey(timeStampType)) {
                            return new Pair<>(null,"Duplicate timestamp types: " + type);
                        }

                        timeMap.put(timeStampType, parsed);
                        j++;
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                        return new Pair<>(null,"timestamp type not found!");
                    }
                } catch (NumberFormatException ignored) {
                    String decimalsRemoved = raw.replaceAll("\\d", "");

                    if (!typeMap.containsKey(decimalsRemoved)) {
                        return new Pair<>(null,"Unknown timestamp type: " + decimalsRemoved);
                    }

                    TimestampType type = typeMap.get(decimalsRemoved);
                    String timeRemoved = raw.replaceAll(decimalsRemoved, "");

                    try {
                        int time = Integer.parseInt(timeRemoved);

                        if (timeMap.containsKey(type)) {
                            return new Pair<>(null,"Duplicate timestamp types: " + type);
                        }

                        timeMap.put(type, time);
                    } catch (NumberFormatException e) {
                        return new Pair<>(null,"Couldn't parse timestamp integer: " + timeRemoved);
                    }
                }
            }

            if (!timeMap.isEmpty()) {
                long totalAdd = 0;

                for (Map.Entry<TimestampType, Integer> entry : timeMap.entrySet()) {
                    totalAdd += (entry.getValue() * (entry.getKey().multiply * 1000));
                }
                return new Pair<>(totalAdd, null);
            }
            return new Pair<>(null, "No timestamps found!");
        }


        @Override
        public int maximumArgs(TimestampArgument annotation, Method method, ICommand command) {
            return 0;
        }
    }

}
