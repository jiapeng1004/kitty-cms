package icu.jiapeng.kitty.material.resource.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

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
     * 文本
     */
    TEXT(4, "文本"),

    /**
     * Office 文档
     */
    OFFICE(5, "Office文档"),

    /**
     * 其他
     */
    OTHER(6, "其他"),

    /**
     * 文件夹
     */
    FOLDER(7, "文件夹"),
    ;

    private final Integer type;
    private final String desc;

    public static Optional<ResourceTypeEnum> ofType(Integer type) {
        return Arrays.stream(values()).filter(item -> item.type.equals(type)).findFirst();
    }

    public static boolean isFolder(Integer type) {
        return FOLDER.type.equals(type);
    }
}
