package service

import (
	"errors"
	"time"

	"kitty-topic/internal/model"
)

type TopicRepo interface {
	CreateTopic(t *model.Topic) error
	GetTopic(id string) (*model.Topic, error)
	ListTopics(page, size int, searchKey string, status *int) ([]model.Topic, int64, error)
	UpdateTopic(t *model.Topic) error
	UpdateTopicStatus(id string, newStatus int, publishedAt *time.Time, updatedBy string) error
	DeleteTopic(id string) error
}

type TopicService struct {
	repo TopicRepo
}

func NewTopicService(repo TopicRepo) *TopicService {
	return &TopicService{repo: repo}
}

type CreateTopicInput struct {
	Title     string
	Source    string
	Content   string
	Tags      string
	CreatedBy string
}

type UpdateTopicInput struct {
	ID        string
	Title     string
	Source    string
	Content   string
	Tags      string
	UpdatedBy string
}

func (s *TopicService) CreateDraft(input *CreateTopicInput) (string, error) {
	if input == nil {
		return "", errors.New("input required")
	}
	if input.Title == "" {
		return "", errors.New("title required")
	}
	if input.Source == "" {
		return "", errors.New("source required")
	}

	t := &model.Topic{
		ID:        "", // to-do2: 生成规则
		Title:     input.Title,
		Source:    input.Source,
		Content:   input.Content,
		Tags:      input.Tags,
		Status:    int(model.TopicStatusDraft),
		CreatedBy: input.CreatedBy,
		UpdatedBy: input.CreatedBy,
	}
	if err := s.repo.CreateTopic(t); err != nil {
		return "", err
	}
	return t.ID, nil
}

type ListTopicQuery struct {
	Page      int
	Size      int
	SearchKey string
	Status    *int
}

type ListTopicResult struct {
	Topics []model.Topic
	Total  int64
}

func (s *TopicService) ListTopics(q *ListTopicQuery) (*ListTopicResult, error) {
	if q == nil {
		return nil, errors.New("query required")
	}
	list, total, err := s.repo.ListTopics(q.Page, q.Size, q.SearchKey, q.Status)
	if err != nil {
		return nil, err
	}
	return &ListTopicResult{Topics: list, Total: total}, nil
}

func (s *TopicService) GetTopic(id string) (*model.Topic, error) {
	return s.repo.GetTopic(id)
}

func (s *TopicService) UpdateTopic(input *UpdateTopicInput) error {
	if input == nil {
		return errors.New("input required")
	}

	t, err := s.repo.GetTopic(input.ID)
	if err != nil {
		return err
	}
	if model.TopicStatus(t.Status) != model.TopicStatusDraft {
		return errors.New("topic can only be updated in DRAFT status")
	}

	t.Title = input.Title
	t.Source = input.Source
	t.Content = input.Content
	t.Tags = input.Tags
	t.UpdatedBy = input.UpdatedBy
	return s.repo.UpdateTopic(t)
}

func (s *TopicService) DeleteTopic(id string) error {
	t, err := s.repo.GetTopic(id)
	if err != nil {
		return err
	}
	if model.TopicStatus(t.Status) != model.TopicStatusDraft {
		return errors.New("topic can only be deleted in DRAFT status")
	}
	return s.repo.DeleteTopic(id)
}

func (s *TopicService) Submit(id string, userKey string) error {
	t, err := s.repo.GetTopic(id)
	if err != nil {
		return err
	}
	if model.TopicStatus(t.Status) != model.TopicStatusDraft {
		return errors.New("submit only allowed from DRAFT")
	}
	return s.repo.UpdateTopicStatus(id, int(model.TopicStatusPendingReview), nil, userKey)
}

func (s *TopicService) Reject(id string, userKey string) error {
	t, err := s.repo.GetTopic(id)
	if err != nil {
		return err
	}
	if model.TopicStatus(t.Status) != model.TopicStatusPendingReview {
		return errors.New("reject only allowed from PENDING_REVIEW")
	}
	return s.repo.UpdateTopicStatus(id, int(model.TopicStatusDraft), nil, userKey)
}

func (s *TopicService) Approve(id string, userKey string) error {
	t, err := s.repo.GetTopic(id)
	if err != nil {
		return err
	}
	if model.TopicStatus(t.Status) != model.TopicStatusPendingReview {
		return errors.New("approve only allowed from PENDING_REVIEW")
	}
	return s.repo.UpdateTopicStatus(id, int(model.TopicStatusApproved), nil, userKey)
}

func (s *TopicService) Publish(id string, userKey string) error {
	t, err := s.repo.GetTopic(id)
	if err != nil {
		return err
	}
	if model.TopicStatus(t.Status) != model.TopicStatusApproved {
		return errors.New("publish only allowed from APPROVED")
	}
	now := time.Now()
	return s.repo.UpdateTopicStatus(id, int(model.TopicStatusPublished), &now, userKey)
}
