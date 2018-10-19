# Why #

I use DataTables in some projects and wrote SpringMVC server side for it. Later the frontend is rewritten and DataTables is no longer used. Now is the time to refine the protocol for general data-grid query. 

* You can not assign a range via `columns[i][search][value]`, as in SQL: `BETWEEN`.
* You can only do regular search OR regex search via `columns[i][search][regex]`, but not `=`, `LIKE`.

## Query ##

Two query methods are designed as follows:

1. Using HTTP GET , as `?firstname=Dave&lastname=Matthews` (NOTE: Not Implemented Yet);
2. Using HTTP POST, as `@RequestBody`:

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

## Examples ##

In order to increase readability, all query strings in following HTTP GET examples are not escaped. (And they are NOT supported yet).

### Basic Query ###

None of the `draw`, `start`, `length`, `orders`, `filters` are required, so the basic query is:

```http
GET /
```

```http
POST /search

{}
```

e.g. NO parameters are needed.

### Pagination ###

Paging is controlled by `start` and `length`, which is the same as `SKIP` and `LIMIT`. 

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

### Ordering ###

Ordering by one column:

```http
GET /?$order=price.value,desc
```

```http
POST /search

{
    "orders": [{
        "field": "price.value",
        "dir": "desc"
    }]
}
```

Ordering by more columns: 

```http
GET /?$order=price.value,desc&$order=createTime,desc
```

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

### Accurate Search ###

```http
GET /?user.name=JamesBond
```

```http
POST /search

{
    "filters": {
        "user.name": {
            "eq": "JamesBond"
        }
    }
}
```

### Filtering ###

* Like (`LIKE %value%`)

```http
GET /?user.name.$like=JamesBond
```

```http
POST /search

{
    "filters": {
        "user.name": {
            "like": "JamesBond"
        }
    }
}
```

* Numerical Range

```http
GET /?price.value.type=double&price.value.$gte=9.0&price.value.$lt=9.5
```

```http
POST /search

{
    "filters": {
        "price.value": {
            "type": "double",
            "gte": 9.0,
            "lt": 9.5
        }
    }
}
```

* Date Range & Time Range

```http
GET /?createTime.type=date&createTime.$lt=2017-09-20&createTime.$gt=2017-09-19
```

```http
POST /search

{
    "filters": {
        "createTime": {
            "type": "date",
            "lt": "2017-09-20",
            "gt": "2017-09-19"
        }
    }
}
```

* In

```http
GET /?status.$in=Deleted,Invalid
```

```http
POST /search

{
    "filters": {
        "status": {
            "in": ["Deleted", "Invalid"]
        }
    }
}
```

## Multiple Filters ##

Multiple filters are joined by `AND` logic.

```http
GET /?name.$like=JamesBond&status=Valid&createTime.$type=date&createTime.$gte=2018-09-18
```

```http
POST /search

{
    "filters": {
        "name": {
            "like": "JamesBond"
        },
        "status": {
            "eq": "Valid"
        },
        "createTime": {
            "type": "date"
            "gte": "2018-09-18"
        }
    }
}

```

## References ##

* [DataTables: Server-side processing](https://datatables.net/manual/server-side)
