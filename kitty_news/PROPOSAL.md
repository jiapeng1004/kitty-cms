# Kitty News 客户端提案（今日头条风格 Demo）

## 目标
- 在后端未完成前，先交付可运行的 Flutter 客户端 Demo。
- UI 方向对齐“今日头条”核心体验：频道切换 + 信息流卡片 + 下拉刷新 + 加载更多。
- 网络调用采用统一 RPC 风格，禁止在业务代码中到处散落 API 路径字符串。

## 设计原则
- **统一协议**：所有请求统一走 `RpcClient.call(RpcRequest)`。
- **路径收敛**：API 路径统一维护在 `RpcPaths`，业务层仅调用领域方法。
- **分层清晰**：
  - `core/network`：RPC 协议与客户端抽象
  - `features/feed/data`：模型、RPC 服务、仓储
  - `features/feed/presentation`：页面与组件
- **可替换后端**：当前使用 `DemoRpcClient` + 假数据 handler，后续可替换为真实 HTTP 实现。

## 目录规划
- `lib/app/news_app.dart`：应用入口与依赖组装
- `lib/core/network/*`：RPC 请求对象、客户端、路径常量
- `lib/features/feed/data/*`：信息流数据层
- `lib/features/feed/presentation/*`：首页与卡片组件

## Demo 范围
- 首页频道（推荐、热点、科技、财经、体育、娱乐）
- 频道切换自动刷新信息流
- 下拉刷新 / 上拉加载更多
- 模拟接口延迟与分页
- 底部导航（首页、视频、关注、我的）

## RPC 规范（关键约束）
- API 路径常量仅出现在 `RpcPaths`。
- 业务调用示例：`newsRpcService.fetchFeed(...)`，不直接拼接 URL。
- 请求参数与响应解析均在 data 层集中处理。

## 后续演进
- 替换 `DemoRpcClient` 为 `HttpRpcClient`（Dio/Http）
- 新增详情页、评论、登录态
- 接入缓存策略与错误重试
