package org.enhance.mybatis.ddl.model;

import java.util.List;

/**
 * @author JiangGengchao
 * @classname AssPlatformController
 * @description 售后服务平台接口
 * @date 2022-03-03
 **/
public class TableDefine {
    private String name;
    private List<ColumnDefine> columns;
    private String pk;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ColumnDefine> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDefine> columns) {
        this.columns = columns;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }
}