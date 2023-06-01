package org.enhance.mybatis.constant;

import org.apache.ibatis.type.JdbcType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JiangGengchao
 * @classname DbMappingType
 * @date 2022-12-02
 **/
public class DbMappingType {
    private DbMappingType() {
        throw new IllegalStateException("Constant class");
    }

    public static Map<String, JdbcType> javaToJdbcTypeMap = new HashMap<>();
    public static Map<JdbcType, String> jdbcTypeToBaseSqlMap = new HashMap<>();
    static {
        javaToJdbcTypeMap.put("java.lang.String", JdbcType.VARCHAR);
        javaToJdbcTypeMap.put("java.lang.Long", JdbcType.BIGINT);
        javaToJdbcTypeMap.put("java.lang.Integer", JdbcType.INTEGER);
        javaToJdbcTypeMap.put("java.lang.Boolean", JdbcType.BOOLEAN);
        javaToJdbcTypeMap.put("java.math.BigInteger", JdbcType.BIGINT);
        javaToJdbcTypeMap.put("java.lang.Float", JdbcType.FLOAT);
        javaToJdbcTypeMap.put("java.lang.Double", JdbcType.DOUBLE);
        javaToJdbcTypeMap.put("java.math.BigDecimal", JdbcType.DECIMAL);
        javaToJdbcTypeMap.put("java.sql.Date", JdbcType.DATE);
        javaToJdbcTypeMap.put("java.util.Date", JdbcType.DATE);
        javaToJdbcTypeMap.put("java.sql.Timestamp", JdbcType.DATETIMEOFFSET);
        javaToJdbcTypeMap.put("java.sql.Time", JdbcType.TIME);
        javaToJdbcTypeMap.put("long", JdbcType.BIGINT);
        javaToJdbcTypeMap.put("int", JdbcType.INTEGER);
        javaToJdbcTypeMap.put("boolean", JdbcType.BOOLEAN);
        javaToJdbcTypeMap.put("float", JdbcType.FLOAT);
        javaToJdbcTypeMap.put("double", JdbcType.DOUBLE);
        javaToJdbcTypeMap.put("byte", JdbcType.TINYINT);
        javaToJdbcTypeMap.put("short", JdbcType.SMALLINT);
        javaToJdbcTypeMap.put("char", JdbcType.VARCHAR);

        jdbcTypeToBaseSqlMap.put(JdbcType.VARCHAR, "varchar");
        jdbcTypeToBaseSqlMap.put(JdbcType.INTEGER, "int");
        jdbcTypeToBaseSqlMap.put(JdbcType.FLOAT, "float");
        jdbcTypeToBaseSqlMap.put(JdbcType.DOUBLE, "double");
        jdbcTypeToBaseSqlMap.put(JdbcType.DATE, "datetime");
        jdbcTypeToBaseSqlMap.put(JdbcType.TIME, "datetime");
        jdbcTypeToBaseSqlMap.put(JdbcType.DECIMAL, "decimal");
        jdbcTypeToBaseSqlMap.put(JdbcType.BOOLEAN, "tinyint");
        jdbcTypeToBaseSqlMap.put(JdbcType.BIGINT, "bigint");
        jdbcTypeToBaseSqlMap.put(JdbcType.TINYINT, "tinyint");
        jdbcTypeToBaseSqlMap.put(JdbcType.SMALLINT, "smallint");
    }
}

