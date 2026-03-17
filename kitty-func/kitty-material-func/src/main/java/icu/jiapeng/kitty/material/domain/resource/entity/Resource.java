package icu.jiapeng.kitty.material.domain.resource.entity;


import lombok.Data;

/**
 * 素材
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@Data
public class Resource {
    /**
     * 资源 ID
     */
    private String id;
    /**
     * 资源标题
     */
    private String title;

    /**
     * 资源栏目 ID
     */
    private String catalogId;

    /**
     * 所在栏目树形编码
     */
    private String catalogTreeCode;

    /**
     * 0: 图片 1: 视频 2: 音频 3: 文档 4: 其他
     * 资源类型
     */
    private Integer type;

    /**
     * 资源所在文件夹 ID
     */
    private String folderId;
}
