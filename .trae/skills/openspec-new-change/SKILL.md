---
name: openspec-new-change
description: 使用实验性的工件工作流开始一个新的OpenSpec变更。当用户想要通过结构化的步骤创建新功能、修复或修改时使用。
license: MIT
compatibility: Requires openspec CLI.
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.1.1"
---

使用实验性的工件驱动方法开始一个新的变更。

**输入**: 用户的请求应包含变更名称（kebab-case格式）或他们想要构建的内容的描述。

**步骤**

1. **如果没有提供明确的输入，询问用户想要构建什么**

   使用**AskUserQuestion工具**（开放式，无预设选项）询问：
   > "您想要进行什么变更？请描述您想要构建或修复的内容。"

   从他们的描述中，推导出kebab-case格式的名称（例如，"添加用户认证" → `add-user-auth`）。

   **重要**: 在不了解用户想要构建什么的情况下，不要继续。

2. **确定工作流模式**

   除非用户明确要求不同的工作流，否则使用默认模式（省略`--schema`）。

   **仅在用户提及以下内容时使用不同的模式**:
   - 特定的模式名称 → 使用`--schema <name>`
   - "show workflows"或"what workflows" → 运行`openspec schemas --json`并让用户选择

   **否则**: 省略`--schema`以使用默认模式。

3. **创建变更目录**
   ```bash
   openspec new change "<name>"
   ```
   仅当用户请求特定工作流时才添加`--schema <name>`。
   这将在`openspec/changes/<name>/`创建一个带有所选模式的脚手架变更。

4. **显示工件状态**
   ```bash
   openspec status --change "<name>"
   ```
   这将显示哪些工件需要创建，哪些已准备就绪（依赖项已满足）。

5. **获取第一个工件的指令**
   第一个工件取决于模式（例如，spec-driven模式的`proposal`）。
   检查状态输出以找到状态为"ready"的第一个工件。
   ```bash
   openspec instructions <first-artifact-id> --change "<name>"
   ```
   这将输出创建第一个工件的模板和上下文。

6. **停止并等待用户指示**

**输出**

完成步骤后，总结：
- 变更名称和位置
- 使用的模式/工作流及其工件序列
- 当前状态（0/N个工件完成）
- 第一个工件的模板
- 提示："准备好创建第一个工件了吗？只需描述这个变更的内容，我会为您起草，或者请我继续。"

**护栏**
- 不要创建任何工件 - 只显示指令
- 不要超越显示第一个工件模板的步骤
- 如果名称无效（不是kebab-case格式），请要求提供有效的名称
- 如果具有该名称的变更已存在，建议继续该变更
- 如果使用非默认工作流，请传递--schema参数
