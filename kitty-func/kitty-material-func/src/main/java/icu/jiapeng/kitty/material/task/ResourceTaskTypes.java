package icu.jiapeng.kitty.material.task;

public final class ResourceTaskTypes {

    private ResourceTaskTypes() {
    }

    public static final String TRANSCODE = "transcode";

    /** 雪碧图/缩略长图占位，可与转码主任务解耦重试 */
    public static final String VIDEO_SPRITE = "video_sprite";
}
