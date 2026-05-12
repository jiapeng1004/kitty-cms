package icu.jiapeng.kitty.transcoder.func.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("transcode_strategy_step")
public class TranscodeStrategyStep {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属策略的根步骤 id，根步骤时等于本行 id */
    private Long rootId;
    private String strategyName;
    /** 策略级工作目录，本策略下输入/输出与 HTTP 下载均限定在此；空则用全局配置 */
    private String workDir;
    private Integer stepId;
    private String depends;
    private String type;
    private String tiAnchor;
    private String param;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
