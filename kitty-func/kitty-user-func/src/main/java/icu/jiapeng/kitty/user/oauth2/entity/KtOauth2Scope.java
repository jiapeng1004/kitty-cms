package icu.jiapeng.kitty.user.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_oauth2_scope")
public class KtOauth2Scope extends CommonEntity {

    @TableField("scope_name")
    private String scopeName;

    @TableField("scope_code")
    private String scopeCode;

    @TableField("scope_desc")
    private String scopeDesc;
}

