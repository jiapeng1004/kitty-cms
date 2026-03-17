# 转码服务强制要求（项目背景）

本文档为 **kitty-transcoder** 的硬性编码规范与 MVP 实践，所有在本模块的开发和 AI 辅助编码必须遵守。  
Cursor 规则已持久化于：`.cursor/rules/transcoder-mandatory-requirements.mdc`。

---

## 1. 依赖注入

- **能用 `@Resource` 的不要用 `@Autowired`**
  - 统一使用 `jakarta.annotation.Resource`（或 `javax.annotation.Resource`）按名称注入。
  - 仅在确有按类型/多实现等需求时再考虑 `@Autowired`。

## 2. 工具类与复用

- **能用 Hutool 等现有工具类库的，不要自行实现**
  - 字符串：`StrUtil`、`CharSequenceUtil` 等。
  - 集合/Bean：`CollUtil`、`BeanUtil`、`JSONUtil` 等。
  - IO/路径：`FileUtil`、`PathUtil` 等。
  - 优先使用项目已引入的 Hutool、Guava、Spring 等，避免手写等价工具函数。

## 3. 集合与流式写法

- **能用 Stream 的不要手动 for + add**
  - 集合转换、过滤、分组、映射等优先用 `Stream` API（`map`、`filter`、`collect`、`toList` 等）。
  - 避免 `for (x : list) { result.add(...); }` 这类手写循环拼接，除非有明确可读性/性能理由并注释说明。

## 4. 异常与中断（禁止破坏中断链）

- **对异常静默时，必须单独处理 `InterruptedException`**
  - 若某处选择 `catch (Exception e)` 并静默吞掉（不抛、只打日志等），会破坏线程中断状态，导致上层/调用方无法感知“被中断”。
  - 因此：**必须先加优先级更高的 `catch (InterruptedException e)` 分支**，在本层按需处理；**大多数情况下应直接向上抛**（或先 `Thread.currentThread().interrupt()` 再抛/再包装抛出），不要静默吞掉。
- **推荐写法**：先 catch `InterruptedException`，本层确需消化时再 `Thread.currentThread().interrupt()` 并 return/结束；否则 rethrow。再 catch 其他 `Exception` 做静默或日志。

## 5. 常量单一定义

- **同一语义的常量只允许在一处定义，多处引用**
  - 若一个常量值需要在多个地方使用，必须在公共常量类（如 `TranscodeConstants`）中定义，其它地方直接使用该常量（如 `RedisKeys.CANCEL_CHANNEL`），禁止在本类再接一次（如 `private static final String X = RedisKeys.X;`）。

## 6. 总体原则

- **MVP 击毙（最小可行、一击到位）**
  - 代码应简洁、符合项目既有风格，不引入多余抽象或重复造轮子。
  - 不写“门外汉”风格：例如到处 `@Autowired`、手写已有工具函数、手写 for-add 代替 Stream、吞掉 Exception 却不单独处理 InterruptedException。

---

与 `.cursor/rules/` 下其他规则（如禁止 Map 硬编码字段名、Controller 请求响应规范）同时生效。
