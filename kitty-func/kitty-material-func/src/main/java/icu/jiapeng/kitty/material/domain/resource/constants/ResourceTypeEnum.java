package icu.jiapeng.kitty.material.domain.resource.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@AllArgsConstructor
@Getter
public enum ResourceTypeEnum {

    /**
     * 视频
     */
    VIDEO(1, "视频"),

    /**
     * 音频
     */
    AUDIO(2, "音频"),

    /**
     * 图片
     */
    IMAGE(3, "图片"),

    /**
     * 文档
     */
    DOCUMENT(4, "文档"),

    /**
     * 文件夹
     */
    FOLDER(5, "文件夹"),

    /**
     * 其他
     */
    OTHER(6, "其他"),
    ;

    private final Integer type;
    private final String desc;
}
