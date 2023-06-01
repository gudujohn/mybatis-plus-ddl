package org.enhance.mybatis.ddl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.enhance.mybatis.ddl.model.ColumnDefine;
import org.enhance.mybatis.ddl.model.TableDefine;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author JiangGengchao
 * @description
 * @classname SqliteDdlGenerate
 * @date 2022/12/7
 **/
public class SqliteDdlGenerate extends AbstractDdlGenerate {
    private JdbcTemplate jdbcTemplate;

    public SqliteDdlGenerate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, List<String>> generate(String schema, List<TableDefine> tableDefines) {
        Map<String, List<String>> ddlSqlResult = new HashMap<>();
        for (TableDefine tableDefine : tableDefines) {
            List<String> ddlSql = new ArrayList<>();
            // 直接create
//            ddlSql.add(System.getProperty("line.separator")+"DROP TABLE IF EXISTS "+tableDefine.getName()+";");
//            ddlSql.add(generateCreateTableSql(tableDefine));

            // 判断表是否存在
            boolean tableExist = tableExist(schema, tableDefine.getName());
            if (tableExist) {
                List<String> gapSql = generateAlterProperties(schema, tableDefine);
                if (CollectionUtils.isNotEmpty(gapSql)) {
                    ddlSql.addAll(gapSql);
                }
            } else {
                ddlSql.add(generateCreateTableSql(tableDefine));
            }
            if (CollectionUtils.isNotEmpty(ddlSql)) {
                ddlSqlResult.put(tableDefine.getName(), ddlSql);
            }
        }
        return ddlSqlResult;
    }

    private String generateCreateTableSql(TableDefine tableDefine) {
        StringBuilder ddlSb = new StringBuilder();
        ddlSb.append(System.getProperty("line.separator"));
        ddlSb.append("CREATE TABLE ").append(tableDefine.getName());
        ddlSb.append("(").append(System.getProperty("line.separator"));
        int size = tableDefine.getColumns().size();
        for (int i = 0; i < size; i++) {
            ColumnDefine column = tableDefine.getColumns().get(i);
            ddlSb.append(column.getColumnName()).append(" ").append(column.getColumnDefinition());
            if (column.isPk()) {
                ddlSb.append(" PRIMARY KEY");
            }
            if (i < size - 1) {
                ddlSb.append(",").append(System.getProperty("line.separator"));
            }
        }
        ddlSb.append(");");
        return ddlSb.toString();
    }

    private boolean tableExist(String schema, String tableName) {
        int existCount = jdbcTemplate.queryForObject("select count(1) from sqlite_master where type='table' and name=?", Integer.class, tableName);
        return existCount != 0;
    }

    private List<String> generateAlterProperties(String schema, TableDefine tableDefine) {
        if (CollectionUtils.isEmpty(tableDefine.getColumns())) {
            return null;
        }

        List<String> columnAlterDdlSql = new ArrayList<>();
        List<ColumnDefine> notExistColumns;
        List<Map<String, Object>> metaDatas = jdbcTemplate.queryForList(String.format("PRAGMA table_info(%s)", tableDefine.getName()));
        if (CollectionUtils.isNotEmpty(metaDatas)) {
            notExistColumns = tableDefine.getColumns().stream().filter(column -> metaDatas.stream().noneMatch(metaData -> StringUtils.equals(MapUtils.getString(metaData, "name"), column.getColumnName()))).collect(Collectors.toList());
        } else {
            notExistColumns = tableDefine.getColumns();
        }
        if (CollectionUtils.isNotEmpty(notExistColumns)) {
            for (ColumnDefine notExistColumn : notExistColumns) {
                StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE ");
                sqlBuilder.append(schema).append(".").append(tableDefine.getName()).append(" ADD COLUMN ").append(notExistColumn.getColumnName()).append(" ").append(notExistColumn.getColumnDefinition()).append(";");
                columnAlterDdlSql.add(sqlBuilder.toString());
            }
        }

        return columnAlterDdlSql;
    }
}
