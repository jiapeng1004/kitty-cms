package icu.jiapeng.kitty.material.infrastructure.resource.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.material.domain.resource.constants.FileEngineTypeEnum;
import lombok.Data;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Data
@TableName("file_storage")
public class MpFileStorage {
    /**
     * 存储 ID
     */
    @TableId
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
