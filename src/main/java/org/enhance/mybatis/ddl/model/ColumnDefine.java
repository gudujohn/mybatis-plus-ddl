package org.enhance.mybatis.ddl.model;

/**
 * @author JiangGengchao
 * @classname AssPlatformController
 * @description 售后服务平台接口
 * @date 2022-03-03
 **/
public class ColumnDefine {
    private String columnName;
    private String columnDefinition;
    private boolean pk = false;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnDefinition() {
        return columnDefinition;
    }

    public void setColumnDefinition(String columnDefinition) {
        this.columnDefinition = columnDefinition;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }
}
