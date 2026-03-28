package icu.jiapeng.kitty.material.message.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 站内信持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_internal_message")
public class KtInternalMessage extends CommonEntity {

    @TableField("sender_user_id")
    private String senderUserId;

    @TableField("receiver_user_id")
    private String receiverUserId;

    @TableField("content")
    private String content;

    @TableField("message_status")
    private String messageStatus;
}
