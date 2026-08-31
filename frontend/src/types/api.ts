/**
 * Unified backend response wrapper, mirroring backend Result<T>.
 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

/**
 * MyBatis-Plus pagination result shape.
 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}
