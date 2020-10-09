<template>
  <div>
    <Card>
      <Form ref="searchForm" :model="searchForm" inline>
        <FormItem prop="key">
          <Input v-model="searchForm.key" placeholder="关键字" style="width:240px;" clearable>
          <Select v-model="searchForm.type" slot="prepend" style="width: 80px">
            <Option value="0">用户名</Option>
            <Option value="1">邮箱</Option>
            <Option value="2">手机号</Option>
          </Select>
          </Input>
        </FormItem>
        <FormItem>
          <Button type="primary" @click="doSearch" icon="ios-search">搜索</Button>
          <Button @click="doReset" icon="md-refresh" style="margin-left:15px;" >重置</Button>
          <Button @click="doAdd" style="margin-left:30px;" icon="md-add">增加</Button>
        </FormItem>
        <FormItem style="float:right">
          <Button @click="exportExcel" icon="md-download">导出</Button>
          <Button style="margin-left:15px;" @click="exportExcelAll" icon="md-download">导出所有</Button>
        </FormItem>
      </Form>
      <Alert show-icon>
          已选择 <span class="select-count">{{selectCount}}</span> 项
          <a class="select-clear" @click="clearSelectAll">清空</a>
          <a class="select-clear" v-if="selectCount > 0" @click="doDelSelected" style="margin-left:10px;">批量删除</a>
      </Alert>
      <Table ref="table" :loading="loading" :columns="columns" :data="tableData" sortable="custom" :height="tableOptions.height"
             @on-selection-change="showSelect" @on-sort-change="changeSort" stripe/>
      <Page ref="tablePage" :total="pageOptions.totalCount" :current="pageOptions.current"
          :page-size="pageOptions.pageSize" @on-change="changePage" @on-page-size-change="changePageSize" show-sizer show-total style="margin-top:20px;text-align:right;"/>
    </Card>
  </div>
</template>

<script>
<#if openFrontLowercase >
import { getTableData, doDelHandler } from '@/api/${pathName}'
<#else>
import { getTableData, doDelHandler } from '@/api/${classname}'
</#if>
import sysparam from '@/libs/sysparam'
import moment from 'moment'
export default {
  name: 'tables_page',
  data () {
    return {
      loading: false,
      searchForm: {
        key: '',
        type: '0',
        sortField: 'ctime',
        sortType: 'desc'
      },
      pageOptions: {
        currentPage: 1,
        totalCount: 0,
        pageSize: 10
      },
      tableOptions: {
        height: 450
      },
      selectCount: 0, // 多选计数
      selectList: [], // 多选数据
      columns: [
          {
              title: '操作',
              key: 'handle',
              fixed: 'left',
              align: 'center',
              width: 100,
              render: (h, params) => {
              return h('div', [
                  h('a', {
                      props: {
                          type: 'text',
                          size: 'small'
                      },
                      on: {
                          click: () => {
                          const id = params.row.${pk.attrname}
                                  const route = {
                              name: '${classname}_edit',
                              params: {
                                  id
                              }
                          }
                          this.$router.push(route)
                  }
          }
    }, '编辑'),
      h('a', {
          props: {
              type: 'text',
              size: 'small'
          },
          attrs: {
              style: 'margin-left:10px'
          },
          on: {
              click: () => {
              const id = params.row.${pk.attrname}
              this.doDelete(id)
          }
      }
  }, '删除')
  ])
  }
  },
        { type: "selection", width: 60, align: "centerthis.pageOptions.currentPage," },
    <#list columns as column>
    <#if column.columnName == "ctime">
        {
          title: '创建时间',
          key: 'ctime',
          sortable: 'custom',
          sortType: 'desc',
          align: 'center',
          width: 150,
          render: (h, params) => {
            return h('span', params.row.ctime ? moment(params.row.ctime).format('YYYY-MM-DD HH:mm:ss') : '-')
          }
        },
    <#elseif column.attrType == "Date">
        {
          title: '${column.comments}',
          key: '${column.attrname}',
          align: 'center',
          width: 150,
          render: (h, params) => {
            return h('span', params.row.${column.attrname} ? moment(params.row.${column.attrname}).format('YYYY-MM-DD HH:mm:ss') : '-')
          }
        },
    <#else>
        { title: '${column.comments}', key: '${column.attrname}', width: 120, },
    </#if>
    </#list>
    ],
    tableData: []
    }
  },
  methods: {
    init () {
      this.tableOptions.height = window.innerHeight - this.$refs.table.$el.offsetTop - 185
    },
    exportExcel () {
      let fileName = '${comments}' + new Date().valueOf() + '.csv'

      this.$exportCsv(fileName, this.tableData, this.columns, this.$refs.table)
    },
    exportExcelAll () {
        let fileName ='${comments}' + new Date().valueOf() + '.csv'
        let params = {
          page: 1,
          pageSize: 1000000,
        }
        getTableData(params).then(res => {
            let data = res.data.data
            this.$Loading.start();
            this.$exportCsv(fileName,  data.list, this.columns, this.$refs.table)
            this.$Loading.finish();
        })
    },
    doSearch () {
      this.loading = true
      let params = {
        currentPage: this.pageOptions.currentPage,
        pageSize: this.pageOptions.pageSize,
        searchType: this.searchForm.type,
        searchKey: this.searchForm.key,
        sortField: this.searchForm.sortField,
        sortType: this.searchForm.sortType
      }
      getTableData(params).then(res => {
        let data = res.data.data
        this.tableData = data.list
        this.pageOptions.pageSize = data.pageSize
        this.pageOptions.totalCount = Number(data.totalData)
        this.loading = false
      })
    },
    doAdd () {
      // 增加
      const id = 'add'
      const route = {
        name: '${classname}_edit',
        params: {
          id
        }
      }
      this.$router.push(route)
    },
    doReset () {
      //重置表单
      this.$refs['searchForm'].resetFields()
    },
    doDelete(id){
      // 弹框
      this.$Modal.confirm({
        title: '操作确认',
        content: '确认是否删除?',
        closable: true,
        onOk : () => {
          //获得数据
          let params = [id]
          doDelHandler(params).then(res => {
            if(res.data.code === 200){
              this.$Message.info('删除成功')
              this.doSearch()
            } else {
              this.$Message.error(res.data.msg)
            }
          })
        }
      })
    },
    doDelSelected() {
      if (this.selectCount <= 0) {
          this.$Message.warning("您还未选择要删除的数据")
          return
      }
      this.$Modal.confirm({
          title: "确认删除",
          content: "您确认要删除所选的 " + this.selectCount + " 条数据?",
          onOk: () => {
              let ids = []
              this.selectList.forEach(function(e) {
                  ids.push( e.${pk.attrname} );
              })
              // 批量删除
              doDelHandler(ids).then(res => {
                  if(res.data.code === 200){
                  this.$Message.info('删除成功')
                  this.doSearch()
              } else {
                  this.$Message.error(res.data.msg)
              }
            })
          }
      })
    },
    changePage (newPageNum) {
      this.pageOptions.currentPage = newPageNum
    },
    changePageSize (newPageSizeNum) {
      this.pageOptions.pageSize = newPageSizeNum
    },
    changeSort (sort) {
      if(sort.order != 'normal'){
        this.searchForm.sortField = sort.key
        this.searchForm.sortType = sort.order
      }
      this.doSearch()
    },
    clearSelectAll() {
      this.$refs.table.selectAll(false)
    },
    changeSelect(v) {
      this.selectCount = v.length
      this.selectList = v
    },
    showSelect(e) {
      this.selectList = e
      this.selectCount = e.length
    },
  },
  mounted () {
    this.init()
    this.doSearch()
  },
  watch: {
    'pageOptions.currentPage' () {
      // 监控页码，改变则触发查询
      this.doSearch()
    },
    'pageOptions.pageSize' () {
      // 监控每页数量，改变则触发查询
      this.doSearch()
    }
  }
}
</script>

<style>
</style>
