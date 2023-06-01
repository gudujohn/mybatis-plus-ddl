package org.enhance.mybatis.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.enhance.mybatis.constant.BaseModelConst;

import java.io.Serializable;
import java.util.*;

/**
 * @author JiangGengchao
 * @classname ModelUtil
 * @date 2023-06-01
 **/
public class ModelUtil {

    private ModelUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final String ERROR_FORMAT_GET_PROPERTY = "getProperty() error, class=%s, propertyName=%s";

    public static <T extends Object, S extends Serializable> Map<S, T> convert2CacheMap(List<T> models) {
        return convertByForeignKeyCacheMap(models, BaseModelConst.ATTRIBUTE_ID);
    }

    public static <T extends Object, S extends Serializable> Map<S, T> convertByForeignKeyCacheMap(List<T> models, String foreignKeyCode) {
        Map<S, T> cachMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(models)) {
            for (T model : models) {
                Object foreignKeyValue = getProperty(model, foreignKeyCode);
                if (Objects.nonNull(foreignKeyValue)) {
                    cachMap.put((S) foreignKeyValue, model);
                }
            }
        }
        return cachMap;
    }

    public static <T extends Object, S extends Object> Map<S, T> convertByForeignPropertyKey(List<T> entities, String foreignPropertyKeyCode, Class<?> K) {
        Map<S, T> cachMap = new LinkedHashMap<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(entities)) {
            for (T t : entities) {
                Object foreignKeyValue = (S) getProperty(t, foreignPropertyKeyCode);
                if (Objects.nonNull(foreignKeyValue)) {
                    cachMap.put((S) foreignKeyValue, t);
                }
            }
        }
        return cachMap;
    }

    public static <T extends Object, S extends Serializable> Map<S, List<T>> groupByForeignKeyCode(List<T> entities, String foreignKeyCode) {
        Map<S, List<T>> entityGroupMap = new LinkedHashMap<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(entities)) {
            for (T t : entities) {
                Object foreignKeyValue = getProperty(t, foreignKeyCode);
                if (Objects.nonNull(foreignKeyValue)) {
                    if (!entityGroupMap.containsKey(foreignKeyValue)) {
                        entityGroupMap.put((S) foreignKeyValue, new ArrayList<>());
                    }
                    entityGroupMap.get(foreignKeyValue).add(t);
                }
            }
        }
        return entityGroupMap;
    }

    public static <T extends Object, K extends Object> Map<K, List<T>> groupByPropertyKeyCode(List<T> entities, String propertyKeyCode, Class k) {
        Map<K, List<T>> entityGroupMap = new LinkedHashMap<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(entities)) {
            for (T t : entities) {
                K propertyKeyValue = (K) getProperty(t, propertyKeyCode);
                if (propertyKeyValue != null) {
                    if (!entityGroupMap.containsKey(propertyKeyValue)) {
                        entityGroupMap.put(propertyKeyValue, new ArrayList<>());
                    }
                    entityGroupMap.get(propertyKeyValue).add(t);
                }
            }
        }
        return entityGroupMap;
    }

    public static <T extends Object> Object getProperty(T model, String propertyKeyCode) {
        try {
            return PropertyUtils.getProperty(model, propertyKeyCode);
        } catch (Exception ex) {
            String errorMessage = String.format(ERROR_FORMAT_GET_PROPERTY, model.getClass().getSimpleName(), propertyKeyCode);
            throw new RuntimeException(errorMessage, ex);
        }
    }

    public static <T extends Object> List<List<T>> spliteModel(List<T> models, int spliteSize) {
        if (CollectionUtils.isEmpty(models)) {
            return null;
        }
        List<List<T>> sm = new ArrayList<>();
        if (models.size() > spliteSize) {
            int count = 0;
            List<T> group = new ArrayList<>();
            for (T model : models) {
                if (count >= spliteSize) {
                    count = 0;
                    sm.add(group);
                    group = new ArrayList<>();
                } else {
                    group.add(model);
                }
                count++;
            }
        } else {
            sm.add(models);
        }
        return sm;
    }

}