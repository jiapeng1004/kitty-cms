package service

import (
	"context"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.uber.org/zap"

	"github.com/kitty-cms/kitty-data/internal/model"
	"github.com/kitty-cms/kitty-data/internal/repository"
)

type EventService struct {
	repo *repository.EventRepo
	log  *zap.Logger
}

func NewEventService(repo *repository.EventRepo, logger *zap.Logger) *EventService {
	return &EventService{
		repo: repo,
		log:  logger,
	}
}

func (s *EventService) Report(ctx context.Context, event *model.Event) error {
	if event.Timestamp.IsZero() {
		event.Timestamp = time.Now()
	}
	return s.repo.Insert(ctx, event)
}

type StatsResult struct {
	EventType string `json:"eventType"`
	Count     int64  `json:"count"`
	SumValue  int64  `json:"sumValue"`
}

func (s *EventService) GetStatsByTimeRange(ctx context.Context, eventTypes []string, startTime, endTime time.Time) ([]StatsResult, error) {
	pipeline := mongo.Pipeline{
		{{"$match", bson.M{
			"eventType": bson.M{"$in": eventTypes},
			"timestamp": bson.M{
				"$gte": startTime,
				"$lte": endTime,
			},
		}}},
		{{"$group", bson.M{
			"_id":      "$eventType",
			"count":    bson.M{"$sum": 1},
			"sumValue": bson.M{"$sum": "$value"},
		}}},
		{{"$project", bson.M{
			"_id":       0,
			"eventType": "$_id",
			"count":     1,
			"sumValue":  1,
		}}},
	}

	cursor, err := s.repo.Aggregate(ctx, pipeline)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var results []StatsResult
	if err := cursor.All(ctx, &results); err != nil {
		return nil, err
	}
	return results, nil
}

type OperatorStats struct {
	Operator string `json:"operator"`
	Count    int64  `json:"count"`
	SumValue int64  `json:"sumValue"`
}

func (s *EventService) GetOperatorRanking(ctx context.Context, eventTypes []string, startTime, endTime time.Time, sortBy string, limit int) ([]OperatorStats, error) {
	sortField := "count"
	if sortBy == "value" {
		sortField = "sumValue"
	}

	pipeline := mongo.Pipeline{
		{{"$match", bson.M{
			"eventType": bson.M{"$in": eventTypes},
			"timestamp": bson.M{
				"$gte": startTime,
				"$lte": endTime,
			},
		}}},
		{{"$group", bson.M{
			"_id":      "$operator",
			"count":    bson.M{"$sum": 1},
			"sumValue": bson.M{"$sum": "$value"},
		}}},
		{{"$sort", bson.M{sortField: -1}}},
		{{"$limit", limit}},
		{{"$project", bson.M{
			"_id":      0,
			"operator": "$_id",
			"count":    1,
			"sumValue": 1,
		}}},
	}

	cursor, err := s.repo.Aggregate(ctx, pipeline)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var results []OperatorStats
	if err := cursor.All(ctx, &results); err != nil {
		return nil, err
	}
	return results, nil
}

type EventRankingStats struct {
	EventType string `json:"eventType"`
	Count     int64  `json:"count"`
	SumValue  int64  `json:"sumValue"`
}

func (s *EventService) GetEventRanking(ctx context.Context, eventTypes []string, startTime, endTime time.Time, sortBy string) ([]EventRankingStats, error) {
	sortField := "count"
	if sortBy == "value" {
		sortField = "sumValue"
	}

	pipeline := mongo.Pipeline{
		{{"$match", bson.M{
			"eventType": bson.M{"$in": eventTypes},
			"timestamp": bson.M{
				"$gte": startTime,
				"$lte": endTime,
			},
		}}},
		{{"$group", bson.M{
			"_id":      "$eventType",
			"count":    bson.M{"$sum": 1},
			"sumValue": bson.M{"$sum": "$value"},
		}}},
		{{"$sort", bson.M{sortField: -1}}},
		{{"$project", bson.M{
			"_id":       0,
			"eventType": "$_id",
			"count":     1,
			"sumValue":  1,
		}}},
	}

	cursor, err := s.repo.Aggregate(ctx, pipeline)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var results []EventRankingStats
	if err := cursor.All(ctx, &results); err != nil {
		return nil, err
	}
	return results, nil
}

type OperatorEventStats struct {
	Operator  string `json:"operator"`
	EventType string `json:"eventType"`
	Count     int64  `json:"count"`
	SumValue  int64  `json:"sumValue"`
}

func (s *EventService) GetOperatorEventRanking(ctx context.Context, operators []string, eventTypes []string, startTime, endTime time.Time) ([]OperatorEventStats, error) {
	pipeline := mongo.Pipeline{
		{{"$match", bson.M{
			"operator":  bson.M{"$in": operators},
			"eventType": bson.M{"$in": eventTypes},
			"timestamp": bson.M{
				"$gte": startTime,
				"$lte": endTime,
			},
		}}},
		{{"$group", bson.M{
			"_id": bson.M{
				"operator":  "$operator",
				"eventType": "$eventType",
			},
			"count":    bson.M{"$sum": 1},
			"sumValue": bson.M{"$sum": "$value"},
		}}},
		{{"$project", bson.M{
			"_id":       0,
			"operator":  "$_id.operator",
			"eventType": "$_id.eventType",
			"count":     1,
			"sumValue":  1,
		}}},
	}

	cursor, err := s.repo.Aggregate(ctx, pipeline)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var results []OperatorEventStats
	if err := cursor.All(ctx, &results); err != nil {
		return nil, err
	}
	return results, nil
}

type TrendResult struct {
	Period   string `json:"period"`
	Count    int64  `json:"count"`
	SumValue int64  `json:"sumValue"`
}

func (s *EventService) GetTrend(ctx context.Context, eventTypes []string, startTime, endTime time.Time, groupBy string) ([]TrendResult, error) {
	var dateFormat string
	switch groupBy {
	case "hour":
		dateFormat = "%Y-%m-%d %H:00"
	case "day":
		dateFormat = "%Y-%m-%d"
	case "week":
		dateFormat = "%Y-W%V"
	case "month":
		dateFormat = "%Y-%m"
	case "year":
		dateFormat = "%Y"
	default:
		dateFormat = "%Y-%m-%d"
	}

	pipeline := mongo.Pipeline{
		{{"$match", bson.M{
			"eventType": bson.M{"$in": eventTypes},
			"timestamp": bson.M{
				"$gte": startTime,
				"$lte": endTime,
			},
		}}},
		{{"$group", bson.M{
			"_id": bson.M{
				"period": bson.M{"$dateToString": bson.M{"format": dateFormat, "date": "$timestamp"}},
			},
			"count":    bson.M{"$sum": 1},
			"sumValue": bson.M{"$sum": "$value"},
		}}},
		{{"$sort", bson.D{{"period", 1}}}},
		{{"$project", bson.M{
			"_id":      0,
			"period":   "$_id.period",
			"count":    1,
			"sumValue": 1,
		}}},
	}

	cursor, err := s.repo.Aggregate(ctx, pipeline)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var results []TrendResult
	if err := cursor.All(ctx, &results); err != nil {
		return nil, err
	}
	return results, nil
}
