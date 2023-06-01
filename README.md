# mybatis-plus-ddl

## 提供基于mybatis-plus的ddl操作

## 使用

1. 基础使用和mybatis-plus一摸一样
2. 启用ddl功能：启动类上加上@EnableAutoDdl注解

### @TableField

- 默认会根据model的java类型映射对应的数据库类型
- 自定义数据类型可以通过配置TableField注解的columnDefinition实现

```demo
@TableField(value = "test_field", columnDefinition = "varchar(255) NULL DEFAULT NULL comment '测试属性'")
private String testField;
```

## 数据库支持列表

- mysql, sqlite
- 其他数据可以自行增加对应的DdlGenerate
