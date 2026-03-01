-- 默认 AK/SK：root / 123456（仅当不存在时插入）
INSERT INTO transcode_access_key (access_key_id, secret_key, name, status, created_at)
SELECT 'root', '123456', 'root', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM transcode_access_key WHERE access_key_id = 'root');
