package icu.jiapeng.kitty.material.infrastructure.resource.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@TableName("resource")
public class MpResource {
    /**
     * 资源 ID
     */
    @TableId
    private String id;
    /**
     * 资源标题
     */
    @TableField("title")
    private String title;

    /**
     * 资源栏目 ID
     */
    @TableField("catalog_id")
    private String catalogId;

    /**
     * 所在栏目树形编码
     */
    @TableField("catalog_tree_code")
    private String catalogTreeCode;

    /**
     * 0: 图片 1: 视频 2: 音频 3: 文档 4: 其他
     * 资源类型
     */
    @TableField("type")
    private Integer type;

    /**
     * 资源所在文件夹 ID
     */
    @TableField("folder_id")
    private String folderId;
}
