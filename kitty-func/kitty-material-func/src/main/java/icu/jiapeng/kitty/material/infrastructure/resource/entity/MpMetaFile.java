package icu.jiapeng.kitty.material.infrastructure.resource.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * 文件JDBC 实体
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Data
@TableName("meta_file")
public class MpMetaFile {
    /**
     * 文件 ID
     */
    @Id
    private String id;

    /**
     * 文件名
     */
    @TableField("name")
    private String name;

    /**
     * 文件大小(字节)
     */
    @TableField("size")
    private Long size;

    /**
     * 文件相对路径
     */
    @TableField("rela_path")
    private String relaPath;

    /**
     * 存储器 ID
     */
    @TableField("storage_id")
    private String storageId;
}
