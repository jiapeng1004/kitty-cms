package icu.jiapeng.kitty.material.domain.resource.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

/**
 * 存储引擎类型
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@AllArgsConstructor
@Getter
public enum FileEngineTypeEnum {

    /**
     * 磁盘存储
     */
    DISK(0, "磁盘存储"),

    /**
     * 对象存储
     */
    OBJECT_STORAGE(1, "对象存储"),
    ;
    private final Integer type;

    private final String name;

    public static Optional<FileEngineTypeEnum> getByType(Integer type) {
        for (FileEngineTypeEnum value : FileEngineTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
