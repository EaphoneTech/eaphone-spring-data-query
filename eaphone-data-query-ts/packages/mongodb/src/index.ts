import type { DataSource, ParsedQuery, ParsedCondition, QueryOutput, CountOutput } from '@eaphone/query-core'
import { MongoClient, type Db, type Filter, type Sort, type Document } from 'mongodb'

export interface MongoDataSourceOptions {
  uri: string
  dbName: string
}

function getLikeFilterPattern(filterValue: string): RegExp {
  let pattern = filterValue
  // Escape regex metacharacters
  pattern = pattern.replace(/[.+?*^$()\[\]]/g, '\\$&')
  // Handle start/end anchors
  if (!filterValue.startsWith('%')) {
    pattern = '^' + pattern
  }
  if (!filterValue.endsWith('%')) {
    pattern = pattern + '$'
  }
  // Replace % with .*
  pattern = pattern.replace(/%/g, '.*')
  return new RegExp(pattern, 'i')
}

function buildFilter(field: string, conditions: ParsedCondition[]): Filter<Document> {
  if (conditions.length === 0) return {}

  const filters: Filter<Document>[] = []

  for (const cond of conditions) {
    if (cond.op === '_eq') {
      filters.push({ [field]: cond.value } as Filter<Document>)
    } else if (cond.op === '_ne') {
      filters.push({ [field]: { $ne: cond.value } } as Filter<Document>)
    } else if (cond.op === '_gt') {
      filters.push({ [field]: { $gt: cond.value } } as Filter<Document>)
    } else if (cond.op === '_gte') {
      filters.push({ [field]: { $gte: cond.value } } as Filter<Document>)
    } else if (cond.op === '_lt') {
      filters.push({ [field]: { $lt: cond.value } } as Filter<Document>)
    } else if (cond.op === '_lte') {
      filters.push({ [field]: { $lte: cond.value } } as Filter<Document>)
    } else if (cond.op === '_in') {
      const arr = cond.value as unknown[]
      const nonNull = arr.filter((v) => v !== null)
      const hasNull = nonNull.length !== arr.length
      if (hasNull) {
        if (nonNull.length === 0) {
          filters.push({ [field]: null } as Filter<Document>)
        } else {
          filters.push({
            $or: [
              { [field]: null },
              { [field]: { $in: nonNull } },
            ],
          } as Filter<Document>)
        }
      } else {
        filters.push({ [field]: { $in: arr } } as Filter<Document>)
      }
    } else if (cond.op === '_nin') {
      const arr = cond.value as unknown[]
      const nonNull = arr.filter((v) => v !== null)
      const hasNull = nonNull.length !== arr.length
      if (hasNull) {
        if (nonNull.length === 0) {
          filters.push({ [field]: { $ne: null } } as Filter<Document>)
        } else {
          filters.push({
            $and: [
              { [field]: { $ne: null } },
              { [field]: { $nin: nonNull } },
            ],
          } as Filter<Document>)
        }
      } else {
        filters.push({ [field]: { $nin: arr } } as Filter<Document>)
      }
    } else if (cond.op === '_all') {
      filters.push({ [field]: { $all: cond.value as unknown[] } } as Filter<Document>)
    } else if (cond.op === '_regex') {
      filters.push({ [field]: { $regex: cond.value as string } } as Filter<Document>)
    } else if (cond.op === '_like') {
      filters.push({ [field]: { $regex: getLikeFilterPattern(cond.value as string).source, $options: 'i' } } as Filter<Document>)
    } else if (cond.op === '_notlike') {
      filters.push({
        [field]: { $not: { $regex: getLikeFilterPattern(cond.value as string).source, $options: 'i' } },
      } as Filter<Document>)
    } else if (cond.op === '_null') {
      if (cond.value === true) {
        filters.push({ [field]: null } as Filter<Document>)
      }
    } else if (cond.op === '_empty') {
      if (cond.value === true) {
        filters.push({ [field]: '' } as Filter<Document>)
      }
    } else if (cond.op === '_isvoid') {
      if (cond.value === true) {
        filters.push({
          $or: [
            { [field]: null },
            { [field]: '' },
            { [field]: { $exists: false } },
          ],
        } as Filter<Document>)
      } else if (cond.value === false) {
        filters.push({
          $and: [
            { [field]: { $ne: null } },
            { [field]: { $ne: '' } },
            { [field]: { $exists: true } },
          ],
        } as Filter<Document>)
      }
    } else if (cond.op === '_exists') {
      filters.push({ [field]: { $exists: cond.value as boolean } } as Filter<Document>)
    }
  }

  if (filters.length === 0) return {}
  return filters.length === 1 ? filters[0] : { $and: filters }
}

function buildSort(sort: ParsedQuery['sort']): Sort {
  if (sort.length === 0) return {}
  const s: Record<string, 1 | -1> = {}
  for (const item of sort) {
    s[item.field] = item.dir === 'desc' ? -1 : 1
  }
  return s as Sort
}

export async function createMongoDataSource(options: MongoDataSourceOptions): Promise<DataSource> {
  const client = new MongoClient(options.uri)
  await client.connect()
  const db: Db = client.db(options.dbName)

  async function query(table: string, input: ParsedQuery): Promise<QueryOutput> {
    const collection = db.collection(table)

    const filter = buildFilter('', input.conditions)
    const sort = buildSort(input.sort)

    const total = await collection.estimatedDocumentCount()

    let filtered: number
    if (input.conditions.length === 0) {
      filtered = total
    } else {
      filtered = await collection.countDocuments(filter)
    }

    const cursor = collection.find(filter).sort(sort).skip(input.offset).limit(input.limit)
    const data = await cursor.toArray()

    const result: Record<string, unknown>[] = data.map((doc) => {
      const { _id, ...rest } = doc
      return { ...rest, _id: _id?.toString() }
    })

    return { total, filtered, data: result }
  }

  async function count(table: string, conditions: ParsedCondition[]): Promise<CountOutput> {
    const collection = db.collection(table)

    const total = await collection.estimatedDocumentCount()

    let filtered: number
    if (conditions.length === 0) {
      filtered = total
    } else {
      const filter = buildFilter('', conditions)
      filtered = await collection.countDocuments(filter)
    }

    return { total, filtered }
  }

  async function close(): Promise<void> {
    await client.close()
  }

  return { query, count, close }
}
