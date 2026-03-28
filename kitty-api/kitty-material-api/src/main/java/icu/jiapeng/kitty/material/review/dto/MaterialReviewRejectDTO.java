package icu.jiapeng.kitty.material.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "审核拒绝")
public record MaterialReviewRejectDTO(
        @NotBlank String taskId,
        @Schema(description = "拒绝原因") String reviewComment
) {
}
