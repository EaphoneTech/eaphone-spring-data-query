# 关于查询 #

在实际操作中，在 DataTables 的通信协议的基础上，遇到了一些不足，主要有以下方面：

* 通过 `columns[i][search][value]` 实现的按列搜索功能，不能指定范围。比如 “指定日期范围” 或者 “大于 xx 的值” 这种场景，无法在统一的一个通信协议里传递
* 通过 `columns[i][search][regex]` 只能指定 `普通搜索` 和 `按正则表达式搜索` 两种场景，

## 查询方式 ##

建议在 POST 接口上通过 `@RequestBody` 方式传入较为完整的查询内容。

```json
{
    "draw": 1,
    "offset": 0,
    "limit": 10,
    "order_by": [{
        "field": "field1",
        "dir": "asc"
    }, {
        "field": "field2",
        "dir": "desc"
    }],
    "where": {
        "field1": {
            "_gt": "",
            "_gte": "",
            "_lt": "",
            "_lte": "",
            "_eq": "",
            "_ne": "",
            "_in": [],
            "_nin": [],
            "_regex": "",
            "_like": "",
            "_exists": true,
            "_isNull": true,
            "_isEmpty": true
        }
    }
}
```

## 示例 ##

### 最简单的请求 ###

因为 `draw`, `offset`, `limit`, `order_by`, `where` 全都改为可选的，因此最简单的请求就变成了

```http
POST /search

{}
```

即：不需要任何参数。

### 分页 ###

单纯分页只需要传入 `offset` 和 `limit` 。（当然如果 `limit` 和后端一致的话也可以不传了）

```http
POST /search

{
    "offset": 30,
    "limit": 10
}
```

### 按某一列排序 ###

最简单的排序：

```http
POST /search

{
    "order_by": [{
        "field": "price.value",
        "dir": "desc"
    }]
}
```

稍微复杂一些的排序（请注意排序的顺序是有意义的，因此使用数组形式）：

```http
POST /search

{
    "order_by": [{
        "field": "price.value",
        "dir": "asc"
    }, {
        "field": "createTime",
        "dir": "desc"
    }],
}
```

### 按某一列筛选 ###

* 精确查询

```http
POST /search

{
    "where": {
        "user.name": {
            "_eq": "张三丰"
        }
    }
}
```

* 模糊查询

```http
POST /search

{
    "where": {
        "user.name": {
            "_like": "张三%"
        }
    }
}
```

* 按数值范围

```http
POST /search

{
    "where": {
        "price.value": {
            "type": "double",
            "_gte": 9.0,
            "_lt": 9.5
        }
    }
}
```

* 按时间范围

```http
POST /search

{
    "where": {
        "createTime": {
            "type": "date",
            "_lt": "2017-09-20",
            "_gt": "2017-09-19"
        }
    }
}

```

* 按枚举 (及 `$in` 操作)

```http
POST /search

{
    "where": {
        "status": {
            "_in": ["已删除", "已失效"]
        }
    }
}
```

## 复合查询 ##

默认多个查询的参数之间是 `AND` 关系。

```http
POST /search

{
    "where": {
        "name": {
            "_like": "张三%"
        },
        "status": {
            "_eq": "有效"
        },
        "createTime": {
            "type": "date"
            "_gte": "2018-09-18"
        }
    }
}

```

## 参考 ##

* [DataTables: Server-side processing](https://datatables.net/manual/server-side) 和相应的 [中文版](http://datatables.club/manual/server-side.html)
