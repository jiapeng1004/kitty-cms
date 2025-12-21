package icu.jiapeng.kitty.material.domain.resource.entity;


import icu.jiapeng.kitty.material.domain.resource.constants.FileEngineTypeEnum;
import lombok.Data;

/**
 * 素材存储
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Data
public class FileStorage {
    /**
     * 存储 ID
     */
    private String id;

    /**
     * 存储名称
     */
    private String name;

    /**
     * 存储引擎类型
     * {@link FileEngineTypeEnum#getType()}
     */
    private Integer fileEngineType;

    /**
     * 存储配置
     */
    private String config;
}