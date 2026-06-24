# Plan: TypeScript Port of eaphone-spring-data-query

## 背景

已有 Java 项目 (`eaphone-spring-data-query`)，定义了一个 `POST /search` 方法接口，支持灵活的查询条件。现需增加 TS 服务器端实现。

功能范围：
- 0 或多个字段的 AND 组合
- limit / offset 分页
- 支持 15 种过滤操作符
- 支持 MongoDB 和 SQLite

## 结构方案

pnpm workspace monorepo，分离 mongodb / sqlite 为独立 package，避免安装不需要的依赖。

```
eaphone-data-query-ts/
├── pnpm-workspace.yaml
├── package.json                          # root — private, workspace scripts
├── tsconfig.base.json                    # base tsconfig (strict, ES2020, NodeNext module)
├── packages/
│   ├── core/
│   │   ├── package.json                  # @eaphone/query-core
│   │   ├── tsconfig.json
│   │   └── src/
│   │       ├── index.ts                  # 入口：导出 middleware 工厂 + DataSource 接口 + 类型
│   │       ├── types.ts                  # 所有 DTO + 配置接口
│   │       ├── middleware.ts             # createEaphoneQueryMiddleware()
│   │       ├── query/
│   │       │   └── parser.ts             # QueryFilter → ParsedCondition[]
│   │       └── type-converters.ts        # ColumnTypeDef → JS value 转换
│   ├── sqlite/
│   │   ├── package.json                  # @eaphone/query-sqlite
│   │   ├── tsconfig.json
│   │   └── src/
│   │       └── index.ts                  # SqliteDataSource
│   └── mongodb/
│       ├── package.json                  # @eaphone/query-mongodb
│       ├── tsconfig.json
│       └── src/
│           └── index.ts                  # MongoDataSource
├── apps/
│   └── samples/
│       ├── package.json
│       └── src/
│           ├── sqlite-sample.ts
│           └── mongodb-sample.ts
└── docs/
    └── usage-ts.md
```

## 用户使用方式

### SQLite

```typescript
import express from 'express'
import { createEaphoneQueryMiddleware } from '@eaphone/query-core'
import { createSqliteDataSource } from '@eaphone/query-sqlite'

const app = express()
app.use(express.json())
app.use(createEaphoneQueryMiddleware({
  baseContext: '/v1/',
  datasource: createSqliteDataSource({ dbPath: './data.db' }),
  resources: [
    { path: 'order', table: 't_order' }
  ]
}))
app.listen(3000)
// → POST /v1/order/search
// → POST /v1/order/count
```

### MongoDB

```typescript
import { createMongoDataSource } from '@eaphone/query-mongodb'

app.use(createEaphoneQueryMiddleware({
  baseContext: '/v1/',
  datasource: createMongoDataSource({ uri: 'mongodb://localhost:27017', dbName: 'mydb' }),
  resources: [
    { path: 'order', table: 'orders' }
  ]
}))
```

## 数据类型

```typescript
type ColumnTypeDef = 'string' | 'integer' | 'double' | 'date' | 'boolean'

interface ResourceConfig {
  path: string                             // e.g. 'order' → POST /v1/order/search
  table: string                            // e.g. 't_order' / 'orders'
  schema?: Record<string, ColumnTypeDef>   // 可选，不提供则：
                                           //   SQLite: PRAGMA table_info 自动读取
                                           //   MongoDB: 按 JSON 运行时值推断
}
```

## 核心数据结构

```typescript
interface QueryInput {
  draw?: number
  offset?: number        // 默认 0
  limit?: number          // 默认 10
  order_by?: QueryOrder[]
  where?: Record<string, QueryFilter>
}

interface QueryFilter {
  _eq?: unknown
  _ne?: unknown
  _gt?: unknown
  _gte?: unknown
  _lt?: unknown
  _lte?: unknown
  _in?: unknown[]
  _nin?: unknown[]
  _all?: unknown[]
  _regex?: string
  _like?: string
  _notlike?: string
  _null?: boolean
  _empty?: boolean
  _isvoid?: boolean
  _exists?: boolean
  type?: 'string' | 'integer' | 'double' | 'date' | 'boolean'
}

interface QueryOrder {
  field?: string
  dir?: 'asc' | 'desc'
  // 也支持 { "field": "asc" } 格式 → 通过 value 对象
  [key: string]: unknown
}

interface QueryOutput<T> {
  draw?: number
  total: number
  filtered: number
  data: T[]
  error?: string
}

interface CountInput {
  where?: Record<string, QueryFilter>
}

interface CountOutput {
  total: number
  filtered: number
  error?: string
}

interface DataSource {
  query(table: string, input: ParsedQuery): Promise<QueryOutput<Record<string, unknown>>>
  count(table: string, conditions: ParsedCondition[]): Promise<CountOutput>
  close(): Promise<void>
}

interface EaphoneQueryOptions {
  baseContext: string
  datasource: DataSource
  resources: ResourceConfig[]
}
```

## QueryFilter → 内部表示

```typescript
interface ParsedCondition {
  field: string            // 字段路径，如 'user.address.city'
  op: FilterOperator       // '_eq' | '_gt' | '_like' | ...
  value: unknown           // 类型转换后的值
}

interface ParsedSort {
  field: string
  dir: 'asc' | 'desc'
}

interface ParsedQuery {
  draw?: number
  offset: number
  limit: number
  conditions: ParsedCondition[]   // AND 组合
  sort: ParsedSort[]
}
```

## 操作符 → 查询语言映射

### MongoDB

| 操作符 | MongoDB Filter |
|--------|----------------|
| `_eq` | `{field: val}` |
| `_ne` | `{field: {$ne: val}}` |
| `_gt` | `{field: {$gt: val}}` |
| `_gte` | `{field: {$gte: val}}` |
| `_lt` | `{field: {$lt: val}}` |
| `_lte` | `{field: {$lte: val}}` |
| `_in` | `{$or: [{field: null}, {field: {$in: list}}]}`（含 null 时拆分） |
| `_nin` | `{$and: [{field: {$ne: null}}, {field: {$nin: list}}]}`（含 null 时） |
| `_all` | `{field: {$all: list}}` |
| `_regex` | `{field: {$regex: str}}` |
| `_like` | `{field: {$regex: <converted-pattern>}}` |
| `_notlike` | `{field: {$not: {$regex: <converted-pattern>}}}` |
| `_null` | `{field: null}`（仅 true） |
| `_empty` | `{field: ""}`（仅 true） |
| `_isvoid` | `{$or: [{field: null}, {field: ""}, {field: {$exists: false}}]}` |
| `_exists` | `{field: {$exists: bool}}` |

### SQLite

使用参数化查询（`?` 占位符防注入）。

| 操作符 | SQL WHERE |
|--------|-----------|
| `_eq` | `field = ?` |
| `_ne` | `field <> ?` |
| `_gt` | `field > ?` |
| `_gte` | `field >= ?` |
| `_lt` | `field < ?` |
| `_lte` | `field <= ?` |
| `_in` | `(field IS NULL OR field IN (?,?))`（含 null 时） |
| `_nin` | `(field IS NOT NULL AND field NOT IN (?,?))`（含 null 时） |
| `_like` | `field LIKE ?` |
| `_notlike` | `field NOT LIKE ?` |
| `_null` | `field IS NULL`（仅 true） |
| `_empty` | `field = ''`（仅 true） |
| `_isvoid` | `(field IS NULL OR field = '')` |
| `_all` | 不支持（文档说明） |
| `_regex` | 不支持（SQLite 默认无 REGEXP） |
| `_exists` | 仅对 JSON 列有意义 |

## 实现分工（10 个 Phase）

### Phase 1: 项目初始化

- 创建 `pnpm-workspace.yaml`，声明 `packages/*` 和 `apps/*`
- 创建 `tsconfig.base.json`（strict, target ES2020, module NodeNext）
- Root `package.json`（private, scripts）
- 安装公共 devDeps: `typescript`, `vitest`, `tsx`, `@types/node`

### Phase 2: Core — 类型系统

`packages/core/src/types.ts`

定义所有接口：
- `QueryInput`, `QueryFilter`, `QueryOrder`, `QueryOutput<T>`, `CountInput`, `CountOutput`
- `FilterOperator` union type
- `ColumnTypeDef` type
- `ParsedCondition`, `ParsedSort`, `ParsedQuery`
- `DataSource` interface（`query`, `count`, `close`）
- `ResourceConfig`, `EaphoneQueryOptions`

### Phase 3: Core — 类型转换器

`packages/core/src/type-converters.ts`

- `convertValue(value: unknown, typeDef?: ColumnTypeDef): unknown`
- 按 ColumnTypeDef 转换：STRING→string, INTEGER→number, DOUBLE→number, BOOLEAN→boolean, DATE→Date
- DATE 解析：ISO 8601 字符串 → JS `Date`，也支持 `YYYY-MM-DD` 日期格式
- 转换失败返回 `null`（与 Java `tryConvert` 一致，静默失败）

### Phase 4: Core — 条件解析引擎

`packages/core/src/query/parser.ts`

- `parseOrderBy(orderBy: QueryOrder[]): ParsedSort[]` — 归一化两种 JSON 格式
- `parseFilter(field: string, filter: QueryFilter, schema?: Record<string, ColumnTypeDef>): ParsedCondition[]`
  - 按 Java 优先级：`_eq > _ne > _isvoid > 其余`
  - 每个非空操作符展开为一条 `ParsedCondition`
  - 对值做类型转换
- `parseQueryInput(input: QueryInput, schema?: Record<string, ColumnTypeDef>): ParsedQuery`
- `parseCountInput(input: CountInput, schema?: Record<string, ColumnTypeDef>): ParsedCondition[]`

### Phase 5: Core — Express 中间件

`packages/core/src/middleware.ts`

- `createEaphoneQueryMiddleware(options: EaphoneQueryOptions): RequestHandler`
- 遍历 `options.resources`，为每个资源注册：
  - `POST ${baseContext}${path}/search`
  - `POST ${baseContext}${path}/count`
- 处理流程：
  1. `req.body` 解析为 `QueryInput`（try/catch → `{error: "Invalid JSON"}`）
  2. 校验 draw/offset/limit
  3. 查询 schema（从 `ResourceConfig.schema` 获取）
  4. `parseQueryInput(input, schema)` → `ParsedQuery`
  5. `datasource.query(table, parsed)` / `datasource.count(table, conditions)`
  6. 返回 `QueryOutput` JSON（status 200，即使有 error）
- 错误处理：catch 所有异常，返回 `{error: message}`（与 Java 一致）

### Phase 6: SQLite 数据源

`packages/sqlite/src/index.ts`

依赖：`@eaphone/query-core`, `better-sqlite3`, `@types/better-sqlite3`

- `createSqliteDataSource(options: { dbPath: string }): DataSource`
- 初始化：
  - 打开 SQLite 连接（同步）
  - 遍历所有 `table`，执行 `PRAGMA table_info(table_name)` 获取列名和类型
  - 缓存为 `Map<tableName, Map<colName, ColumnTypeDef>>`
  - 用户提供的 schema 优先级高于 PRAGMA 结果
- `query(table, parsed)`：
  - 构建 `SELECT * FROM table WHERE ... ORDER BY ... LIMIT ? OFFSET ?`
  - WHERE 子句：用 `AND` 连接每个条件，参数用 `?` 占位符
  - ORDER BY 子句：`field ASC/DESC`，字段名直接拼接（白名单校验防止注入）
  - 执行 SQL → rows → QueryOutput
- `count(table, conditions)`：
  - `total`: `SELECT COUNT(*) FROM table`（无条件）
  - `filtered`: `SELECT COUNT(*) FROM table WHERE ...`
  - 返回 `{ total, filtered }`
- `close()`：`db.close()`

### Phase 7: MongoDB 数据源

`packages/mongodb/src/index.ts`

依赖：`@eaphone/query-core`, `mongodb`

- `createMongoDataSource(options: { uri: string; dbName: string }): DataSource`
- 初始化：`new MongoClient(uri)`，连接到指定 db
- `query(table, parsed)`：
  - 构建 Filter 文档：遍历 `conditions`，按操作符映射为 MongoDB Filter（见上表）
  - `collection.find(filter).sort(sortObj).skip(offset).limit(limit).toArray()`
  - 返回 `QueryOutput`
- `count(table, conditions)`：
  - `total`: `db.collection.estimatedDocumentCount()`
  - `filtered`: `db.collection.countDocuments(filter)`
  - 返回 `{ total, filtered }`
- `close()`：`client.close()`

### Phase 8: 测试

使用 `vitest`，放在各 package 的 `src/__tests__/` 目录。

- **Core — parser 测试**：每个操作符的解析结果、order_by 两种格式、空输入、类型转换
- **Core — type-converters 测试**：各类型转换、转换失败返回 null
- **SQLite 集成测试**：内存数据库（`:memory:`），创建表 INSERT 数据，测试 query 和 count
- **MongoDB Filter 构建测试**：mock 或纯函数测试 Filter 文档生成正确性
- **Middleware 测试**：mock DataSource，测试路由注册和请求/响应

### Phase 9: 示例项目

`apps/samples/`

- 独立 `package.json`，dependencies 引用 `@eaphone/query-core` + `@eaphone/query-sqlite` / `@eaphone/query-mongodb`（workspace 协议）
- `sqlite-sample.ts`：创建表、插入示例数据、启动 express、说明 curl 示例
- `mongodb-sample.ts`：同上的 MongoDB 版本

### Phase 10: 文档

`docs/usage-ts.md`

- 快速开始
- 配置说明（baseContext, datasource, resources, schema）
- SQLite 示例 + MongoDB 示例
- API 参考（请求/响应格式）
- 行为说明（如下）

## 需要文档化的 Java 特殊行为

1. **`_null: false` 被忽略** — Java 仅处理 `_null: true`，`false` 相当于没传。如果需要 IS NOT NULL，应使用 `_ne: null` 或其他方式。
2. **`_eq`/`_ne`/`_isvoid` 互斥** — 这三个以 `if/else if/else` 链执行，出现任一后同一 QueryFilter 中其他操作符（`_gt`、`_like` 等）被跳过。优先级：`_eq > _ne > _isvoid > 其余`。
3. **`_like` 的 `%` 通配符** — SQL 标准行为，不传 `%` 则隐含首尾锚定（如 `_like: "foo"` 等效于 `_eq: "foo"`）。`%` 的位置决定模糊匹配范围。
4. **`_in`/`_nin` 含 `null` 时拆分** — 数组含 `null` 时，null 值单独用 `IS NULL`/`IS NOT NULL` 处理，非 null 值用 `IN`/`NOT IN`，通过 `OR`/`AND` 组合。
5. **`_all` 在 SQLite 不支持** — MongoDB 特有的数组包含操作符，SQLite 无对应实现。
6. **`_regex` 在 SQLite 默认不可用** — SQLite 需要加载 `REGEXP` 扩展才有该功能，建议用 `_like` 替代。
7. **`_exists` 语义** — MongoDB `$exists` 操作符，仅在 MongoDB 有意义；SQLite 中只对 JSON 列或可选列适用。
8. **类型转换失败返回 null** — Java 实现中 `tryConvert` 解析失败静默返回 `null`（不抛异常），TS 保持相同行为。

## 实现顺序

```
Phase 1 (root init)
  → Phase 2 (core types)
    → Phase 3 (core type-converters)
      → Phase 4 (core parser)
        → Phase 5 (core middleware)
          → Phase 6 (sqlite datasource)
          → Phase 7 (mongodb datasource)
          (Phase 6 & 7 可并行)
            → Phase 8 (tests)
              → Phase 9 (samples)
                → Phase 10 (docs)
```
