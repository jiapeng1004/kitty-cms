package model

import (
	"time"
)

type AccessKey struct {
	ID         string `bson:"_id,omitempty" json:"id"`
	AK         string             `bson:"ak" json:"ak"`
	SK         string             `bson:"sk" json:"sk"`
	Remark     string             `bson:"remark" json:"remark"`
	Enabled    bool               `bson:"enabled" json:"enabled"`
	CreateTime time.Time          `bson:"createTime" json:"createTime"`
	UpdateTime time.Time          `bson:"updateTime" json:"updateTime"`
}
