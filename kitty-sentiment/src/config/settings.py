"""
配置管理
"""

import os
from dotenv import load_dotenv
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field
from typing import Optional
from functools import lru_cache

# 加载环境变量
load_dotenv()


class DatabaseConfig(BaseSettings):
    """数据库配置"""
    model_config = SettingsConfigDict(env_prefix="DATABASE_")
    
    type: str = Field(default="sqlite", description="数据库类型 (sqlite/mysql/doris)")
    host: str = Field(default="", description="数据库主机 (SQLite不需要)")
    port: int = Field(default=0, description="数据库端口 (SQLite不需要)")
    name: str = Field(default="sentiment.db", description="数据库名称或文件路径")
    user: str = Field(default="", description="数据库用户 (SQLite不需要)")
    password: str = Field(default="", description="数据库密码 (SQLite不需要)")

    @property
    def connection_url(self) -> str:
        """生成数据库连接URL"""
        if self.type == "sqlite":
            return f"sqlite:///{self.name}"
        elif self.type == "mysql":
            return f"mysql+pymysql://{self.user}:{self.password}@{self.host}:{self.port}/{self.name}"
        elif self.type == "doris":
            return f"doris://{self.user}:{self.password}@{self.host}:{self.port}/{self.name}"
        else:
            raise ValueError(f"Unsupported database type: {self.type}")


class CrawlerConfig(BaseSettings):
    """爬虫配置"""
    model_config = SettingsConfigDict(env_prefix="CRAWLER_")
    
    user_agent: str = Field(
        default="Mozilla/5.0 (compatible; KittySentimentBot/1.0)",
        description="User-Agent"
    )
    delay: float = Field(default=1.0, ge=0.1, description="请求间隔(秒)")
    timeout: int = Field(default=30, ge=1, description="请求超时(秒)")
    max_retries: int = Field(default=3, ge=1, le=10, description="最大重试次数")


class ServerConfig(BaseSettings):
    """服务器配置"""
    model_config = SettingsConfigDict(env_prefix="SERVER_")
    
    host: str = Field(default="0.0.0.0", description="服务器主机")
    port: int = Field(default=8000, ge=1, le=65535, description="服务器端口")
    debug: bool = Field(default=False, description="调试模式")


class Settings(BaseSettings):
    """应用设置"""
    model_config = SettingsConfigDict(env_prefix="APP_", env_nested_delimiter="_")
    
    app_name: str = Field(default="kitty-sentiment", description="应用名称")
    app_env: str = Field(default="development", description="应用环境")
    debug: bool = Field(default=False, description="调试模式")
    log_level: str = Field(default="INFO", description="日志级别")

    # 子配置
    database: DatabaseConfig = Field(default_factory=DatabaseConfig)
    crawler: CrawlerConfig = Field(default_factory=CrawlerConfig)
    server: ServerConfig = Field(default_factory=ServerConfig)


@lru_cache()
def get_settings() -> Settings:
    """获取配置实例（单例）"""
    return Settings()


# 全局配置实例
settings = get_settings()
