package icu.jiapeng.kitty.material.resource.vo;

import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataSnapshotVO;
import icu.jiapeng.kitty.material.review.vo.MaterialReviewTaskVO;
import icu.jiapeng.kitty.material.task.vo.MaterialResourceTaskVO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "资源详情（含最新编目快照）")
public class MaterialResourceDetailVO {

    @Schema(description = "资源基础信息")
    private MaterialResourceVO resource;

    @Schema(description = "各模板最新编目（无数据则不返回该模板）")
    private List<MaterialMetadataSnapshotVO> metadata;

    @Schema(description = "资源任务列表（未逻辑删除）")
    private List<MaterialResourceTaskVO> tasks;

    @Schema(description = "转码类任务（来自 resource_task，便于详情页分区展示）")
    private List<MaterialResourceTaskVO> transcodeTasks;

    @Schema(description = "标签/向量等多模态相关任务（按 task_type 启发式筛选，可扩展）")
    private List<MaterialResourceTaskVO> taggingTasks;

    @Schema(description = "该资源关联的通用审核任务（review_task）")
    private List<MaterialReviewTaskVO> reviewTasks;

    @Schema(description = "已登记的多码率/封面/雪碧等衍生产物（下载与展示用）")
    private List<MaterialResourceDerivativeVO> derivatives;

    public MaterialResourceVO getResource() {
        return resource;
    }

    public void setResource(MaterialResourceVO resource) {
        this.resource = resource;
    }

    public List<MaterialMetadataSnapshotVO> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<MaterialMetadataSnapshotVO> metadata) {
        this.metadata = metadata;
    }

    public List<MaterialResourceTaskVO> getTasks() {
        return tasks;
    }

    public void setTasks(List<MaterialResourceTaskVO> tasks) {
        this.tasks = tasks;
    }

    public List<MaterialResourceTaskVO> getTranscodeTasks() {
        return transcodeTasks;
    }

    public void setTranscodeTasks(List<MaterialResourceTaskVO> transcodeTasks) {
        this.transcodeTasks = transcodeTasks;
    }

    public List<MaterialResourceTaskVO> getTaggingTasks() {
        return taggingTasks;
    }

    public void setTaggingTasks(List<MaterialResourceTaskVO> taggingTasks) {
        this.taggingTasks = taggingTasks;
    }

    public List<MaterialReviewTaskVO> getReviewTasks() {
        return reviewTasks;
    }

    public void setReviewTasks(List<MaterialReviewTaskVO> reviewTasks) {
        this.reviewTasks = reviewTasks;
    }

    public List<MaterialResourceDerivativeVO> getDerivatives() {
        return derivatives;
    }

    public void setDerivatives(List<MaterialResourceDerivativeVO> derivatives) {
        this.derivatives = derivatives;
    }
}
