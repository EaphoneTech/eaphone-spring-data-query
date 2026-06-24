import express from 'express'
import { createEaphoneQueryMiddleware } from '@eaphone/query-core'
import { createSqliteDataSource } from '@eaphone/query-sqlite'

async function main() {
  const datasource = await createSqliteDataSource({ dbPath: './sample.db' })

  const app = express()
  app.use(express.json())

  app.use(createEaphoneQueryMiddleware({
    baseContext: '/v1/',
    datasource,
    resources: [
      { path: 'order', table: 't_order' },
    ],
  }))

  app.listen(3000, () => {
    console.log('SQLite sample server running on http://localhost:3000')
    console.log('Endpoints:')
    console.log('  POST /v1/order/search')
    console.log('  POST /v1/order/count')
    console.log('')
    console.log('Example:')
    console.log('  curl -X POST http://localhost:3000/v1/order/search \\')
    console.log('    -H "Content-Type: application/json" \\')
    console.log('    -d \'{"offset":0,"limit":10}\'')
  })
}

main().catch(console.error)
