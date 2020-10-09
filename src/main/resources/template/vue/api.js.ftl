import axios from '@/libs/api.request'

export const getTableData = (data) => {
  return axios.request({
    url: '${moduleName}/${pathName}/list',
    method: 'get',
    params: data
  })
}
export const getDataById = (id) => {
  return axios.request({
    url: '${moduleName}/${pathName}/info/' + id,
    method: 'get'
  })
}
export const save = (data) => {
  return axios.request({
    url: '${moduleName}/${pathName}/save',
    method: 'post',
    data: data
  })
}
export const doDelHandler = (data) => {
  return axios.request({
    url: '${moduleName}/${pathName}/delete',
    method: 'post',
    data: data
  })
}
