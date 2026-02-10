---
name: openspec-continue-change
description: 通过创建下一个工件继续处理OpenSpec变更。当用户想要推进其变更、创建下一个工件或继续其工作流时使用。
license: MIT
compatibility: Requires openspec CLI.
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.1.1"
---

通过创建下一个工件继续处理变更。

**输入**: 可选地指定变更名称。如果省略，检查是否可以从对话上下文中推断。如果模糊或歧义，必须提示可用的变更。

**步骤**

1. **如果未提供变更名称，提示选择**

   运行`openspec list --json`获取按最近修改时间排序的可用变更。然后使用**AskUserQuestion工具**让用户选择要处理的变更。

   展示最近修改的3-4个变更作为选项，显示：
   - 变更名称
   - 模式（如果存在`schema`字段，则使用该字段，否则为"spec-driven"）
   - 状态（例如，"0/5任务"，"完成"，"无任务"）
   - 最近修改时间（来自`lastModified`字段）

   将最近修改的变更标记为"（推荐）"，因为这可能是用户想要继续的变更。

   **重要**: 不要猜测或自动选择变更。始终让用户选择。

2. **检查当前状态**
   ```bash
   openspec status --change "<name>" --json
   ```
   解析JSON以了解当前状态。响应包括：
   - `schemaName`: 使用的工作流模式（例如，"spec-driven"）
   - `artifacts`: 具有状态的工件数组（"done"，"ready"，"blocked"）
   - `isComplete`: 布尔值，指示是否所有工件都已完成

3. **根据状态采取行动**:

   ---

   **如果所有工件都已完成（`isComplete: true`）**:
   - 祝贺用户
   - 显示最终状态，包括使用的模式
   - 建议："所有工件已创建！现在您可以实现此变更或归档它。"
   - 停止

   ---

   **如果工件已准备好创建**（状态显示具有`status: "ready"`的工件）:
   - 从状态输出中选择第一个具有`status: "ready"`的工件
   - 获取其指令：
     ```bash
     openspec instructions <artifact-id> --change "<name>" --json
     ```
   - 解析JSON。关键字段包括：
     - `context`: 项目背景（对您的约束 - 不要包含在输出中）
     - `rules`: 工件特定规则（对您的约束 - 不要包含在输出中）
     - `template`: 用于输出文件的结构
     - `instruction`: 模式特定指导
     - `outputPath`: 写入工件的位置
     - `dependencies`: 为上下文读取的已完成工件
   - **创建工件文件**:
     - 读取任何已完成的依赖文件以获取上下文
     - 使用`template`作为结构 - 填写其部分
     - 应用`context`和`rules`作为编写时的约束 - 但不要将它们复制到文件中
     - 写入指令中指定的输出路径
   - 显示创建的内容和现在解锁的内容
   - 创建一个工件后停止

   ---

   **如果没有工件准备好创建（全部阻塞）**:
   - 这在有效模式下不应该发生
   - 显示状态并建议检查问题

4. **创建工件后，显示进度**
   ```bash
   openspec status --change "<name>"
   ```

**输出**

每次调用后，显示：
- 创建了哪个工件
- 使用的模式工作流
- 当前进度（N/M完成）
- 现在解锁了哪些工件
- 提示："想要继续吗？只需让我继续或告诉我下一步该做什么。"

**工件创建指南**

工件类型及其目的取决于模式。使用指令输出中的`instruction`字段来了解要创建什么。

常见工件模式：

**spec-driven模式**（proposal → specs → design → tasks）:
- **proposal.md**: 如果不清楚，询问用户关于变更的信息。填写Why、What Changes、Capabilities、Impact。
  - Capabilities部分至关重要 - 列出的每个能力都需要一个规范文件。
- **specs/<capability>/spec.md**: 为提案的Capabilities部分中列出的每个能力创建一个规范（使用能力名称，而不是变更名称）。
- **design.md**: 记录技术决策、架构和实现方法。
- **tasks.md**: 将实现分解为带复选框的任务。

对于其他模式，遵循CLI输出中的`instruction`字段。

**护栏**
- 每次调用创建一个工件
- 创建新工件前始终阅读依赖工件
- 永远不要跳过工件或乱序创建
- 如果上下文不清楚，在创建前询问用户
- 写入后验证工件文件存在，然后标记进度
- 使用模式的工件序列，不要假设特定的工件名称
- **重要**: `context`和`rules`是对您的约束，不是文件的内容
  - 不要将`<context>`、`<rules>`、`<project_context>`块复制到工件中
  - 这些指导您写什么，但永远不应该出现在输出中
