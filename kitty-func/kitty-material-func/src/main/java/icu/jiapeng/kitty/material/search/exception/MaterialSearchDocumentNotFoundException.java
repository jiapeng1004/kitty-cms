package icu.jiapeng.kitty.material.search.exception;

/**
 * ES 更新时目标文档不存在，调用方可回退为整文档索引。
 */
public class MaterialSearchDocumentNotFoundException extends RuntimeException {

    public MaterialSearchDocumentNotFoundException(String resourceId) {
        super("ES document not found: " + resourceId);
    }
}
