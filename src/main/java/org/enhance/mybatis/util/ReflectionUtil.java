package org.enhance.mybatis.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ReflectionUtil {
    private static final Logger log = LoggerFactory.getLogger(ReflectionUtil.class);

    private ReflectionUtil() {
        throw new IllegalStateException("Constant class");
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getGenericType(final Class<?> clazz) {
        return (Class<T>) getGenericType(clazz, 0);
    }

    public static Class<?> getGenericType(final Class<?> clazz, final int index) {

        Type genericType = getDeeperGenericSuperclass(clazz);

        if (!(genericType instanceof ParameterizedType)) {
            log.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genericType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            log.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: " + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            log.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class<?>) params[index];
    }

    private static Type getDeeperGenericSuperclass(final Class<?> clazz) {
        Type genericType = clazz.getGenericSuperclass();
        if (!(genericType instanceof ParameterizedType)) {
            Class<?> superClass = clazz.getSuperclass();
            if (!superClass.equals(Object.class)) {
                return getDeeperGenericSuperclass(clazz.getSuperclass());
            }
        }
        return genericType;
    }

    public static Field[] getDeclaredField(Class<?> entityClass) {
        Field[] fields = new Field[]{};
        for (; entityClass != Object.class; entityClass = entityClass.getSuperclass()) {
            fields = ArrayUtils.addAll(fields, entityClass.getDeclaredFields());
        }
        return fields;
    }

    public static String[] getAllStringConst(Class<?> constClass) {
        Field[] fields = ReflectionUtil.getDeclaredField(constClass);
        String[] values = ArrayUtils.EMPTY_STRING_ARRAY;
        if (ArrayUtils.isNotEmpty(fields)) {
            try {
                for (Field field : fields) {
                    int modifiers = field.getModifiers();
                    if (!String.class.isAssignableFrom(field.getType()) || !Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
                        continue;
                    }
                    String value = Objects.toString(field.get(constClass));
                    values = ArrayUtils.add(values, value);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return values;
    }

    public static Map<String, Object> getAttributes(Object obj) {
        return getAttributes(obj, false);
    }

    public static Map<String, Object> getAttributes(Object obj, boolean withNullValue) {
        Map<String, Object> result = new HashMap<>();
        List<Field> fields = getAttributeFields(obj.getClass());
        if (CollectionUtils.isNotEmpty(fields)) {
            for (Field field : fields) {
                field.setAccessible(true);
                String key = field.getName();
                Object value = null;

                try {
                    value = field.get(obj);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                if (withNullValue) {
                    result.put(key, value);
                } else if (StringUtils.isNotEmpty(key) && value != null) {
                    result.put(key, value);
                }
            }
        }
        return result;
    }

    public static <T extends Object> T attributesMapping2Object(Map<String, Object> attributes, Class<T> clazz) {
        try {
            T object = clazz.newInstance();
            if (MapUtils.isNotEmpty(attributes)) {
                List<Field> fields = getAttributeFields(clazz);
                for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                    String key = attribute.getKey();
                    Object value = attribute.getValue();
                    Field field = getAttributeField(fields, key);
                    if (field != null) {
                        field.setAccessible(true);
                        field.set(object, value);
                    } else {
                        log.error("can not find class {} field {}", clazz.getName(), key);
                    }
                }
            }
            return object;
        } catch (Exception e) {
            throw new RuntimeException("attributes mapping to object failed!!!", e);
        }
    }

    private static List<Field> getAttributeFields(Class<?> clazz) {
        Field[] fields = ReflectionUtil.getDeclaredField(clazz);
        List<Field> attributeFields = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(fields)) {
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isPrivate(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
                    attributeFields.add(field);
                }
            }
        }
        return attributeFields;
    }

    private static Field getAttributeField(List<Field> fields, String fieldName) {
        assert StringUtils.isNotEmpty(fieldName) : "fieldName is ReflectionUtil.getAttributeField necessary input parameter.";
        assert CollectionUtils.isNotEmpty(fields) : "fields is ReflectionUtil.getAttributeField necessary input parameter.";
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }
}
