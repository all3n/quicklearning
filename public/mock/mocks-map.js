import user from './user'
import table from './table'
import job from './job'
import index from './historyserver'

const mocks_map = {
  'appmaster': [
    ...user,
    ...table,
    ...job
  ],
  'historyserver':[
    ...index
  ]
}
export default mocks_map
