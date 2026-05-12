package icu.jiapeng.kitty.material.review.api;

import icu.jiapeng.kitty.material.review.dto.MaterialReviewApproveDTO;
import icu.jiapeng.kitty.material.review.dto.MaterialReviewRejectDTO;
import icu.jiapeng.kitty.material.review.dto.MaterialReviewSubmitDTO;
import icu.jiapeng.kitty.material.review.vo.MaterialReviewTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 通用审核任务（OpenFeign + MVC 契约）。
 */
@Tag(name = "Material-审核")
@FeignClient(name = "kitty-mam", contextId = "materialReview")
public interface MaterialReviewApi {

    @Operation(summary = "提交审核")
    @PostMapping("/api/material/review/submit")
    MaterialReviewTaskVO submit(@Valid @RequestBody MaterialReviewSubmitDTO req);

    @Operation(summary = "审核通过")
    @PostMapping("/api/material/review/approve")
    MaterialReviewTaskVO approve(@Valid @RequestBody MaterialReviewApproveDTO req);

    @Operation(summary = "审核拒绝")
    @PostMapping("/api/material/review/reject")
    MaterialReviewTaskVO reject(@Valid @RequestBody MaterialReviewRejectDTO req);

    @Operation(summary = "按业务查询审核记录")
    @GetMapping("/api/material/review/query")
    List<MaterialReviewTaskVO> query(@RequestParam String bizType, @RequestParam String bizId);
}
