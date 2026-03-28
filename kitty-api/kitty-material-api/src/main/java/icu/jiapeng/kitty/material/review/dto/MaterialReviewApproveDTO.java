package icu.jiapeng.kitty.material.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "审核通过")
public record MaterialReviewApproveDTO(
        @NotBlank String taskId,
        @Schema(description = "审核意见，可选") String reviewComment
) {
}
