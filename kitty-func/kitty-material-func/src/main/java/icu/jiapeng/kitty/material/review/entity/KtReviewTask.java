package icu.jiapeng.kitty.material.review.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 通用审核任务（表 {@code review_task}）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_review_task")
public class KtReviewTask extends CommonEntity {

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private String bizId;

    @TableField("submit_user_id")
    private String submitUserId;

    @TableField("review_user_id")
    private String reviewUserId;

    @TableField("status")
    private String status;

    @TableField("review_comment")
    private String reviewComment;
}
