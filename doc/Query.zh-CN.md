# 关于查询 #

在实际操作中，在 DataTables 的通信协议的基础上，遇到了一些不足，主要有以下方面：

* 通过 `columns[i][search][value]` 实现的按列搜索功能，不能指定范围。比如 “指定日期范围” 或者 “大于 xx 的值” 这种场景，无法在统一的一个通信协议里传递
* 通过 `columns[i][search][regex]` 只能指定 `普通搜索` 和 `按正则表达式搜索` 两种场景，

## 查询方式 ##

为了便于操作，提供了如下几种方式：

1. 在 GET 接口上通过 query 方式即 `?firstname=Dave&lastname=Matthews` 进行简单的查询。
2. 在 POST 接口上通过 `@RequestBody` 方式传入较为完整的查询内容

```json
{
    "draw": 1,
    "start": 0,
    "length": 10,
    "orders": [{
        "field": "field1",
        "dir": "asc"
    }, {
        "field": "field2",
        "dir": "desc"
    }],
    "filters": {
        "field1": {
            "gt": "",
            "gte": "",
            "lt": "",
            "lte": "",
            "eq": "",
            "ne": "",
            "in": [],
            "nin": [],
            "regex": "",
            "like": "",
            "exists": true,
            "isNull": true,
            "isEmpty": true
        }
    }
}
```

## 实例 ##

为了增强可读性，下面的请求认为在 `@GetMapping("/")` 上进行，并且参数都没有进行转码。

### 最简单的请求 ###

因为 `order`, `columns`, `search` 全都改为可选的，因此最简单的请求就变成了

```http
GET /
```

```http
POST /search

{}
```

即：不需要任何参数

### 分页 ###

单纯分页只需要传入 `start` 和 `length` 。（当然如果 `length` 和后端一致的话也可以不传了）

```http
GET /?start=30&length=10
```

```http
POST /search

{
    "start": 30,
    "length": 10
}
```

### 按某一列排序 ###

最简单的排序：

```http
GET /?$order=price.value,desc
```

稍微复杂一些的排序（请注意排序的顺序是有意义的，因此使用数组形式）：

```http
POST /search

{
    "orders": [{
        "field": "price.value",
        "dir": "asc"
    }, {
        "field": "createTime",
        "dir": "desc"
    }],
}
```

### 按某一列精确搜索 ###

```http
GET /?user.name=张三丰
```

```http
POST /search

{
    "filters": {
        "user.name": {
            "eq": "张三丰"
        }
    }
}
```

### 按某一列筛选 ###

* 模糊查询

```http
GET /?user.name.$like=张三丰
```

```http
POST /search

{
    "filters": {
        "user.name": {
            "like": "张三丰"
        }
    }
}
```

* 按数值范围

```http
GET /?price.value.$gte=9.0&price.value.$lt=9.5
```

```http
POST /search

{
    "filters": {
        "price.value": {
            "gte": 9.0,
            "lt": 9.5
        }
    }
}
```

* 按时间范围

```http
GET /?createTime.$lt=2017-09-20&createTime.$gt=2017-09-19
```

```http
POST /search

{
    "filters": {
        "createTime": {
            "lt": "2017-09-20",
            "gt": "2017-09-19"
        }
    }
}

```

* 按枚举 (及 `$in` 操作)

```http
GET /?status.$in=已删除,已失效
```

```http
POST /search

{
    "filters": {
        "status": {
            "in": ["已删除", "已失效"]
        }
    }
}
```

## 复合查询 ##

默认多个查询的参数之间是 `AND` 关系。

```http
GET /?name.$like=张三&status=有效&createTime.$gte=2018-09-18
```

```http
POST /search

{
    "filters": {
        "name": {
            "like": "张三"
        },
        "status": "有效",
        "createTime": {
            "gte": "2018-09-18"
        }
    }
}

```

## 参考 ##

* [DataTables: Server-side processing](https://datatables.net/manual/server-side) 和相应的 [中文版](http://datatables.club/manual/server-side.html)
