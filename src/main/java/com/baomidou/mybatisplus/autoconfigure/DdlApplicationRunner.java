//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.ddl.DdlHelper;
import com.baomidou.mybatisplus.extension.ddl.IDdl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.enhance.mybatis.ddl.GenerateDdlFactory;
import org.enhance.mybatis.ddl.annotation.EnableAutoDdl;
import org.enhance.mybatis.ddl.model.TableDefine;
import org.enhance.mybatis.util.DdlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author JiangGengchao
 * @classname DdlApplicationRunner
 * @date 2023-06-01
 **/
public class DdlApplicationRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DdlApplicationRunner.class);
    @Autowired(required = false)
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired(required = false)
    private ResourcePatternResolver resourcePatternResolver;
    private List<IDdl> ddlList;
    private ApplicationContext applicationContext;
    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    public DdlApplicationRunner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public DdlApplicationRunner(List<IDdl> ddlList) {
        this.ddlList = ddlList;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.debug("  ...  DDL start create  ...  ");
        if (ObjectUtils.isNotEmpty(this.ddlList)) {
            this.ddlList.forEach((ddl) -> {
                ddl.runScript((dataSource) -> {
                    DdlHelper.runScript(ddl.getDdlGenerator(), dataSource, ddl.getSqlFiles(), true);
                });
            });
        } else {
            // step1:获取扫描路径
            Set<String> scanPackages = getScanPackages();
            log.debug("maybatis ddl scan pakcages:{}", scanPackages);
            Map<Class<? extends Annotation>, Set<Class<?>>> scanResult = scannerTargeAnnotaionClass(scanPackages, TableName.class, EnableAutoDdl.class);
            if (scanResult.get(EnableAutoDdl.class) == null || !scanResult.get(EnableAutoDdl.class).stream().findFirst().isPresent()) {
                return;
            }

            Set<Class<?>> modelTableClasses = scanResult.get(TableName.class);
            if (CollectionUtils.isEmpty(modelTableClasses)) {
                log.info("There is no table model need to init.");
                return;
            }
            String driverClassName = getProperty("spring.datasource.driverClassName");
            DbType dbType = DdlUtil.getDbType(driverClassName);
            List<TableDefine> tableDefines = DdlUtil.parseTableDefines(modelTableClasses, dbType);
            // 生成相应ddl sql
            GenerateDdlFactory generateDdlFactory = new GenerateDdlFactory(getProperty("spring.datasource.url"), this.namedParameterJdbcTemplate);
            Map<String, List<String>> ddls = generateDdlFactory.generate(tableDefines, dbType);
            if (MapUtils.isEmpty(ddls)) {
                return;
            }
            // 执行脚本
            JdbcTemplate jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
            log.info("========>execute auto ddl sql begin.");
            for (Map.Entry<String, List<String>> entry : ddls.entrySet()) {
                log.info(entry.getKey());
                for (String sql : entry.getValue()) {
                    log.debug("========>execute ddl sql:{}", sql);
                    jdbcTemplate.execute(sql);
                }
            }
            log.info("========>execute auto ddl sql success.");
        }
        log.debug("  ...  DDL end create  ...  ");
    }

    private Set<String> getScanPackages() {
        Map<String, Object> annotatedBeans = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        SpringBootApplication annotation = annotatedBeans.values().toArray()[0].getClass().getAnnotation(SpringBootApplication.class);
        String[] scanPackageStrs = annotation.scanBasePackages();
        Set<String> scanPackages = new HashSet<>();
        if (ArrayUtils.isNotEmpty(scanPackageStrs)) {
            for (String scanPackageStr : scanPackageStrs) {
                scanPackages.add(scanPackageStr);
            }
        }

        String name = annotatedBeans.values().toArray()[0].getClass().getName();
        String defaultPackage = name.substring(0, name.lastIndexOf("."));
        scanPackages.add(defaultPackage);
        return scanPackages;
    }

    private Map<Class<? extends Annotation>, Set<Class<?>>> scannerTargeAnnotaionClass(Set<String> basePackages, Class<? extends Annotation>... targetAnnotations) {
        Map<Class<? extends Annotation>, Set<Class<?>>> result = new HashMap<>();
        MetadataReaderFactory metadataReaderFactory = this.applicationContext.getBean(MetadataReaderFactory.class);

        for (String basePackage : basePackages) {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    resolveBasePackage(basePackage) + '/' + DEFAULT_RESOURCE_PATTERN;

            try {
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
                        String className = metadataReader.getClassMetadata().getClassName();
                        try {
                            for (Class<? extends Annotation> targetAnnotation : targetAnnotations) {
                                if (annotationMetadata.hasAnnotation(targetAnnotation.getName())) {
                                    Set<Class<?>> classSet = result.get(targetAnnotation);
                                    if (classSet == null) {
                                        classSet = new HashSet<>();
                                    }
                                    classSet.add(Class.forName(className));
                                    result.put(targetAnnotation, classSet);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            log.error("Reflection error:{}-msg:{}", className, e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Reflection io exception:{}", e.getMessage());
            }
        }

        return result;
    }

    private String resolveBasePackage(String basePackage) {
        Environment environment = this.applicationContext.getEnvironment();
        return ClassUtils.convertClassNameToResourcePath(environment.resolveRequiredPlaceholders(basePackage));
    }

    private String getProperty(String key) {
        String value = System.getProperty(key);
        if (StringUtils.isEmpty(value)) {
            value = this.applicationContext.getEnvironment().getProperty(key);
        }
        return value;
    }
}