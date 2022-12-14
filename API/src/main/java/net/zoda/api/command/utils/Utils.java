package net.zoda.api.command.utils;

import net.zoda.api.command.argument.target.TargetType;

import java.lang.reflect.ParameterizedType;

public final class Utils {

    private Utils() {
        throw new AssertionError(getClass().getSimpleName() + " cannot be instanced!");
    }

    public static boolean isPresent(Class<?> clazz, TargetType type, String name) {
        try {
            switch (type) {
                case METHOD -> clazz.getDeclaredMethod(name);
                case FIELD -> clazz.getDeclaredField(name);
            }
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Class<?> getType(String name, TargetType target,Class<?> clazz) {
        try{
            Class<?> type = null;

            if(target.equals(TargetType.FIELD)) {
                type = clazz.getDeclaredField(name).getType();
            }else {
                type = clazz.getDeclaredMethod(name).getReturnType();
            }

            return type;
        }catch (Exception e) {
            return null;
        }
    }

    public static ParameterizedType getGenericType(String name, TargetType target,Class<?> clazz) {
        try{
            ParameterizedType type = null;

            if(target.equals(TargetType.FIELD)) {
                type = (ParameterizedType) clazz.getDeclaredField(name).getGenericType();
            }else {
                type = (ParameterizedType) clazz.getDeclaredMethod(name).getGenericReturnType();
            }

            return type;
        }catch (Exception e) {
            return null;
        }
    }
}
