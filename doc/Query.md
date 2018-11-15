# Why #

I use DataTables in some projects and wrote SpringMVC server side for it. Later the frontend is rewritten and DataTables is no longer used. Now is the time to refine the protocol for general data-grid query. 

* You can not assign a range via `columns[i][search][value]`, as in SQL: `BETWEEN`.
* You can only do regular search OR regex search via `columns[i][search][regex]`, but not `=`, `LIKE`.

## Query ##

HTTP POST as `@RequestBody` is recommended:

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

## Examples ##

### Basic Query ###

None of the `draw`, `offset`, `limit`, `order_by`, `where` are required, so the basic query is:

```http
POST /search

{}
```

e.g. NO parameters are needed.

### Pagination ###

Paging is controlled by `offset` and `limit`, which keeps the same as `SKIP` and `LIMIT` in SQL. 

```http
POST /search

{
    "offset": 30,
    "limit": 10
}
```

### Ordering ###

Ordering by one column:

```http
POST /search

{
    "order_by": [{
        "field": "price.value",
        "dir": "desc"
    }]
}
```

Ordering by more columns: 

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

### Filtering ###

* Search by equality

```http
POST /search

{
    "where": {
        "user.name": {
            "_eq": "James Bond"
        }
    }
}
```

* Like (`LIKE %value%`)

```http
POST /search

{
    "where": {
        "user.name": {
            "_like": "James%"
        }
    }
}
```

* By Numerical Range

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

* Date Range & Time Range

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

* In

```http
POST /search

{
    "where": {
        "status": {
            "_in": ["Deleted", "Invalid"]
        }
    }
}
```

## Multiple Filters ##

Multiple filters are joined by `AND` logic.

```http
POST /search

{
    "where": {
        "name": {
            "_like": "James"
        },
        "status": {
            "_eq": "Valid"
        },
        "createTime": {
            "type": "date"
            "_gte": "2018-09-18"
        }
    }
}

```

## References ##

* [DataTables: Server-side processing](https://datatables.net/manual/server-side)
