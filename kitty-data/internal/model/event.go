package model

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type Event struct {
	ID         primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	EventType  string             `bson:"eventType" json:"eventType"`
	Operator   string             `bson:"operator" json:"operator"`
	Value      int64              `bson:"value" json:"value"`
	Timestamp  time.Time          `bson:"timestamp" json:"timestamp"`
	CreateTime time.Time          `bson:"createTime" json:"createTime"`
}
