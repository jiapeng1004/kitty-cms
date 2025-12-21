package icu.jiapeng.kitty.material.domain.resource.entity;


import lombok.Data;

/**
 * 素材文件
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Data
public class MetaFile {
    /**
     * 文件 ID
     */
    private String id;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件大小(字节)
     */
    private Long size;

    /**
     * 文件相对路径
     */
    private String relaPath;

    /**
     * 存储器 ID
     */
    private String storageId;
}
