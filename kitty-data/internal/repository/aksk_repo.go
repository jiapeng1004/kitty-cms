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

type AkskRepo struct {
	collection *mongo.Collection
}

func NewAkskRepo(collectionName string) *AkskRepo {
	return &AkskRepo{
		collection: db.GetCollection(collectionName),
	}
}

func (r *AkskRepo) FindAll(ctx context.Context) ([]model.AccessKey, error) {
	filter := bson.M{"enabled": true}
	cursor, err := r.collection.Find(ctx, filter)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var results []model.AccessKey
	if err := cursor.All(ctx, &results); err != nil {
		return nil, err
	}
	return results, nil
}

func (r *AkskRepo) FindByAK(ctx context.Context, ak string) (*model.AccessKey, error) {
	filter := bson.M{"ak": ak, "enabled": true}
	var result model.AccessKey
	err := r.collection.FindOne(ctx, filter).Decode(&result)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, nil
		}
		return nil, err
	}
	return &result, nil
}

func (r *AkskRepo) Upsert(ctx context.Context, ak *model.AccessKey) error {
	filter := bson.M{"ak": ak.AK}
	update := bson.M{
		"$set": bson.M{
			"sk":         ak.SK,
			"remark":     ak.Remark,
			"enabled":    ak.Enabled,
			"updateTime": time.Now(),
		},
		"$setOnInsert": bson.M{
			"createTime": time.Now(),
		},
	}
	opts := options.UpdateOne().SetUpsert(true)
	_, err := r.collection.UpdateOne(ctx, filter, update, opts)
	return err
}
