import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia

export * from './modules/auth'
export * from './modules/app'
export * from './modules/permission'
export * from './modules/tabs'
