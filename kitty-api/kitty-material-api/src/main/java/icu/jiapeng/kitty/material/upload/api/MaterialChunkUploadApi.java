package icu.jiapeng.kitty.material.upload.api;

import icu.jiapeng.kitty.material.upload.dto.MaterialChunkUploadPartReportDTO;
import icu.jiapeng.kitty.material.upload.dto.MaterialChunkUploadSessionCreateDTO;
import icu.jiapeng.kitty.material.upload.vo.MaterialChunkUploadSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 分片上传会话 API（5.5）。
 */
@Tag(name = "Material-分片上传")
public interface MaterialChunkUploadApi {

    @Operation(summary = "创建上传会话")
    @PostMapping("/api/material/upload/chunk/session")
    MaterialChunkUploadSessionVO createSession(@Valid @RequestBody MaterialChunkUploadSessionCreateDTO req);

    @Operation(summary = "查询会话与已登记分片（续传）")
    @GetMapping("/api/material/upload/chunk/session/{sessionId}")
    MaterialChunkUploadSessionVO getSession(@PathVariable String sessionId);

    @Operation(summary = "登记单个分片完成（占位，供续传合并校验）")
    @PostMapping("/api/material/upload/chunk/session/{sessionId}/part")
    MaterialChunkUploadSessionVO reportPart(
            @PathVariable String sessionId,
            @Valid @RequestBody MaterialChunkUploadPartReportDTO req);

    @Operation(summary = "完成会话（校验分片齐全，不写真实合并）")
    @PostMapping("/api/material/upload/chunk/session/{sessionId}/complete")
    MaterialChunkUploadSessionVO completeSession(@PathVariable String sessionId);

    @Operation(summary = "取消会话并清理已登记分片")
    @PostMapping("/api/material/upload/chunk/session/{sessionId}/cancel")
    MaterialChunkUploadSessionVO cancelSession(@PathVariable String sessionId);

    @Operation(summary = "分片二进制上传（Content-Length + Content-Range: bytes a-b/total，校验后登记分片）")
    @PostMapping(value = "/api/material/upload/chunk/session/{sessionId}/chunk", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    MaterialChunkUploadSessionVO uploadChunkWithHttpHeaders(
            @PathVariable String sessionId,
            @RequestHeader(value = HttpHeaders.CONTENT_LENGTH, required = false) String contentLength,
            @RequestHeader(HttpHeaders.CONTENT_RANGE) String contentRange,
            @RequestBody(required = false) byte[] body);
}
