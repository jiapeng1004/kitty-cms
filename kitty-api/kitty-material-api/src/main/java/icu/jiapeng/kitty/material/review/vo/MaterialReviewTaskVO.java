package icu.jiapeng.kitty.material.review.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "审核任务 VO")
public record MaterialReviewTaskVO(
        String id,
        String bizType,
        String bizId,
        String submitUserId,
        String reviewUserId,
        @Schema(description = "pending / approved / rejected") String status,
        String reviewComment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
