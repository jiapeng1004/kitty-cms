import fs from 'fs'
import path from 'path'
import {fileURLToPath} from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(__dirname, '../src/mam')
const repls = [
    ["from '@/api/", "from '@/mam/api/"],
    ['from "@/api/', 'from "@/mam/api/'],
    ["from '@/components/", "from '@/mam/components/"],
    ["from '@/composables/", "from '@/mam/composables/"],
    ["from '@/constants/", "from '@/mam/constants/"],
    ["from '@/utils/material", "from '@/mam/utils/material"],
]

function walk(dir) {
    for (const name of fs.readdirSync(dir, {withFileTypes: true})) {
        const p = path.join(dir, name.name)
        if (name.isDirectory()) {
            walk(p)
            continue
        }
        if (!/\.(ts|vue)$/.test(name.name)) continue
        let content = fs.readFileSync(p, 'utf8')
        let updated = content
        for (const [old, neu] of repls) {
            updated = updated.split(old).join(neu)
        }
        if (updated !== content) {
            fs.writeFileSync(p, updated, 'utf8')
        }
    }
}

walk(root)
console.log('mam imports updated')
