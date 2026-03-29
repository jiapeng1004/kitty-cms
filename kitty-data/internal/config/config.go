package config

import (
	"fmt"
	"sync"

	"github.com/spf13/viper"
)

type Config struct {
	Server ServerConfig `mapstructure:"server"`
	Mongo  MongoConfig  `mapstructure:"mongo"`
	Auth   AuthConfig   `mapstructure:"auth"`
}

type ServerConfig struct {
	Address string `mapstructure:"address"`
}

type MongoConfig struct {
	URI              string `mapstructure:"uri"`
	Database         string `mapstructure:"database"`
	EventsCollection string `mapstructure:"eventsCollection"`
	AkskCollection   string `mapstructure:"akskCollection"`
}

type AuthConfig struct {
	AkskRefreshIntervalSeconds int `mapstructure:"akskRefreshIntervalSeconds"`
	TimestampDriftSeconds      int `mapstructure:"timestampDriftSeconds"`
}

var (
	cfg  *Config
	once sync.Once
)

func Load(path string) (*Config, error) {
	var err error
	once.Do(func() {
		viper.SetConfigFile(path)
		viper.SetConfigType("yaml")
		viper.AutomaticEnv()

		if err = viper.ReadInConfig(); err != nil {
			err = fmt.Errorf("failed to read config: %w", err)
			return
		}

		cfg = &Config{}
		if err = viper.Unmarshal(cfg); err != nil {
			err = fmt.Errorf("failed to unmarshal config: %w", err)
			cfg = nil
		}
	})
	return cfg, err
}

func Get() *Config {
	return cfg
}
