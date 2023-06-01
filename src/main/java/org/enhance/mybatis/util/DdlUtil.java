package org.enhance.mybatis.util;

import com.baomidou.mybatisplus.annotation.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.enhance.mybatis.constant.DbMappingType;
import org.enhance.mybatis.ddl.model.ColumnDefine;
import org.enhance.mybatis.ddl.model.TableDefine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author JiangGengchao
 * @description
 * @classname DdlUtil
 * @date 2023/6/1
 **/
public class DdlUtil {
    private static final DbType[] TYPE_AUTOID = {DbType.MYSQL, DbType.SQLITE};

    public static DbType getDbType(String driverClassName) {
        if (StringUtils.contains(driverClassName, "mysql")) {
            return DbType.MYSQL;
        } else if (StringUtils.contains(driverClassName, "sqlite")) {
            return DbType.SQLITE;
        }
        // TODO other sql type
        return DbType.OTHER;
    }

    public static List<TableDefine> parseTableDefines(Set<Class<?>> classes, DbType dbType) {
        List<TableDefine> tableDefines = new ArrayList<>();
        for (Class<?> tableClazz : classes) {
            TableDefine tableDefine = new TableDefine();

            TableName tableName = tableClazz.getAnnotation(TableName.class);
            tableDefine.setName(tableName.value());

            List<ColumnDefine> columnDefines = new ArrayList<>();
            Field[] declaredFields = ReflectionUtil.getDeclaredField(tableClazz);
            for (Field field : declaredFields) {
                if (!field.isAnnotationPresent(TableField.class) && !field.isAnnotationPresent(TableId.class)) {
                    continue;
                }
                ColumnDefine columnDefine = getColumnDefine(field, dbType);
                columnDefines.add(columnDefine);
            }
            tableDefine.setColumns(columnDefines);

            tableDefines.add(tableDefine);
        }
        return tableDefines;
    }

    private static ColumnDefine getColumnDefine(Field field, DbType dbType) {
        ColumnDefine columnDefine = new ColumnDefine();

        if (field.isAnnotationPresent(TableId.class)) {
            TableId idAnnotation = field.getAnnotation(TableId.class);
            String columnName = field.getName();
            if (StringUtils.isNotEmpty(idAnnotation.value())) {
                columnName = idAnnotation.value();
            }
            columnDefine.setColumnName(columnName);

            JdbcType jdbcType = DbMappingType.javaToJdbcTypeMap.get(field.getType().getName());
            String dbTypeStr = DbMappingType.jdbcTypeToBaseSqlMap.get(jdbcType);

            StringBuilder columnDefinitionSb = new StringBuilder();
            columnDefinitionSb.append(dbTypeStr);
            if (dbTypeStr.contains("varchar")) {
                columnDefinitionSb.append("(").append(255).append(")");
            }
            columnDefinitionSb.append(" NOT NULL");
            if (jdbcType == JdbcType.BIGINT) {
                if (idAnnotation.type() == IdType.AUTO || (idAnnotation.type() == IdType.NONE)) {
                    switch (dbType) {
                        case MYSQL:
                        case MARIADB:
                            columnDefinitionSb.append(" AUTO_INCREMENT");
                            break;
                        case SQLITE:
                            columnDefinitionSb.append(" AUTOINCREMENT");
                            break;
                        default:
                    }
                }
            }
            columnDefine.setColumnDefinition(columnDefinitionSb.toString());
            columnDefine.setPk(true);
        } else {
            TableField columnAnnotation = field.getAnnotation(TableField.class);

            String columnName = StringUtils.isNotEmpty(columnAnnotation.value()) ? columnAnnotation.value() : field.getName();
            columnDefine.setColumnName(columnName);

            StringBuilder columnDefinitionSb = new StringBuilder();
            if (StringUtils.isNotEmpty(columnAnnotation.columnDefinition())) {
                columnDefinitionSb.append(columnAnnotation.columnDefinition());
            } else {
                JdbcType jdbcType = columnAnnotation.jdbcType() == null ? columnAnnotation.jdbcType() : DbMappingType.javaToJdbcTypeMap.get(field.getType().getName());
                String dbTypeStr = DbMappingType.jdbcTypeToBaseSqlMap.get(jdbcType);

                columnDefinitionSb.append(" ").append(dbTypeStr);
                if (dbTypeStr.contains("varchar")) {
                    columnDefinitionSb.append("(").append(255).append(")");
                }
                columnDefinitionSb.append("NULL DEFAULT NULL");
            }
            columnDefine.setColumnDefinition(columnDefinitionSb.toString());
        }

        return columnDefine;
    }
}
