package icu.jiapeng.kitty.material.transcode.model;

/**
 * 上传侧转码策略解析来源：显式 → 栏目绑定 → 全局默认 → 无（不主转码）。
 */
public enum TranscodeStrategySource {
    EXPLICIT,
    CATALOG,
    GLOBAL,
    NONE
}
