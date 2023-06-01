package org.enhance.mybatis.ddl;

import com.baomidou.mybatisplus.annotation.DbType;
import org.apache.commons.lang3.StringUtils;
import org.enhance.mybatis.ddl.model.TableDefine;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

public class GenerateDdlFactory {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private String databaseUrl;

    public GenerateDdlFactory(String databaseUrl, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.databaseUrl = databaseUrl;
    }

    public Map<String, List<String>> generate(List<TableDefine> tableDefines, DbType dbType) {
        AbstractDdlGenerate generate;
        switch (dbType) {
            case MYSQL:
            case MARIADB:
                generate = new MysqlDdlGenerate(namedParameterJdbcTemplate.getJdbcTemplate());
                break;
            case SQLITE:
                generate = new SqliteDdlGenerate(namedParameterJdbcTemplate.getJdbcTemplate());
                break;
            default:
                throw new RuntimeException(String.format("Unsupport dbType {} generate.", dbType));
        }
        return generate.generate(getDbName(), tableDefines);
    }

    private String getDbName() {
        if (StringUtils.isEmpty(databaseUrl)) {
            return null;
        }
        String[] split1 = databaseUrl.split(":");
        String[] split2 = split1[split1.length - 1].split("/");
        String[] databaseSplit = split2[split2.length - 1].split("\\?");
        return databaseSplit[0];
    }
}