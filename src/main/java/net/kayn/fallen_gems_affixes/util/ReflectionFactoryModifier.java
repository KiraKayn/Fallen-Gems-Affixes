package net.kayn.fallen_gems_affixes.util;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionFactoryModifier {

    private static final ReflectionFactory RF = ReflectionFactory.getReflectionFactory();
    // cache instantiators
    private static final Map<Class<?>, Constructor<?>> INSTANTIATOR_CACHE = new ConcurrentHashMap<>();
    // cache fields (including superclasses fields) for non-record class
    private static final Map<Class<?>, Field[]> FIELDS_CACHE = new ConcurrentHashMap<>();
    // cache record components for construct a new instance with canonical constructor.
    private static final Map<Class<?>, RecordComponent[]> RECORD_COMPONENTS_CACHE = new ConcurrentHashMap<>();
    // cache accessors
    private static final Map<Class<?>, Method[]> RECORD_ACCESSOR_CACHE = new ConcurrentHashMap<>();
    // cache accessors
    private static final Map<Class<?>, boolean[]> FIELD_FILTER_CACHE = new ConcurrentHashMap<>();
    // add to blackList if you can't modify
    public static final Set<Class<?>> blackList = ConcurrentHashMap.newKeySet();
    // filter field name
    public static final Set<String> fieldNameFilter = ConcurrentHashMap.newKeySet();

    public static RecordComponent[] collectRecordComponents(Class<?> clazz) {
        return clazz.getRecordComponents();
    }

    @SuppressWarnings("unchecked")
    public static <T> T copyAndModifyNumbers(T original, float multiplied) {
        if (original == null) return null;
        Class<?> clazz = original.getClass();
//        if (blackList.contains(clazz)) return original;
        try {
            if (clazz.isRecord()) {
                RecordComponent[] components = RECORD_COMPONENTS_CACHE.computeIfAbsent(clazz, ReflectionFactoryModifier::collectRecordComponents);
                Method[] accessors = RECORD_ACCESSOR_CACHE.computeIfAbsent(clazz, c -> {
                    Method[] accs = new Method[components.length];
                    for (int i = 0; i < components.length; i++) {
                        Method acc = components[i].getAccessor();
                        acc.setAccessible(true);
                        accs[i] = acc;
                    }
                    return accs;
                });
                Constructor<?> instantiator = INSTANTIATOR_CACHE.computeIfAbsent(clazz, c -> {
                    try {
                        Class<?>[] argTypes = new Class<?>[components.length];
                        for (int i = 0; i < components.length; i++) {
                            argTypes[i] = components[i].getType();
                        }
                        Constructor<?> constructor = c.getDeclaredConstructor(argTypes);
                        constructor.setAccessible(true);
                        return constructor;
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
                Object[] parameters = new Object[accessors.length];
                boolean[] filterCache = FIELD_FILTER_CACHE.computeIfAbsent(clazz, c -> {
                    boolean[] isOK = new boolean[components.length];
                    Arrays.fill(isOK, true);
                    for (int i = 0; i < components.length; i++) {
                        for (String filter : fieldNameFilter) {
                            String compName = components[i].getName().toLowerCase();
                            if (compName.startsWith(filter) || compName.endsWith(filter)) {
                                isOK[i] = false;
                                break;
                            }
                        }
                    }
                    return isOK;
                });
                for (int i = 0; i < parameters.length; i++) {
                    Method acc = accessors[i];
                    Object val = acc.invoke(original);
                    if (val instanceof Number num && filterCache[i]) {
                        Number newVal = modifyNumber(num, multiplied);
                        parameters[i] = newVal;
                    } else {
                        parameters[i] = val;
                    }
                }
                T copy = (T) instantiator.newInstance(parameters);
                return copy;
            }
            Constructor<?> instantiator = INSTANTIATOR_CACHE.computeIfAbsent(clazz, ReflectionFactoryModifier::createInstantiator);
            T copy = (T) instantiator.newInstance();

            Field[] fields = FIELDS_CACHE.computeIfAbsent(clazz, ReflectionFactoryModifier::collectAllFilteredFields);

            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> t = field.getType();

                if (t.isPrimitive()) {
                    if (t == int.class) {
                        int v = field.getInt(original);
                        field.setInt(copy, (int)(v * multiplied));
                    } else if (t == long.class) {
                        long v = field.getLong(original);
                        field.setLong(copy, (long) (v * multiplied));
                    } else if (t == double.class) {
                        double v = field.getDouble(original);
                        field.setDouble(copy, (double) (v * multiplied));
                    } else if (t == float.class) {
                        float v = field.getFloat(original);
                        field.setFloat(copy, (float) (v * multiplied));
                    } else if (t == short.class) {
                        short v = field.getShort(original);
                        field.setShort(copy, (short) (v * multiplied));
                    } else if (t == byte.class) {
                        byte v = field.getByte(original);
                        field.setByte(copy, (byte) (v * multiplied));
                    } else {
                        // boolean, char or others
                        Object val = field.get(original);
                        field.set(copy, val);
                    }
                } else {
                    // not primitive
                    Object val = field.get(original);
                    if (val instanceof Number num) {
                        Number result = (Number) modifyNumber(num, multiplied);
                        field.set(copy, result);
                    } else {
                        // shadow other objects
                        field.set(copy, val);
                    }
                }
            }

            return copy;
        } catch (Exception ignore) {
            blackList.add(original.getClass());
            return original;
        }
    }

    // create a constructor without parameter
    private static Constructor<?> createInstantiator(Class<?> clazz) {
        try {
            Constructor<?> objDef = Object.class.getDeclaredConstructor();
            Constructor<?> cons = RF.newConstructorForSerialization(clazz, objDef);
            cons.setAccessible(true);
            return cons;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instantiator for " + clazz, e);
        }
    }

    // collect all declared fields from class and superclasses (excluding Object)
    private static Field[] collectAllFilteredFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            Field[] declared = c.getDeclaredFields();
            boolean shouldAdd = true;
            for (Field field : declared) {
                String name = field.getName();
                for (String filter : fieldNameFilter) {
                    if (name.contains(filter)) {
                        Class<?> type = field.getType();
                        if (type.isPrimitive() && type != char.class && type != boolean.class || Number.class.isAssignableFrom(type)) {
                            shouldAdd = false;
                        }
                    }
                }
                if (shouldAdd) {
                    list.add(field);
                }
            }
            c = c.getSuperclass();
        }
        return list.toArray(new Field[0]);
    }

    private static Number modifyNumber(Number n, float multiplied) {
        if (n instanceof Integer i) return (int) (i * multiplied);
        if (n instanceof Long l) return (long) (l * multiplied);
        if (n instanceof Double d) return (double) (d * multiplied);
        if (n instanceof Float f) return (float) (f * multiplied);
        if (n instanceof Short s) return (short) (s * multiplied);
        if (n instanceof Byte b) return (byte) (b * multiplied);
        // BigInteger, BigDecimal ignore
        return n;
    }
}