package icu.jiapeng.kitty.material.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "资源 ID 列表")
public class MaterialResourceIdsDTO {

    @NotEmpty
    @Schema(description = "资源主键，非空")
    private List<String> resourceIds;
}
