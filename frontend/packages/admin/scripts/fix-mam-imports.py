import os

root = os.path.join(os.path.dirname(__file__), '..', 'src', 'mam')
repls = [
    ("from '@/api/", "from '@/mam/api/"),
    ('from "@/api/', 'from "@/mam/api/'),
    ("from '@/components/", "from '@/mam/components/"),
    ("from '@/composables/", "from '@/mam/composables/"),
    ("from '@/constants/", "from '@/mam/constants/"),
    ("from '@/utils/material", "from '@/mam/utils/material"),
]

for dirpath, _, files in os.walk(root):
    for f in files:
        if not f.endswith(('.ts', '.vue')):
            continue
        p = os.path.join(dirpath, f)
        with open(p, 'r', encoding='utf-8') as fh:
            content = fh.read()
        updated = content
        for old, new in repls:
            updated = updated.replace(old, new)
        if updated != content:
            with open(p, 'w', encoding='utf-8', newline='\n') as fh:
                fh.write(updated)

print('mam imports updated')
