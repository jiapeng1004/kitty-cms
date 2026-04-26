package icu.jiapeng.kitty.material.resource.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@TableName("kt_resource")
@Data
@EqualsAndHashCode(callSuper = true)
public class KtResource extends CommonEntity {
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
     * 资源类型，见 ResourceTypeEnum。
     */
    @TableField("type")
    private Integer type;

    /**
     * 父资源 ID（文件夹）。无父级固定为 0。
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 文件大小（字节）。
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件指纹（大小 + 分段 CRC 归一化串）。
     */
    @TableField("fingerprint")
    private String fingerprint;

    /**
     * 逻辑删/回收站：0 正常、1 已入回收站；与全局 logic-not-delete-value / logic-delete-value 一致，插入时由公共填充置 0。
     */
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
