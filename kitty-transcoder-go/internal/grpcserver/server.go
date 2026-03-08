package grpcserver

import (
	"context"
	"encoding/json"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"

	"kitty-transcoder-go/internal/grpcpb"
	"kitty-transcoder-go/internal/model"
	"kitty-transcoder-go/internal/service"
)

var _ grpcpb.TranscodeServiceServer = (*TranscodeGrpcServer)(nil)

type TranscodeGrpcServer struct {
	grpcpb.UnimplementedTranscodeServiceServer
	taskSvc     *service.TaskService
	strategySvc *service.StrategyService
}

func NewTranscodeGrpcServer(taskSvc *service.TaskService, strategySvc *service.StrategyService) *TranscodeGrpcServer {
	return &TranscodeGrpcServer{taskSvc: taskSvc, strategySvc: strategySvc}
}

func (s *TranscodeGrpcServer) CreateTask(ctx context.Context, req *grpcpb.CreateTaskReq) (*grpcpb.CreateTaskResp, error) {
	if req == nil || req.GetInputPath() == "" {
		return nil, status.Error(codes.InvalidArgument, "input_path required")
	}
	createReq := &model.CreateTaskReq{
		InputType:         req.GetInputType(),
		InputPath:         req.GetInputPath(),
		StrategyID:        req.GetStrategyId(),
		Priority:          int(req.GetPriority()),
		WatermarkURL:      req.GetWatermarkUrl(),
		WatermarkPosition: req.GetWatermarkPosition(),
	}
	if createReq.InputType == "" {
		createReq.InputType = "FILE"
	}
	task, err := s.taskSvc.CreateTask(createReq)
	if err != nil {
		return nil, status.Error(codes.Internal, err.Error())
	}
	return &grpcpb.CreateTaskResp{TaskId: task.ID}, nil
}

func (s *TranscodeGrpcServer) GetTask(ctx context.Context, req *grpcpb.GetTaskReq) (*grpcpb.TaskResp, error) {
	task, err := s.taskSvc.GetTask(req.GetTaskId())
	if err != nil {
		return nil, status.Error(codes.NotFound, err.Error())
	}
	return taskToResp(task), nil
}

func (s *TranscodeGrpcServer) ListTasks(ctx context.Context, req *grpcpb.ListTasksReq) (*grpcpb.ListTasksResp, error) {
	page, size := int(req.GetPage()), int(req.GetSize())
	if page <= 0 {
		page = 1
	}
	if size <= 0 {
		size = 20
	}
	list, total, err := s.taskSvc.ListTasks(page, size, req.GetStatus(), req.GetTaskType())
	if err != nil {
		return nil, status.Error(codes.Internal, err.Error())
	}
	tasks := make([]*grpcpb.TaskResp, 0, len(list))
	for i := range list {
		tasks = append(tasks, taskToResp(&list[i]))
	}
	return &grpcpb.ListTasksResp{Tasks: tasks, Total: total}, nil
}

func (s *TranscodeGrpcServer) CancelTask(ctx context.Context, req *grpcpb.CancelTaskReq) (*grpcpb.CancelTaskResp, error) {
	err := s.taskSvc.CancelTask(req.GetTaskId())
	if err != nil {
		return nil, status.Error(codes.Internal, err.Error())
	}
	return &grpcpb.CancelTaskResp{Success: true}, nil
}

func (s *TranscodeGrpcServer) GetProgress(ctx context.Context, req *grpcpb.GetProgressReq) (*grpcpb.ProgressResp, error) {
	task, err := s.taskSvc.GetTask(req.GetTaskId())
	if err != nil {
		return nil, status.Error(codes.NotFound, err.Error())
	}
	return &grpcpb.ProgressResp{
		TaskId:   task.ID,
		Progress: int32(task.Progress),
		Status:   task.Status,
	}, nil
}

func (s *TranscodeGrpcServer) GetStrategies(ctx context.Context, _ *grpcpb.GetStrategiesReq) (*grpcpb.GetStrategiesResp, error) {
	names, err := s.strategySvc.ListStrategyNames()
	if err != nil {
		return nil, status.Error(codes.Internal, err.Error())
	}
	return &grpcpb.GetStrategiesResp{StrategyNames: names}, nil
}

func (s *TranscodeGrpcServer) GetStrategy(ctx context.Context, req *grpcpb.GetStrategyReq) (*grpcpb.StrategyResp, error) {
	steps, err := s.strategySvc.GetStrategy(req.GetStrategyId())
	if err != nil {
		return nil, status.Error(codes.NotFound, err.Error())
	}
	name := ""
	if len(steps) > 0 {
		name = steps[0].StrategyName
	}
	dtos := make([]*grpcpb.StrategyStepDto, 0, len(steps))
	for i := range steps {
		dtos = append(dtos, &grpcpb.StrategyStepDto{
			StepId:  int32(steps[i].StepID),
			Type:    steps[i].Type,
			Depends: steps[i].Depends,
			Param:   steps[i].Param,
		})
	}
	return &grpcpb.StrategyResp{Name: name, Steps: dtos}, nil
}

func (s *TranscodeGrpcServer) CreateStrategy(ctx context.Context, req *grpcpb.CreateStrategyReq) (*grpcpb.CreateStrategyResp, error) {
	if req.GetName() == "" {
		return nil, status.Error(codes.InvalidArgument, "name required")
	}
	steps := make([]model.StrategyStepParam, 0, len(req.GetSteps()))
	for _, dto := range req.GetSteps() {
		sp := model.StrategyStepParam{
			StepID:  int(dto.GetStepId()),
			Type:    dto.GetType(),
			Depends: dto.GetDepends(),
		}
		if dto.Param != "" {
			_ = json.Unmarshal([]byte(dto.GetParam()), &sp)
		}
		steps = append(steps, sp)
	}
	_, err := s.strategySvc.CreateStrategy(req.GetName(), steps)
	if err != nil {
		return nil, status.Error(codes.Internal, err.Error())
	}
	return &grpcpb.CreateStrategyResp{Id: req.GetName()}, nil
}

func (s *TranscodeGrpcServer) DeleteStrategy(ctx context.Context, req *grpcpb.DeleteStrategyReq) (*grpcpb.DeleteStrategyResp, error) {
	err := s.strategySvc.DeleteStrategy(req.GetStrategyId())
	if err != nil {
		return nil, status.Error(codes.Internal, err.Error())
	}
	return &grpcpb.DeleteStrategyResp{Success: true}, nil
}

func taskToResp(t *model.TranscodeTask) *grpcpb.TaskResp {
	resp := &grpcpb.TaskResp{
		Id:            t.ID,
		Status:        t.Status,
		Progress:      int32(t.Progress),
		InputType:     t.InputType,
		InputPath:     t.InputPath,
		OutputPath:    t.OutputPath,
		OutputHttpUrl: t.OutputHttpURL,
		ErrorMessage:  t.ErrorMessage,
	}
	if t.StrategyID != nil {
		resp.StrategyId = *t.StrategyID
	}
	if !t.CreatedAt.IsZero() {
		resp.CreatedAt = t.CreatedAt.Unix()
	}
	if t.StartedAt != nil {
		resp.StartedAt = t.StartedAt.Unix()
	}
	if t.CompletedAt != nil {
		resp.CompletedAt = t.CompletedAt.Unix()
	}
	return resp
}
