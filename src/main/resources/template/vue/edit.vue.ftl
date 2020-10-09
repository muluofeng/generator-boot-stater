<template>
    <div>
        <Card>
            <p slot="title">
                ID：{{dataForm.${pk.attrname}}}
            </p>
            <span slot="extra">
                &nbsp;<Button size="small" @click="doCancel">返回</Button>
            </span>
            <Form ref="formInline" :model="dataForm" :label-width="120" @submit.native.prevent>
<#list columns as column>
<#if column.columnName != pk.columnName>
                <Row>
                    <Col span="24">
                        <FormItem label="${column.comments}">
                            <Input v-model="dataForm.${column.attrname}" placeholder="${column.comments}" style="width:240px;" />
                        </FormItem>
                    </Col>
                </Row>
</#if>
</#list>
                <Row>
                    <Col span="24">
                        <FormItem style="text-align:center;margin-top:30px;margin-bottom:30px;">
                            <Button type="primary" @click="doSave">保存</Button>
                            <Button style="margin-left: 40px" @click="doCancel">取消</Button>
                        </FormItem>
                    </Col>
                </Row>
            </Form>
        </Card>
    </div>
</template>

<script>
import moment from 'moment'
import { mapMutations } from 'vuex'
<#if openFrontLowercase >
import { getDataById, save } from '@/api/${pathName}'
<#else>
import { getDataById, save } from '@/api/${classname}'
</#if>
import sysparam from '@/libs/sysparam'

export default {
    name: '${classname}_edit',
    data () {
        return {
            loading: false,
            dataForm: {
                ${pk.attrname} : null,
            }
        }
    },
    filters: {
        formatDate (time){
            if(time){
                return moment(time).format('YYYY-MM-DD HH:mm:ss')
            }
            return ''
        }
    },
    methods: {
        ...mapMutations([
            'closeTag'
        ]),
        doSearch (id = this.$route.params.id) {

            if(id !== 'add'){
                this.loading = true
                // 根据id 查询
                getDataById(id).then(res => {
                    this.dataForm = res.data.data
                })
            }
        },
        doSave () {
            save(this.dataForm).then(res => {
                if (res.data.code  === 200) {
                    this.$Message.success('保存成功')
                    this.doCancel()
                } else {
                    this.$Message.error(res.data.msg)
                }
            })
        },
        doCancel () {
            this.closeTag({
                name:  '${classname}_edit',
                params: {
                    id: this.$route.params.id
                }
            })
        }
    },
    mounted () {
        this.doSearch()
    },
    // 守卫导航, 监听路由变化
    beforeRouteEnter(to, from, next) {
        next(vm => {
            //因为当钩子执行前，组件实例还没被创建 vm 就是当前组件的实例相当于上面的 this，所以在 next 方法里你就可以把 vm 当 this
            console.debug('打开新编辑页')
            vm.doSearch()
        });
    },
    beforeRouteUpdate(to, from, next)  {
        console.debug('切换编辑页:' + to.params.id)
        this.doSearch(to.params.id)
        next()
    },
    beforeRouteLeave(to, from, next) {
        next();
    }
}
</script>
