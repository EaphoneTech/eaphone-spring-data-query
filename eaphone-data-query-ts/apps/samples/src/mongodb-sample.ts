import express from 'express'
import { createEaphoneQueryMiddleware } from '@eaphone/query-core'
import { createMongoDataSource } from '@eaphone/query-mongodb'

async function main() {
  const datasource = await createMongoDataSource({
    uri: 'mongodb://localhost:27017',
    dbName: 'sample_db',
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

  app.listen(3001, () => {
    console.log('MongoDB sample server running on http://localhost:3001')
    console.log('Endpoints:')
    console.log('  POST /v1/order/search')
    console.log('  POST /v1/order/count')
  })
}

main().catch(console.error)
