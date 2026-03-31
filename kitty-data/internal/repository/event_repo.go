package repository

import (
	"context"
	"time"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"

	"github.com/kitty-cms/kitty-data/internal/db"
	"github.com/kitty-cms/kitty-data/internal/model"
)

type EventRepo struct {
	collection *mongo.Collection
}

func NewEventRepo(collectionName string) *EventRepo {
	return &EventRepo{
		collection: db.GetCollection(collectionName),
	}
}

func (r *EventRepo) Insert(ctx context.Context, event *model.Event) error {
	event.CreateTime = time.Now()
	_, err := r.collection.InsertOne(ctx, event)
	return err
}

func (r *EventRepo) FindByTimeRange(ctx context.Context, eventTypes []string, startTime, endTime time.Time) ([]model.Event, error) {
	filter := bson.M{
		"eventType": bson.M{"$in": eventTypes},
		"timestamp": bson.M{
			"$gte": startTime,
			"$lte": endTime,
		},
	}
	cursor, err := r.collection.Find(ctx, filter)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var results []model.Event
	if err := cursor.All(ctx, &results); err != nil {
		return nil, err
	}
	return results, nil
}

func (r *EventRepo) Aggregate(ctx context.Context, pipeline mongo.Pipeline) (*mongo.Cursor, error) {
	return r.collection.Aggregate(ctx, pipeline)
}

func (r *EventRepo) Watch(ctx context.Context) (*mongo.ChangeStream, error) {
	opts := options.ChangeStream().SetFullDocument(options.UpdateLookup)
	return r.collection.Watch(ctx, mongo.Pipeline{}, opts)
}
