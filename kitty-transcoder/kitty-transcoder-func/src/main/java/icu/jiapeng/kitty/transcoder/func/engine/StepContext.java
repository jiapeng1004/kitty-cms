package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.ProbeResult;

/**
 * 步骤执行上下文：供 probe 写入探测结果、if 读取并执行子策略。
 */
public interface StepContext {

    void setCurrentStepIndex(int stepIndex);
    int getCurrentStepIndex();

    /** 当前步骤的输入来自哪一步（用于 if 从依赖步骤取 probe 结果） */
    void setInputStepIndex(int stepIndex);
    int getInputStepIndex();

    void setProbeResult(int stepIndex, ProbeResult result);
    ProbeResult getProbeResult(int stepIndex);

    /** 本步骤解析后的输出路径（由引擎根据步骤的 outputTemplate 解析后设置，执行器从此读取） */
    void setResolvedOutputPath(String path);
    String getResolvedOutputPath();

    /** 执行指定策略（子策略），入参为当前输入路径，返回该策略的最终输出路径。 */
    String runStrategy(String strategyId, String inputPath) throws Exception;

    /** 任务级水印地址（发起转码时可选传入，传则转码步骤叠加水印；支持本地路径或 HTTP URL） */
    String getWatermarkUrl();
    void setWatermarkUrl(String url);
    String getWatermarkPosition();
    void setWatermarkPosition(String position);
    String getTaskId();
    void setTaskId(String taskId);

    /** 当前策略工作目录（策略未配则用全局），输入/输出与 HTTP 下载均限定在此 */
    String getWorkDir();
    void setWorkDir(String workDir);

    /**
     * 报告当前步骤的精细进度（0-100），供转码等耗时步骤在内部循环中调用。
     * 若未设置回调则忽略。
     */
    void reportStepProgress(int percent);
}
