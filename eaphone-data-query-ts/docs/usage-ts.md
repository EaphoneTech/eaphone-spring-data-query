# eaphone-data-query-ts 使用说明

## 项目结构

```
eaphone-data-query-ts/
├── packages/
│   ├── core/              # @eaphone/query-core — 核心类型 + 解析器 + Express 中间件
│   ├── sqlite/            # @eaphone/query-sqlite — SQLite 数据源
│   └── mongodb/           # @eaphone/query-mongodb — MongoDB 数据源
└── apps/samples/          # 示例项目
```

## 快速开始

### 安装

```bash
pnpm add @eaphone/query-core
# 选择需要的数据源:
pnpm add @eaphone/query-sqlite   # SQLite
pnpm add @eaphone/query-mongodb  # MongoDB
```

### SQLite 示例

```typescript
import express from 'express'
import { createEaphoneQueryMiddleware } from '@eaphone/query-core'
import { createSqliteDataSource } from '@eaphone/query-sqlite'

async function main() {
  const datasource = await createSqliteDataSource({ dbPath: './data.db' })

  const app = express()
  app.use(express.json())
  app.use(createEaphoneQueryMiddleware({
    baseContext: '/v1/',
    datasource,
    resources: [
      { path: 'order', table: 't_order' },
    ],
  }))

  app.listen(3000)
}
main().catch(console.error)
```

### MongoDB 示例

```typescript
import express from 'express'
import { createEaphoneQueryMiddleware } from '@eaphone/query-core'
import { createMongoDataSource } from '@eaphone/query-mongodb'

async function main() {
  const datasource = await createMongoDataSource({
    uri: 'mongodb://localhost:27017',
    dbName: 'mydb',
  })

  const app = express()
  app.use(express.json())
  app.use(createEaphoneQueryMiddleware({
    baseContext: '/v1/',
    datasource,
    resources: [
      { path: 'order', table: 'orders' },
    ],
  }))

  app.listen(3000)
}
main().catch(console.error)
```

## 配置

### EaphoneQueryOptions

```typescript
interface EaphoneQueryOptions {
  baseContext: string           // URL 前缀，如 '/v1/'
  datasource: DataSource        // 数据源实例
  resources: ResourceConfig[]   // 资源路由配置
}
```

### ResourceConfig

```typescript
interface ResourceConfig {
  path: string                            // 资源名，如 'order' → POST /v1/order/search
  table: string                           // 数据库表名 / collection 名
  schema?: Record<string, ColumnTypeDef>  // 可选：字段类型映射
}
```

类型会自动推断：
- **SQLite**: 初始化时通过 `PRAGMA table_info` 读取列类型；用户提供的 schema 优先级更高
- **MongoDB**: 按 JSON 运行时值类型推断；用户提供的 schema 用于日期和时间戳转换

### ColumnTypeDef

```typescript
type ColumnTypeDef = 'string' | 'integer' | 'double' | 'date' | 'boolean'
```

## API

### POST `/{baseContext}/{path}/search`

请求体：

```json
{
  "draw": 1,
  "offset": 0,
  "limit": 10,
  "order_by": [{ "field": "name", "dir": "asc" }],
  "where": {
    "status": { "_eq": "valid" },
    "price": { "_gt": 10, "_lt": 100 }
  }
}
```

响应：

```json
{
  "draw": 1,
  "total": 100,
  "filtered": 23,
  "data": [ ... ],
  "error": ""
}
```

### POST `/{baseContext}/{path}/count`

请求体：

```json
{
  "where": {
    "status": { "_eq": "valid" }
  }
}
```

响应：

```json
{
  "total": 100,
  "filtered": 50
}
```

## 操作符列表

| 操作符 | 说明 | MongoDB | SQLite |
|--------|------|---------|--------|
| `_eq` | 等于 | `{field: val}` | `field = ?` |
| `_ne` | 不等于 | `{field: {$ne: val}}` | `field <> ?` |
| `_gt` | 大于 | `{field: {$gt: val}}` | `field > ?` |
| `_gte` | 大于等于 | `{field: {$gte: val}}` | `field >= ?` |
| `_lt` | 小于 | `{field: {$lt: val}}` | `field < ?` |
| `_lte` | 小于等于 | `{field: {$lte: val}}` | `field <= ?` |
| `_in` | 在列表中 | `{field: {$in: [...]}}` | `field IN (...)` |
| `_nin` | 不在列表中 | `{field: {$nin: [...]}}` | `field NOT IN (...)` |
| `_all` | 包含全部 | `{field: {$all: [...]}}` | 不支持 |
| `_regex` | 正则匹配 | `{field: {$regex: ...}}` | 不支持（默认无 REGEXP） |
| `_like` | LIKE 匹配 | `{field: {$regex: ...}}` | `field LIKE ?` |
| `_notlike` | NOT LIKE | `{field: {$not: {$regex: ...}}}` | `field NOT LIKE ?` |
| `_null` | IS NULL | `{field: null}` | `field IS NULL` |
| `_empty` | 空字符串 | `{field: ""}` | `field = ''` |
| `_isvoid` | NULL 或空 | `{$or: [null, "", {$exists: false}]}` | `IS NULL OR field = ''` |
| `_exists` | 字段存在 | `{field: {$exists: bool}}` | 不支持 |

### order_by 格式

支持两种格式：

```json
// 格式 1: 显式字段+方向
{ "field": "name", "dir": "asc" }

// 格式 2: 简写
{ "name": "asc" }
```

## 行为说明

以下行为继承自 Java 实现，可能与直觉不同：

### 1. `_null: false` 被忽略

Java 实现仅处理 `_null: true`，`false` 相当于没有传入。需要 IS NOT NULL 查询时，请使用 `_ne: null` 或其他方式。

### 2. `_eq` / `_ne` / `_isvoid` 互斥优先级

这三个操作符以 `if/else if/else` 链执行：

```
_eq > _ne > _isvoid > 其余操作符
```

如果在同一 QueryFilter 中同时出现 `_eq` 和 `_like`，只有 `_eq` 生效。其他操作符（`_gt`、`_like`、`_in` 等）不会被同时应用。

### 3. `_like` 的 `%` 语义

SQL 标准 `LIKE` 行为。`%` 匹配任意字符序列。不传 `%` 时等价于精确匹配（隐含首尾锚定）。

```json
{ "_like": "James" }   // 等效于 _eq: "James"
{ "_like": "James%" }  // 以 James 开头
{ "_like": "%James%" } // 包含 James
```

### 4. `_in` / `_nin` 含 `null` 时拆分

当数组中包含 `null` 时，null 值会被分离出来单独处理：

- `_in: ["A", null]` → `(field IS NULL OR field IN ('A'))`
- `_nin: ["A", null]` → `(field IS NOT NULL AND field NOT IN ('A'))`

### 5. SQLite 不支持的操作符

- `_all` — MongoDB 专用数组包含操作符
- `_regex` — SQLite 默认无 `REGEXP` 函数；请使用 `_like`
- `_exists` — 仅对 JSON 列有意义

### 6. 类型转换失败返回 null

Java 实现中 `tryConvert` 在解析失败时静默返回 `null`（不抛异常），TS 保持相同行为。

### 7. 嵌套字段

MongoDB 通过字符串路径直接支持嵌套字段查询（如 `user.address.city`）。SQLite 不支持嵌套字段路径。
