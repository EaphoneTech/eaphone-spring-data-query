import type { RequestHandler, Router } from 'express'
import { Router as createRouter } from 'express'
import type { EaphoneQueryOptions, QueryInput, QueryOutput, CountOutput } from './types.js'
import { parseQueryInput, parseCountInput } from './query/parser.js'

export function createEaphoneQueryMiddleware(options: EaphoneQueryOptions): RequestHandler {
  const router = createRouter()

  for (const resource of options.resources) {
    const base = options.baseContext.endsWith('/')
      ? options.baseContext
      : options.baseContext + '/'

    const searchPath = `${base}${resource.path}/search`
    const countPath = `${base}${resource.path}/count`

    router.post(searchPath, async (req, res) => {
      try {
        const input: QueryInput = req.body
        if (typeof input !== 'object' || input === null) {
          res.json({ draw: undefined, total: 0, filtered: 0, data: [], error: 'Invalid request body' } satisfies QueryOutput)
          return
        }
        const parsed = parseQueryInput(input, resource.schema)
        const result = await options.datasource.query(resource.table, parsed)
        result.draw = result.draw ?? input.draw
        res.json(result)
      } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : String(err)
        res.json({ total: 0, filtered: 0, data: [], error: msg } satisfies QueryOutput)
      }
    })

    router.post(countPath, async (req, res) => {
      try {
        const input: QueryInput = req.body
        if (typeof input !== 'object' || input === null) {
          res.json({ total: 0, filtered: 0, error: 'Invalid request body' } satisfies CountOutput)
          return
        }
        const conditions = parseCountInput(input, resource.schema)
        const result = await options.datasource.count(resource.table, conditions)
        res.json(result)
      } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : String(err)
        res.json({ total: 0, filtered: 0, error: msg } satisfies CountOutput)
      }
    })
  }

  return router
}
