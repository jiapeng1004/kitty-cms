package icu.jiapeng.kitty.user.tenant.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * 租户实体类
 *
 * @author jiapeng
 * @since 2026/2/10
 */
@Data
@TableName("kt_tenant")
public class KtTenant {
    @Id
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("name")
    private String name;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField("creator")
    private String creator;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;

    @TableField("updater")
    private String updater;
}
