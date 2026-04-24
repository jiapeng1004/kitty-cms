package icu.jiapeng.kitty.material.transcode.vo;

import lombok.Data;

@Data
public class CatalogTranscodeBindVO {

    private String id;
    private String catalogId;
    private String strategyId;

    /** 策略名称，便于栏目编辑回显 */
    private String strategyName;

    private Integer resourceType;
    private Integer sortNum;
}
