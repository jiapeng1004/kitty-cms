package icu.jiapeng.kitty.transcoder.func.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("transcode_access_key")
public class TranscodeAccessKey {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String accessKeyId;
    private String secretKey;
    private String name;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description;
}
