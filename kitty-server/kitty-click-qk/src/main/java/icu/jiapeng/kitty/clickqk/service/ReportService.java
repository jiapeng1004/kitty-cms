package icu.jiapeng.kitty.clickqk.service;

import icu.jiapeng.kitty.clickqk.dto.ReportReq;
import io.smallrye.mutiny.Uni;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/21
 */
public interface ReportService {
    /**
     * 同步上报
     *
     * @param ak        ak
     * @param timestamp 时间戳
     * @param reportReq 上报参数
     * @return void
     */
    Uni<Void> report(String ak, Long timestamp, ReportReq reportReq);

    /**
     * 异步上报
     *
     * @param ak        ak
     * @param timestamp 时间戳
     * @param reportReq 上报参数
     * @return void
     */
    Uni<Void> reportAsync(String ak, Long timestamp, ReportReq reportReq);
}
