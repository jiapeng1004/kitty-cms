import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Button, Spin } from 'antd'
import { FullscreenExitOutlined, FullscreenOutlined } from '@ant-design/icons'
import Editor from '@monaco-editor/react'

/** 与编辑器外框一体的顶栏高度（接近 VS Code 深色标题区） */
const CHROME_BAR_H = 36

async function requestFullscreenEl(el: HTMLElement) {
  if (el.requestFullscreen) {
    await el.requestFullscreen()
    return
  }
  const w = el as HTMLElement & { webkitRequestFullscreen?: () => Promise<void> }
  if (w.webkitRequestFullscreen) {
    await w.webkitRequestFullscreen()
  }
}

async function exitFullscreenDoc() {
  if (document.exitFullscreen) {
    await document.exitFullscreen()
    return
  }
  const d = document as Document & { webkitExitFullscreen?: () => Promise<void> }
  if (d.webkitExitFullscreen) {
    await d.webkitExitFullscreen()
  }
}

type ImportRule = {
  symbol: string
  importLine: string
}

const IMPORT_RULES: ImportRule[] = [
  { symbol: 'scrapy', importLine: 'import scrapy' },
  { symbol: 'json', importLine: 'import json' },
  { symbol: 're', importLine: 'import re' },
  { symbol: 'datetime', importLine: 'from datetime import datetime, timedelta' },
  { symbol: 'timedelta', importLine: 'from datetime import datetime, timedelta' },
  { symbol: 'BaseModel', importLine: 'from pydantic import BaseModel, Field, ValidationError' },
  { symbol: 'Field', importLine: 'from pydantic import BaseModel, Field, ValidationError' },
  { symbol: 'ValidationError', importLine: 'from pydantic import BaseModel, Field, ValidationError' },
  { symbol: 'SentimentItem', importLine: 'from src.items import SentimentItem' },
]

/** 供表单底部按钮调用：按已用符号补全 import 行（运行仍可不写 import） */
export function applySpiderAutoImports(code: string): string {
  const body = code || ''
  const lines = body.split(/\r?\n/)
  const exists = new Set(
    lines
      .map((x) => x.trim())
      .filter((x) => x.startsWith('import ') || x.startsWith('from ')),
  )
  const needed: string[] = []

  for (const rule of IMPORT_RULES) {
    const useRegex = new RegExp(`\\b${rule.symbol}\\b`)
    if (useRegex.test(body) && !exists.has(rule.importLine)) {
      needed.push(rule.importLine)
      exists.add(rule.importLine)
    }
  }
  if (needed.length === 0) return body
  return `${needed.join('\n')}\n\n${body}`
}

interface Props {
  value?: string
  onChange?: (value: string) => void
  /** 用于估算编辑器高度（行数基准），Monaco 使用像素高度 */
  minRows?: number
}

export default function PythonCodeEditor({ value, onChange, minRows = 16 }: Props) {
  const code = value ?? ''
  const baseHeight = useMemo(() => Math.max(360, minRows * 24), [minRows])
  const containerRef = useRef<HTMLDivElement>(null)
  const [isFullscreen, setIsFullscreen] = useState(false)
  const [editorPx, setEditorPx] = useState(baseHeight)

  const syncFullscreenState = useCallback(() => {
    const el = containerRef.current
    setIsFullscreen(!!(el && document.fullscreenElement === el))
  }, [])

  useEffect(() => {
    document.addEventListener('fullscreenchange', syncFullscreenState)
    return () => document.removeEventListener('fullscreenchange', syncFullscreenState)
  }, [syncFullscreenState])

  useEffect(() => {
    const measure = () => {
      if (!isFullscreen) {
        setEditorPx(baseHeight)
        return
      }
      setEditorPx(Math.max(320, window.innerHeight - CHROME_BAR_H))
    }
    measure()
    window.addEventListener('resize', measure)
    return () => window.removeEventListener('resize', measure)
  }, [isFullscreen, baseHeight])

  const toggleFullscreen = useCallback(async () => {
    const el = containerRef.current
    if (!el) return
    try {
      if (!document.fullscreenElement) {
        await requestFullscreenEl(el)
      } else {
        await exitFullscreenDoc()
      }
    } catch {
      /* 部分环境拒绝全屏时忽略 */
    }
  }, [])

  const loadingBoxStyle = useMemo(
    () => ({
      height: editorPx,
      display: 'flex' as const,
      alignItems: 'center' as const,
      justifyContent: 'center' as const,
    }),
    [editorPx],
  )

  return (
    <div>
      <div
        ref={containerRef}
        style={{
          background: '#1e1e1e',
          ...(isFullscreen
            ? {
                height: '100%',
                width: '100%',
                display: 'flex',
                flexDirection: 'column' as const,
                boxSizing: 'border-box' as const,
              }
            : {}),
        }}
      >
        <div
          style={{
            border: '1px solid #303030',
            borderRadius: 6,
            overflow: 'hidden',
            background: '#1e1e1e',
            display: 'flex',
            flexDirection: 'column',
            flex: isFullscreen ? 1 : undefined,
            minHeight: isFullscreen ? 0 : undefined,
          }}
        >
          <div
            style={{
              flexShrink: 0,
              height: CHROME_BAR_H,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'flex-end',
              padding: '0 10px',
              background: '#252526',
              borderBottom: '1px solid #1e1e1e',
            }}
          >
            <Button
              type="text"
              size="small"
              icon={isFullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
              onClick={toggleFullscreen}
              style={{
                color: 'rgba(255, 255, 255, 0.78)',
                fontSize: 12,
              }}
              styles={{
                root: {
                  color: 'rgba(255, 255, 255, 0.78)',
                },
                icon: {
                  color: 'rgba(255, 255, 255, 0.65)',
                },
              }}
            >
              {isFullscreen ? '退出全屏' : '全屏编辑'}
            </Button>
          </div>
          <Editor
            height={`${editorPx}px`}
            defaultLanguage="python"
            theme="vs-dark"
            value={code}
            onChange={(v) => onChange?.(v ?? '')}
            loading={
              <div style={loadingBoxStyle}>
                <Spin tip="加载编辑器…" />
              </div>
            }
            options={{
            automaticLayout: true,
            minimap: { enabled: true, scale: 1 },
            fontSize: 14,
            fontFamily: "'JetBrains Mono', 'Fira Code', Consolas, 'Courier New', monospace",
            fontLigatures: true,
            lineNumbers: 'on',
            lineNumbersMinChars: 3,
            scrollBeyondLastLine: false,
            wordWrap: 'on',
            wrappingIndent: 'indent',
            tabSize: 4,
            insertSpaces: true,
            detectIndentation: false,
            folding: true,
            foldingStrategy: 'indentation',
            renderWhitespace: 'selection',
            bracketPairColorization: { enabled: true },
            guides: {
              bracketPairs: true,
              indentation: true,
            },
            smoothScrolling: true,
            cursorBlinking: 'smooth',
            cursorSmoothCaretAnimation: 'on',
            padding: { top: 12, bottom: 12 },
            scrollbar: {
              verticalScrollbarSize: 12,
              horizontalScrollbarSize: 12,
            },
            quickSuggestions: {
              other: true,
              comments: true,
              strings: true,
            },
            suggestOnTriggerCharacters: true,
            acceptSuggestionOnEnter: 'on',
            formatOnPaste: false,
            formatOnType: false,
          }}
          />
        </div>
      </div>
    </div>
  )
}
