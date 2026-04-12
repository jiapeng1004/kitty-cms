import React from 'react'
import ReactDOM from 'react-dom/client'
import { HashRouter } from 'react-router-dom'
import { loader } from '@monaco-editor/react'
import * as monaco from 'monaco-editor'

import App from './App.tsx'
import './index.css'

// 使用 node_modules 内的 monaco-editor，避免 loader 默认从 CDN 拉取导致首屏慢/不稳定
loader.config({ monaco })

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <HashRouter>
      <App />
    </HashRouter>
  </React.StrictMode>,
)
