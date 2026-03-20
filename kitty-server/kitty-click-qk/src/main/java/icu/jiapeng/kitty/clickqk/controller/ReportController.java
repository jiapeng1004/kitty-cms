package icu.jiapeng.kitty.clickqk.controller;

import icu.jiapeng.kitty.clickqk.constants.Constant;
import icu.jiapeng.kitty.clickqk.dto.ReportReq;
import icu.jiapeng.kitty.clickqk.service.ReportService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReportController {


    @Inject
    ReportService reportService;
    /**
     * 同步事件上报
     *
     * @param ak        ak
     * @param timestamp 时间戳
     * @param req       请求体
     * @return void
     */
    @POST
    @Path("/report")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> report(@HeaderParam(Constant.X_AK) String ak,
                            @HeaderParam(Constant.X_TS) Long timestamp,
                            ReportReq req) {
        return reportService.report(ak, timestamp, req);
    }

    /**
     * 异步事件上报
     *
     * @param ak        ak
     * @param timestamp 时间戳
     * @param req       请求体
     * @return void
     */
    @POST
    @Path("/reportAsync")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> reportAsync(@HeaderParam(Constant.X_AK) String ak,
                                 @HeaderParam(Constant.X_TS) Long timestamp,
                                 ReportReq req) {
        return reportService.reportAsync(ak, timestamp, req);
    }

}

