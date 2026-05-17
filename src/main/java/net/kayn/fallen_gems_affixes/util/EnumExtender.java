package net.kayn.fallen_gems_affixes.util;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

public final class EnumExtender {
    private static final Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot access sun.misc.Unsafe", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E addEnumValue(Class<E> enumClass, String name) {
        try {
            Class.forName(enumClass.getName(), true, enumClass.getClassLoader());

            Field valuesField = getField(enumClass);
            E[] existing = (E[]) valuesField.get(null);
            if (existing == null) {
                throw new RuntimeException("Enum values array is null for " + enumClass.getSimpleName());
            }

            E instance = (E) UNSAFE.allocateInstance(enumClass);
            UNSAFE.putObject(instance,
                    UNSAFE.objectFieldOffset(Enum.class.getDeclaredField("name")), name);
            UNSAFE.putInt(instance,
                    UNSAFE.objectFieldOffset(Enum.class.getDeclaredField("ordinal")), existing.length);

            E[] updated = (E[]) Array.newInstance(enumClass, existing.length + 1);
            System.arraycopy(existing, 0, updated, 0, existing.length);
            updated[existing.length] = instance;

            UNSAFE.putObject(
                    UNSAFE.staticFieldBase(valuesField),
                    UNSAFE.staticFieldOffset(valuesField),
                    updated);

            for (String cacheName : new String[]{"enumConstants", "enumConstantDirectory"}) {
                try {
                    Field cacheField = Class.class.getDeclaredField(cacheName);
                    UNSAFE.putObject(enumClass,
                            UNSAFE.objectFieldOffset(cacheField), null);
                } catch (NoSuchFieldException ignored) {}
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add " + name + " to " + enumClass.getSimpleName(), e);
        }
    }

    private static <E extends Enum<E>> @NotNull Field getField(Class<E> enumClass) {
        Field valuesField = null;
        for (Field f : enumClass.getDeclaredFields()) {
            if (f.getType().isArray() && f.getType().getComponentType() == enumClass) {
                valuesField = f;
                break;
            }
        }
        if (valuesField == null) {
            throw new RuntimeException("Cannot find enum values array field in " + enumClass.getSimpleName());
        }

        valuesField.setAccessible(true);
        return valuesField;
    }
}