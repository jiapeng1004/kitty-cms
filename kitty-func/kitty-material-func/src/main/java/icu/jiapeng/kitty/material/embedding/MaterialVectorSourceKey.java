package icu.jiapeng.kitty.material.embedding;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * 向量来源标识：多平台/多模型并存时用原始字符串唯一区分（如 {@code none}、{@code openai:text-embedding-3-small}）。
 */
public record MaterialVectorSourceKey(String code) {

    public static final String CODE_NONE = "none";

    public MaterialVectorSourceKey(String code) {
        this.code = Objects.requireNonNull(code, "code").trim();
        if (this.code.isEmpty()) {
            throw new IllegalArgumentException("code must not be blank");
        }
    }

    public static MaterialVectorSourceKey none() {
        return new MaterialVectorSourceKey(CODE_NONE);
    }

    public boolean isNone() {
        return CODE_NONE.equalsIgnoreCase(code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaterialVectorSourceKey that = (MaterialVectorSourceKey) o;
        return code.equalsIgnoreCase(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code.toLowerCase());
    }

    @NonNull
    @Override
    public String toString() {
        return code;
    }
}
