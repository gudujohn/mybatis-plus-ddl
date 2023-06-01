package org.enhance.mybatis.ddl;

import org.enhance.mybatis.ddl.model.TableDefine;

import java.util.List;
import java.util.Map;

/**
 * @author JiangGengchao
 * @description
 * @classname MysqlDdlGenerate
 * @date 2022/12/5
 **/
public abstract class AbstractDdlGenerate {
    public abstract Map<String, List<String>> generate(String schema, List<TableDefine> tableDefines);
}
