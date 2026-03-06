package icu.jiapeng.kitty.transcoder.func.grpc;

import icu.jiapeng.kitty.transcoder.api.*;
import icu.jiapeng.kitty.transcoder.func.strategy.StrategyService;
import icu.jiapeng.kitty.transcoder.func.task.TaskService;
import icu.jiapeng.kitty.transcoder.grpc.*;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class TranscodeGrpcServiceImpl extends TranscodeServiceGrpc.TranscodeServiceImplBase {

    @Resource
    private TaskService taskService;
    @Resource
    private StrategyService strategyService;

    @Override
    public void createTask(CreateTaskReq request, StreamObserver<CreateTaskResp> responseObserver) {
        try {
            CreateTaskRequest req = new CreateTaskRequest();
            req.setInputType(request.getInputType().isEmpty() ? "DISK" : request.getInputType());
            req.setInputPath(request.getInputPath());
            req.setStrategyId(request.getStrategyId());
            req.setPriority(request.getPriority() > 0 ? request.getPriority() : 5);
            req.setWatermarkUrl(request.getWatermarkUrl().isEmpty() ? null : request.getWatermarkUrl());
            req.setWatermarkPosition(request.getWatermarkPosition().isEmpty() ? null : request.getWatermarkPosition());
            if (request.getNotificationsCount() > 0) {
                req.setNotifications(request.getNotificationsList().stream()
                        .map(n -> {
                            icu.jiapeng.kitty.transcoder.api.NotificationConfig c = new icu.jiapeng.kitty.transcoder.api.NotificationConfig();
                            c.setMethod(n.getMethod());
                            c.setTarget(n.getTarget());
                            return c;
                        })
                        .collect(Collectors.toList()));
            }
//            String accessKeyId = TranscoderGrpcAuthInterceptor.ACCESS_KEY_ID_CTX.get();
            String taskId = taskService.createTask(req, null);
            responseObserver.onNext(CreateTaskResp.newBuilder().setTaskId(taskId).build());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asException());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getTask(GetTaskReq request, StreamObserver<TaskResp> responseObserver) {
        try {
            TaskVO vo = taskService.getTask(request.getTaskId());
            if (vo == null) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND.asException());
                return;
            }
            TaskResp.Builder b = TaskResp.newBuilder()
                    .setId(vo.getId())
                    .setStatus(vo.getStatus() != null ? vo.getStatus() : "")
                    .setProgress(vo.getProgress() != null ? vo.getProgress() : 0)
                    .setInputType(vo.getInputType() != null ? vo.getInputType() : "")
                    .setInputPath(vo.getInputPath() != null ? vo.getInputPath() : "")
                    .setStrategyId(vo.getStrategyId() != null ? vo.getStrategyId() : "");
            if (vo.getOutputPath() != null) b.setOutputPath(vo.getOutputPath());
            if (vo.getOutputHttpUrl() != null) b.setOutputHttpUrl(vo.getOutputHttpUrl());
            if (vo.getWatermarkUrl() != null) b.setWatermarkUrl(vo.getWatermarkUrl());
            if (vo.getWatermarkPosition() != null) b.setWatermarkPosition(vo.getWatermarkPosition());
            if (vo.getCreatedAt() != null) b.setCreatedAt(vo.getCreatedAt());
            if (vo.getStartedAt() != null) b.setStartedAt(vo.getStartedAt());
            if (vo.getCompletedAt() != null) b.setCompletedAt(vo.getCompletedAt());
            if (vo.getErrorMessage() != null) b.setErrorMessage(vo.getErrorMessage());
            responseObserver.onNext(b.build());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asException());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listTasks(ListTasksReq request, StreamObserver<ListTasksResp> responseObserver) {
        try {
            int page = request.getPage() > 0 ? request.getPage() : 1;
            int size = request.getSize() > 0 ? request.getSize() : 20;
            List<TaskVO> list = taskService.listTasks(page, size, null);
            ListTasksResp.Builder b = ListTasksResp.newBuilder();
            for (TaskVO vo : list) {
                b.addTasks(toTaskResp(vo));
            }
            responseObserver.onNext(b.build());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asException());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void cancelTask(CancelTaskReq request, StreamObserver<CancelTaskResp> responseObserver) {
        try {
            boolean ok = taskService.cancelTask(request.getTaskId());
            responseObserver.onNext(CancelTaskResp.newBuilder().setSuccess(ok).build());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asException());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getProgress(GetProgressReq request, StreamObserver<ProgressResp> responseObserver) {
        try {
            ProgressVO vo = taskService.getProgress(request.getTaskId());
            ProgressResp.Builder b = ProgressResp.newBuilder()
                    .setTaskId(vo.getTaskId())
                    .setProgress(vo.getProgress() != null ? vo.getProgress() : 0)
                    .setStatus(vo.getStatus() != null ? vo.getStatus() : "");
            if (vo.getCurrentStep() != null) b.setCurrentStep(vo.getCurrentStep());
            if (vo.getTotalSteps() != null) b.setTotalSteps(vo.getTotalSteps());
            if (vo.getStepProgressList() != null) {
                for (icu.jiapeng.kitty.transcoder.api.StepProgressItem item : vo.getStepProgressList()) {
                    b.addStepProgressList(icu.jiapeng.kitty.transcoder.grpc.StepProgressItem.newBuilder()
                            .setStepId(item.getStepId() != null ? item.getStepId() : 0)
                            .setType(item.getType() != null ? item.getType() : "")
                            .setName(item.getName() != null ? item.getName() : "")
                            .setStatus(item.getStatus() != null ? item.getStatus() : "")
                            .setProgress(item.getProgress() != null ? item.getProgress() : 0)
                            .build());
                }
            }
            responseObserver.onNext(b.build());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asException());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getStrategies(GetStrategiesReq request, StreamObserver<GetStrategiesResp> responseObserver) {
        try {
            List<StrategyVO> list = strategyService.getStrategies();
            GetStrategiesResp.Builder b = GetStrategiesResp.newBuilder();
            for (StrategyVO vo : list) {
                b.addStrategies(toStrategyResp(vo));
            }
            responseObserver.onNext(b.build());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asException());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getStrategy(GetStrategyReq request, StreamObserver<StrategyResp> responseObserver) {
        try {
            StrategyVO vo = strategyService.getStrategy(request.getStrategyId());
            if (vo == null) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND.asException());
                return;
            }
            responseObserver.onNext(toStrategyResp(vo));
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asException());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void createStrategy(CreateStrategyReq request, StreamObserver<CreateStrategyResp> responseObserver) {
        try {
            CreateStrategyRequest req = reqFromProto(request);
            String id = strategyService.createStrategy(req);
            responseObserver.onNext(CreateStrategyResp.newBuilder().setStrategyId(id).build());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asException());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateStrategy(UpdateStrategyReq request, StreamObserver<UpdateStrategyResp> responseObserver) {
        try {
            CreateStrategyRequest req = new CreateStrategyRequest();
            req.setName(request.getName());
            req.setWorkDir(request.getWorkDir().isEmpty() ? null : request.getWorkDir());
            req.setSteps(request.getStepsList().stream().map(this::stepDtoFromProto).collect(Collectors.toList()));
            boolean ok = strategyService.updateStrategy(request.getStrategyId(), req);
            responseObserver.onNext(UpdateStrategyResp.newBuilder().setSuccess(ok).build());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asException());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteStrategy(DeleteStrategyReq request, StreamObserver<DeleteStrategyResp> responseObserver) {
        try {
            boolean ok = strategyService.deleteStrategy(request.getStrategyId());
            responseObserver.onNext(DeleteStrategyResp.newBuilder().setSuccess(ok).build());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asException());
        } finally {
            responseObserver.onCompleted();
        }
    }

    private static TaskResp toTaskResp(TaskVO vo) {
        TaskResp.Builder b = TaskResp.newBuilder()
                .setId(vo.getId())
                .setStatus(vo.getStatus() != null ? vo.getStatus() : "")
                .setProgress(vo.getProgress() != null ? vo.getProgress() : 0)
                .setInputType(vo.getInputType() != null ? vo.getInputType() : "")
                .setInputPath(vo.getInputPath() != null ? vo.getInputPath() : "")
                .setStrategyId(vo.getStrategyId() != null ? vo.getStrategyId() : "");
        if (vo.getOutputPath() != null) b.setOutputPath(vo.getOutputPath());
        if (vo.getOutputHttpUrl() != null) b.setOutputHttpUrl(vo.getOutputHttpUrl());
        if (vo.getCreatedAt() != null) b.setCreatedAt(vo.getCreatedAt());
        if (vo.getStartedAt() != null) b.setStartedAt(vo.getStartedAt());
        if (vo.getCompletedAt() != null) b.setCompletedAt(vo.getCompletedAt());
        return b.build();
    }

    private static StrategyResp toStrategyResp(StrategyVO vo) {
        StrategyResp.Builder b = StrategyResp.newBuilder()
                .setId(vo.getId())
                .setName(vo.getName() != null ? vo.getName() : "")
                .setStepCount(vo.getStepCount() != null ? vo.getStepCount() : 0);
        if (vo.getWorkDir() != null) b.setWorkDir(vo.getWorkDir());
        if (vo.getCreatedAt() != null) b.setCreatedAt(vo.getCreatedAt());
        if (vo.getSteps() != null) {
            for (StrategyStepVO s : vo.getSteps()) {
                StrategyStepResp.Builder sb = StrategyStepResp.newBuilder()
                        .setStepId(s.getStepId() != null ? s.getStepId() : 0)
                        .setType(s.getType() != null ? s.getType() : "transcode")
                        .setDepends(s.getDepends() != null ? s.getDepends() : "");
                if (s.getInputTemplate() != null) sb.setInputTemplate(s.getInputTemplate());
                if (s.getOutputTemplate() != null) sb.setOutputTemplate(s.getOutputTemplate());
                if (s.getTargetFormat() != null) sb.setTargetFormat(s.getTargetFormat());
                if (s.getResolution() != null) sb.setResolution(s.getResolution());
                if (s.getBitrate() != null) sb.setBitrate(s.getBitrate());
                if (s.getFrameRate() != null) sb.setFrameRate(s.getFrameRate());
                if (s.getEncoder() != null) sb.setEncoder(s.getEncoder());
                b.addSteps(sb.build());
            }
        }
        return b.build();
    }

    private CreateStrategyRequest reqFromProto(CreateStrategyReq r) {
        CreateStrategyRequest req = new CreateStrategyRequest();
        req.setName(r.getName());
        req.setWorkDir(r.getWorkDir().isEmpty() ? null : r.getWorkDir());
        req.setSteps(r.getStepsList().stream().map(this::stepDtoFromProto).collect(Collectors.toList()));
        return req;
    }

    private StrategyStepDTO stepDtoFromProto(StrategyStepDto d) {
        StrategyStepDTO s = new StrategyStepDTO();
        s.setType(d.getType().isEmpty() ? null : d.getType());
        s.setDepends(d.getDepends().isEmpty() ? null : d.getDepends());
        s.setInputTemplate(d.getInputTemplate().isEmpty() ? null : d.getInputTemplate());
        s.setOutputTemplate(d.getOutputTemplate().isEmpty() ? null : d.getOutputTemplate());
        s.setTargetFormat(d.getTargetFormat().isEmpty() ? null : d.getTargetFormat());
        s.setResolution(d.getResolution().isEmpty() ? null : d.getResolution());
        s.setBitrate(d.getBitrate() > 0 ? d.getBitrate() : null);
        s.setFrameRate(d.getFrameRate() > 0 ? d.getFrameRate() : null);
        s.setEncoder(d.getEncoder().isEmpty() ? null : d.getEncoder());
        return s;
    }
}
