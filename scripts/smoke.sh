#!/usr/bin/env bash
# 简单冒烟测试：需提前启动后端，数据库已初始化；admin/admin123 默认账户

set -euo pipefail
BASE=${BASE:-http://localhost:8080/api}
TOKEN=$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token // empty')
if [ -z "$TOKEN" ]; then
  echo "登录失败，检查后端或账号" && exit 1
fi
echo "登录成功"

echo "Ping..."
curl -sf "$BASE/ping" >/dev/null && echo "Ping OK"

echo "拉取分类..."
curl -sf "$BASE/categories" -H "Authorization: Bearer $TOKEN" >/dev/null && echo "分类 OK"

echo "拉取媒体..."
curl -sf "$BASE/media?page=1&size=1" -H "Authorization: Bearer $TOKEN" >/dev/null && echo "媒体 OK"

echo "拉取播放列表..."
curl -sf "$BASE/playlists" -H "Authorization: Bearer $TOKEN" >/dev/null && echo "播放列表 OK"

echo "拉取终端状态..."
curl -sf "$BASE/monitor/terminal-status" -H "Authorization: Bearer $TOKEN" >/dev/null && echo "终端 OK"

echo "布局模板..."
TPL_ID=$(curl -sf "$BASE/layout-templates" -H "Authorization: Bearer $TOKEN" | jq -r '.data[0].id // empty')
if [ -n "$TPL_ID" ]; then
  curl -sf "$BASE/layout-templates/$TPL_ID/history" -H "Authorization: Bearer $TOKEN" >/dev/null && echo "模板历史 OK"
else
  echo "暂无模板可验证，跳过"
fi

echo "冒烟完成"
