package grpcserver

import (
	"context"

	"kitty-topic/internal/grpcpb"
	"kitty-topic/internal/model"
	"kitty-topic/internal/service"

	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

var _ grpcpb.TopicServiceServer = (*TopicGrpcServer)(nil)

type TopicGrpcServer struct {
	grpcpb.UnimplementedTopicServiceServer
	svc *service.TopicService
}

func RegisterTopicGrpcServer(s *grpc.Server, svc *service.TopicService) {
	grpcpb.RegisterTopicServiceServer(s, &TopicGrpcServer{svc: svc})
}

func (s *TopicGrpcServer) CreateTopic(ctx context.Context, req *grpcpb.CreateTopicReq) (*grpcpb.CreateTopicResp, error) {
	_ = ctx
	if req == nil {
		return nil, status.Error(codes.InvalidArgument, "req required")
	}
	id, err := s.svc.CreateDraft(&service.CreateTopicInput{
		Title:   req.GetTitle(),
		Source:  req.GetSource(),
		Content: req.GetContent(),
		Tags:    req.GetTags(),
		// MVP：createdBy 暂不做；后续 to-do4 会补齐鉴权注入。
		CreatedBy: "",
	})
	if err != nil {
		return nil, status.Error(codes.Unimplemented, err.Error())
	}
	// MVP：service 目前可能返回空 id
	return &grpcpb.CreateTopicResp{Id: id}, nil
}

func (s *TopicGrpcServer) GetTopic(ctx context.Context, req *grpcpb.GetTopicReq) (*grpcpb.TopicResp, error) {
	_ = ctx
	if req == nil {
		return nil, status.Error(codes.InvalidArgument, "req required")
	}
	t, err := s.svc.GetTopic(req.GetId())
	if err != nil {
		return nil, status.Error(codes.Unimplemented, err.Error())
	}
	return topicToResp(t), nil
}

func (s *TopicGrpcServer) ListTopics(ctx context.Context, req *grpcpb.ListTopicsReq) (*grpcpb.ListTopicsResp, error) {
	_ = ctx
	if req == nil {
		return nil, status.Error(codes.InvalidArgument, "req required")
	}
	var statusPtr *int
	if req.GetStatus() != 0 {
		v := int(req.GetStatus())
		statusPtr = &v
	}
	resp, err := s.svc.ListTopics(&service.ListTopicQuery{
		Page:      int(req.GetPage()),
		Size:      int(req.GetSize()),
		SearchKey: req.GetSearchKey(),
		Status:    statusPtr,
	})
	if err != nil {
		return nil, status.Error(codes.Unimplemented, err.Error())
	}
	out := &grpcpb.ListTopicsResp{
		Topics: make([]*grpcpb.TopicResp, 0, len(resp.Topics)),
		Total:  resp.Total,
	}
	for i := range resp.Topics {
		t := resp.Topics[i]
		out.Topics = append(out.Topics, topicToResp(&t))
	}
	return out, nil
}

func (s *TopicGrpcServer) UpdateTopic(ctx context.Context, req *grpcpb.UpdateTopicReq) (*grpcpb.UpdateTopicResp, error) {
	_ = ctx
	if req == nil {
		return nil, status.Error(codes.InvalidArgument, "req required")
	}
	// MVP：更新逻辑未完成
	err := s.svc.UpdateTopic(&service.UpdateTopicInput{
		ID:        req.GetId(),
		Title:     req.GetTitle(),
		Source:    req.GetSource(),
		Content:   req.GetContent(),
		Tags:      req.GetTags(),
		UpdatedBy: "",
	})
	if err != nil {
		return nil, status.Error(codes.Unimplemented, err.Error())
	}
	return &grpcpb.UpdateTopicResp{Success: true}, nil
}

func (s *TopicGrpcServer) DeleteTopic(ctx context.Context, req *grpcpb.DeleteTopicReq) (*grpcpb.DeleteTopicResp, error) {
	_ = ctx
	if req == nil {
		return nil, status.Error(codes.InvalidArgument, "req required")
	}
	if err := s.svc.DeleteTopic(req.GetId()); err != nil {
		return nil, status.Error(codes.Unimplemented, err.Error())
	}
	return &grpcpb.DeleteTopicResp{Success: true}, nil
}

func (s *TopicGrpcServer) SubmitTopic(ctx context.Context, req *grpcpb.SubmitTopicReq) (*grpcpb.OperationResp, error) {
	_ = ctx
	if req == nil {
		return nil, status.Error(codes.InvalidArgument, "req required")
	}
	if err := s.svc.Submit(req.GetId(), ""); err != nil {
		return nil, status.Error(codes.Unimplemented, err.Error())
	}
	return &grpcpb.OperationResp{Success: true}, nil
}

func (s *TopicGrpcServer) RejectTopic(ctx context.Context, req *grpcpb.RejectTopicReq) (*grpcpb.OperationResp, error) {
	_ = ctx
	if req == nil {
		return nil, status.Error(codes.InvalidArgument, "req required")
	}
	if err := s.svc.Reject(req.GetId(), ""); err != nil {
		return nil, status.Error(codes.Unimplemented, err.Error())
	}
	return &grpcpb.OperationResp{Success: true}, nil
}

func (s *TopicGrpcServer) ApproveTopic(ctx context.Context, req *grpcpb.ApproveTopicReq) (*grpcpb.OperationResp, error) {
	_ = ctx
	if req == nil {
		return nil, status.Error(codes.InvalidArgument, "req required")
	}
	if err := s.svc.Approve(req.GetId(), ""); err != nil {
		return nil, status.Error(codes.Unimplemented, err.Error())
	}
	return &grpcpb.OperationResp{Success: true}, nil
}

func (s *TopicGrpcServer) PublishTopic(ctx context.Context, req *grpcpb.PublishTopicReq) (*grpcpb.OperationResp, error) {
	_ = ctx
	if req == nil {
		return nil, status.Error(codes.InvalidArgument, "req required")
	}
	if err := s.svc.Publish(req.GetId(), ""); err != nil {
		return nil, status.Error(codes.Unimplemented, err.Error())
	}
	return &grpcpb.OperationResp{Success: true}, nil
}

func topicToResp(t *model.Topic) *grpcpb.TopicResp {
	if t == nil {
		return &grpcpb.TopicResp{}
	}
	out := &grpcpb.TopicResp{
		Id:        t.ID,
		Title:     t.Title,
		Source:    t.Source,
		Content:   t.Content,
		Tags:      t.Tags,
		Status:    int32(t.Status),
		CreatedBy: t.CreatedBy,
		UpdatedBy: t.UpdatedBy,
	}
	if t.PublishedAt != nil {
		out.PublishedAt = t.PublishedAt.Unix()
	}
	if !t.CreatedAt.IsZero() {
		out.CreatedAt = t.CreatedAt.Unix()
	}
	if !t.UpdatedAt.IsZero() {
		out.UpdatedAt = t.UpdatedAt.Unix()
	}
	return out
}
