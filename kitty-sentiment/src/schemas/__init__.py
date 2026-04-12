"""
Pydantic数据模式
"""

from pydantic import BaseModel, Field, ConfigDict, field_validator
from typing import Optional, List, Any
from datetime import datetime
from enum import Enum


def snake_to_camel(name: str) -> str:
    parts = name.split("_")
    return parts[0] + "".join(p.capitalize() for p in parts[1:])


class CamelModel(BaseModel):
    """API 响应统一使用 camelCase，与前端对齐。"""

    model_config = ConfigDict(
        alias_generator=snake_to_camel,
        populate_by_name=True,
        from_attributes=True,
    )


class SentimentLabel(str, Enum):
    """情感标签枚举"""
    POSITIVE = "positive"
    NEGATIVE = "negative"
    NEUTRAL = "neutral"


# 舆情相关模式
class SentimentBase(CamelModel):
    """舆情基础模式"""
    source_id: int = Field(..., description="信息源ID")
    title: str = Field(..., min_length=1, max_length=500, description="标题")
    content: str = Field(..., min_length=1, description="内容")
    url: str = Field(..., description="原文链接")
    author: Optional[str] = Field(None, max_length=200, description="作者")
    publish_time: datetime = Field(..., description="发布时间")
    sentiment_score: Optional[float] = Field(0.0, ge=-1.0, le=1.0, description="情感得分")
    sentiment_label: Optional[SentimentLabel] = Field(SentimentLabel.NEUTRAL, description="情感标签")
    keywords: Optional[str] = Field(None, max_length=1000, description="关键词")
    views: int = Field(0, ge=0, description="浏览量等")
    likes: int = Field(0, ge=0, description="点赞数")
    comments: int = Field(0, ge=0, description="评论数")


class SentimentCreate(SentimentBase):
    """创建舆情请求"""
    pass


class SentimentUpdate(BaseModel):
    """更新舆情请求"""
    title: Optional[str] = Field(None, min_length=1, max_length=500)
    content: Optional[str] = None
    sentiment_score: Optional[float] = Field(None, ge=-1.0, le=1.0)
    sentiment_label: Optional[SentimentLabel] = None
    keywords: Optional[str] = Field(None, max_length=1000)


class SentimentResponse(SentimentBase):
    """舆情响应模式"""
    id: int
    source_name: Optional[str] = Field(None, description="信息源名称（列表接口关联查询）")
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(
        alias_generator=snake_to_camel,
        populate_by_name=True,
        from_attributes=True,
    )


def _normalize_source_type_tags(v: str) -> str:
    s = v.replace("，", ",")
    parts = [p.strip() for p in s.split(",") if p.strip()]
    return ",".join(parts)


# 信息源相关模式
class SourceBase(CamelModel):
    """信息源基础模式"""
    name: str = Field(..., min_length=1, max_length=200)
    url: str = Field(..., description="URL")
    type: str = Field(
        ...,
        min_length=1,
        max_length=500,
        description="类型标签，英文逗号分隔（可含中文）",
    )
    spider_name: Optional[str] = Field(None, max_length=200, description="与名称一致，作为 Scrapy spider 名")

    @field_validator("type", mode="before")
    @classmethod
    def _strip_type(cls, v: object) -> object:
        if v is None or not isinstance(v, str):
            return v
        return _normalize_source_type_tags(v)
    category: Optional[str] = Field(None, max_length=100)
    enabled: Optional[bool] = True
    config: Optional[dict] = None


class SourceCreate(SourceBase):
    """创建信息源请求"""
    pass


class SourceUpdate(BaseModel):
    """更新信息源请求"""
    name: Optional[str] = Field(None, min_length=1, max_length=200)
    url: Optional[str] = None
    type: Optional[str] = Field(None, min_length=1, max_length=500)
    spider_name: Optional[str] = Field(None, max_length=200)
    category: Optional[str] = Field(None, max_length=100)
    enabled: Optional[bool] = None
    config: Optional[dict] = None

    model_config = ConfigDict(populate_by_name=True)

    @field_validator("type", mode="before")
    @classmethod
    def _strip_type_update(cls, v: object) -> object:
        if v is None or not isinstance(v, str):
            return v
        return _normalize_source_type_tags(v)


class SourceResponse(SourceBase):
    """信息源响应模式"""
    id: int
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(
        alias_generator=snake_to_camel,
        populate_by_name=True,
        from_attributes=True,
    )

    @field_validator("enabled", mode="before")
    @classmethod
    def coerce_enabled(cls, v: Any) -> Any:
        if v is None:
            return True
        if isinstance(v, int):
            return bool(v)
        return v


# 分页相关模式
class PaginationResponse(CamelModel):
    """分页响应"""
    total: int
    page: int
    page_size: int
    total_pages: int
    has_next: bool
    has_prev: bool


class SentimentListResponse(CamelModel):
    """舆情列表响应"""
    items: List[SentimentResponse]
    pagination: PaginationResponse


class SourceListResponse(CamelModel):
    """信息源列表响应"""
    items: List[SourceResponse]
    pagination: PaginationResponse


# 请求模式
class ListSentimentsRequest(BaseModel):
    """查询舆情请求"""
    page: int = Field(1, ge=1)
    page_size: int = Field(20, ge=1, le=100)
    source_id: Optional[int] = None
    sentiment_label: Optional[SentimentLabel] = None
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None
    keywords: Optional[str] = None


class ListSourcesRequest(BaseModel):
    """查询信息源请求"""
    page: int = Field(1, ge=1)
    page_size: int = Field(20, ge=1, le=100)
    enabled: Optional[bool] = None
    type: Optional[str] = None
    category: Optional[str] = None


# 爬虫相关模式
class CrawlRequest(BaseModel):
    """爬取请求"""
    source_id: Optional[int] = None
    url: Optional[str] = None
    keywords: Optional[List[str]] = None
    max_pages: int = Field(10, ge=1, le=100)


class CrawlResponse(CamelModel):
    """爬取响应"""
    success: bool
    message: str
    crawled_count: int = 0
    new_items: int = 0
    errors: List[str] = Field(default_factory=list)


class LoginRequest(BaseModel):
    username: str
    password: str


class LoginResponse(CamelModel):
    token: str
    expires_in: int = 86400
