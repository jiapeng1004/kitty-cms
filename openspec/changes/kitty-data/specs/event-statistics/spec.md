## ADDED Requirements

### Requirement: 统计查询接口 - 时间范围统计

系统 SHALL 提供时间范围内的统计接口，支持按事件类型统计触发次数和操作值总和。

#### Scenario: 查询指定时间范围的事件统计
- **WHEN** GET /api/v1/stats/range?eventType=xxx&startTime=xxx&endTime=xxx
- **THEN** 返回该时间范围内该事件类型的触发次数和操作值总和

#### Scenario: 查询多个事件类型的统计
- **WHEN** GET /api/v1/stats/range?eventTypes=type1,type2&startTime=xxx&endTime=xxx
- **THEN** 返回多个事件类型的分别统计结果

---

### Requirement: 统计查询接口 - 操作者排名

系统 SHALL 提供操作者排名接口，支持按事件类型统计操作者的触发次数和操作值。

#### Scenario: 查询操作者排名
- **WHEN** GET /api/v1/stats/ranking?eventType=xxx&startTime=xxx&endTime=xxx&limit=10
- **THEN** 返回该时间范围内该事件类型的操作者排名（前 N 名）

#### Scenario: 按操作值排名
- **WHEN** GET /api/v1/stats/ranking?eventType=xxx&startTime=xxx&endTime=xxx&sortBy=value&limit=10
- **THEN** 返回按操作值总和排名的操作者列表

---

### Requirement: 统计查询接口 - 事件排名

系统 SHALL 提供事件排名接口，统计指定时间范围内各事件的触发情况。

#### Scenario: 查询事件触发次数排名
- **WHEN** GET /api/v1/stats/events/ranking?eventTypes=type1,type2&startTime=xxx&endTime=xxx
- **THEN** 返回各事件类型的触发次数排名

#### Scenario: 查询事件操作值排名
- **WHEN** GET /api/v1/stats/events/ranking?eventTypes=type1,type2&startTime=xxx&endTime=xxx&sortBy=value
- **THEN** 返回各事件类型的操作值总和排名

---

### Requirement: 统计查询接口 - 操作者维度事件排名

系统 SHALL 提供操作者维度的事件排名接口，统计指定操作者在各事件上的触发情况。

#### Scenario: 查询指定操作者的事件统计
- **WHEN** GET /api/v1/stats/operator-events?operators=op1,op2&eventTypes=type1,type2&startTime=xxx&endTime=xxx
- **THEN** 返回各操作者在各事件类型上的触发次数和操作值

---

### Requirement: 统计查询接口 - 趋势分析

系统 SHALL 提供趋势分析接口，支持按不同时间维度统计事件趋势。

#### Scenario: 按小时统计趋势
- **WHEN** GET /api/v1/stats/trend?eventTypes=type1,type2&startTime=xxx&endTime=xxx&groupBy=hour
- **THEN** 返回按小时聚合的事件触发次数和操作值趋势

#### Scenario: 按天统计趋势
- **WHEN** GET /api/v1/stats/trend?eventTypes=type1,type2&startTime=xxx&endTime=xxx&groupBy=day
- **THEN** 返回按天聚合的事件触发次数和操作值趋势

#### Scenario: 按周统计趋势
- **WHEN** GET /api/v1/stats/trend?eventTypes=type1,type2&startTime=xxx&endTime=xxx&groupBy=week
- **THEN** 返回按周聚合的事件触发次数和操作值趋势

#### Scenario: 按月统计趋势
- **WHEN** GET /api/v1/stats/trend?eventTypes=type1,type2&startTime=xxx&endTime=xxx&groupBy=month
- **THEN** 返回按月聚合的事件触发次数和操作值趋势

#### Scenario: 按年统计趋势
- **WHEN** GET /api/v1/stats/trend?eventTypes=type1,type2&startTime=xxx&endTime=xxx&groupBy=year
- **THEN** 返回按年聚合的事件触发次数和操作值趋势
