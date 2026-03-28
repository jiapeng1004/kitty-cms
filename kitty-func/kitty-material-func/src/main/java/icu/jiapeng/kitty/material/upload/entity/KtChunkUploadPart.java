package icu.jiapeng.kitty.material.upload.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("kt_chunk_upload_part")
@EqualsAndHashCode(callSuper = true)
public class KtChunkUploadPart extends CommonEntity {

    @TableField("session_id")
    private String sessionId;

    @TableField("chunk_index")
    private Integer chunkIndex;

    @TableField("byte_size")
    private Long byteSize;
}
