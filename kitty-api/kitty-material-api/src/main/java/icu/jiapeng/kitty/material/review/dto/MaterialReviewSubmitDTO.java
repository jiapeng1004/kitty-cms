package icu.jiapeng.kitty.material.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "提交审核")
public record MaterialReviewSubmitDTO(
        @NotBlank @Schema(description = "业务类型，如 material_resource") String bizType,
        @NotBlank @Schema(description = "业务主键，如 resource_id") String bizId
) {
}
