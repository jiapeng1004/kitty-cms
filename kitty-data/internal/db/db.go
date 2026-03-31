package db

import (
	"context"
	"fmt"
	"time"

	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
	"go.mongodb.org/mongo-driver/v2/mongo/readpref"
	"go.uber.org/zap"

	"github.com/kitty-cms/kitty-data/internal/config"
)

var (
	Client *mongo.Client
	DB     *mongo.Database
	log    *zap.Logger
)

func Init(cfg *config.Config, logger *zap.Logger) error {
	log = logger
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	clientOpts := options.Client().ApplyURI(cfg.Mongo.URI)
	client, err := mongo.Connect(clientOpts)
	if err != nil {
		return fmt.Errorf("failed to connect to mongo: %w", err)
	}

	if err := client.Ping(ctx, readpref.Primary()); err != nil {
		return fmt.Errorf("failed to ping mongo: %w", err)
	}

	Client = client
	DB = client.Database(cfg.Mongo.Database)
	log.Info("connected to mongodb", zap.String("database", cfg.Mongo.Database))
	return nil
}

func GetCollection(name string) *mongo.Collection {
	return DB.Collection(name)
}

func Close(ctx context.Context) error {
	if Client != nil {
		return Client.Disconnect(ctx)
	}
	return nil
}
