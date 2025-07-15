/*
 * Copyright [2025] [贾鹏]
 *
 * kitty-cms采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 贾鹏: jiapeng_aoa@163.com。
 * 5.不可二次分发开源参与同类竞品，如有想法可联系 贾鹏: jiapeng_aoa@163.com商议合作。
 */
package icu.jiapeng.kitty.user.runtime.service;


import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

/**
 * sql服务
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@Service
public class SqlService {

    /**
     * 生成创建表的SQL语句
     *
     * @return SQL语句
     */
    public String ddl() {
        // 获取所有的表信息
        List<TableInfo> tableInfos = TableInfoHelper.getTableInfos();
        StringJoiner sql = new StringJoiner("\n\n");
        for (TableInfo tableInfo : tableInfos) {
            // 获取表结构信息
            sql.add(buildTableDdl(tableInfo));
        }
        return sql.toString();
    }

    public String buildTableDdl(TableInfo tableInfo) {
        StringBuilder ddl = new StringBuilder();
        ddl.append("DROP TABLE IF EXISTS").append(" ").append(tableInfo.getTableName()).append(";").append("\n");
        ddl.append("CREATE TABLE `").append(tableInfo.getTableName()).append("` (\n");

        // 添加字段定义
        // 获取主键
        String primaryKey = tableInfo.getKeyColumn();
        ddl.append("`").append(primaryKey).append("`").append(" ").append(mapJavaTypeToSqlType(tableInfo.getKeyType()))
                .append(" ")
        ;
        ddl.append(" PRIMARY KEY ").append(",").append("\n");
        List<TableFieldInfo> fieldList = tableInfo.getFieldList();
        for (int i = 0; i < fieldList.size(); i++) {
            TableFieldInfo fieldInfo = fieldList.get(i);
            ddl.append("  `").append(fieldInfo.getColumn()).append("` ");

            // 映射 Java 类型到 SQL 类型
            String sqlType = mapJavaTypeToSqlType(fieldInfo.getPropertyType());
            ddl.append(" ").append(sqlType).append(" ");
            // 检查是否有非空注解
            Field field = fieldInfo.getField();
            NotNull annotation = AnnotationUtils.getAnnotation(field, NotNull.class);
            if (annotation != null) {
                ddl.append(" NOT NULL");
            } else {
                ddl.append(" NULL DEFAULT NULL ");
            }

            if (i < fieldList.size() - 1) {
                ddl.append(",");
            }
            ddl.append("\n");
        }

        ddl.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        return ddl.toString();
    }

    private String mapJavaTypeToSqlType(Class<?> javaType) {
        if (javaType == String.class) {
            return "VARCHAR(255)";
        } else if (javaType == Integer.class || javaType == int.class) {
            return "INT";
        } else if (javaType == Long.class || javaType == long.class) {
            return "BIGINT";
        } else if (javaType == Boolean.class || javaType == boolean.class) {
            return "TINYINT(1)";
        } else if (javaType == Double.class || javaType == double.class) {
            return "DOUBLE";
        } else if (javaType == Float.class || javaType == float.class) {
            return "FLOAT";
        } else if (javaType == Date.class || javaType == LocalDateTime.class) {
            return "TIMESTAMP";
        } else {
            return "TEXT";
        }
    }
}
