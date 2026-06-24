# TODO: ts port

## 背景

已有 java 项目，现在要增加 ts 的服务器端实现。

本项目定义了一个 POST /search 方法的接口，用它可以用前端实现较为灵活的查询条件。具体功能包括：

- 0 或多个字段的 AND 组合
- 支持 limit 和 offset 以实现分页   
- 支持 mongodb, MySQL 和 SQLite (通过 jpa)

## HTTP 接口的形式

具体参见 doc/Query.md

## ts 的技术要求

- 项目放在 eaphone-data-query-ts 目录中
- 包管理器使用 pnpm
- 使用 typescript
- 进行必要的类型转换 (如时间/日期/时间戳)
- 支持字段嵌套 (mongodb)
- 提供 express 中间件
  - 使用一个 options 初始化整个中间件
    - 基础 context (例如, `/v1/`)
    - 一个数据源 (至少需要支持 mongodb 和 sqlite) 的连接方式
    - 多个资源路径 (例如, order) 和对应的表名 (例如, `t_order`)
    - 为每个资源路径注册 `POST /search` 和 `POST /count` 两个接口
- 带有单元测试
- 带有示例项目 写到 eaphone-data-query-ts-samples 目录下
- 编写使用和配置文档 写到 docs/usage-ts.md 中
