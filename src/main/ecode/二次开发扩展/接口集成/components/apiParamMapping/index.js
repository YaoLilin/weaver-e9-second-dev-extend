const {Table, Button, message} = antd;
const MappingDialog = ecodeSDK.imp(MappingDialog);

class MappingConfList extends React.Component{

    constructor(props){
        super(props);
        this.state={
            list: [],
            pageNo: 1,
            pageSize: 10,
            total: 0,
            dialogVisible:false,
            confData : {
                confId: '',
                name: '',
                workflow:{id:'',name:''},
                api:{id:'',name:''},
                modelInfo:{id:'',modeName:'',tableName:''},
                dataSource: 'workflow',
                formId: '',
                bodyType: 0,
                bodyDetailNum: null,
                mappingData:{
                    bodyParameters: [],
                    queryParameters: [],
                    headerParameters: [],
                }
            },
            isCreate : false,
        }
    }

    componentDidMount() {
        this.loadMappingList(1, this.state.pageSize);
    }

    columns = [
        {
            title: '唯一标识',
            dataIndex: 'confId',
        },
        {
            title: '配置名称',
            dataIndex: 'name',
            render: (text,data) =>
                <span style={{color:'rgb(77, 122, 216)',cursor:'pointer'}}
                      onClick={()=>this.handleClickName(data.id)}>{text}</span>,
        },
        {
            title: '数据来源',
            dataIndex: 'resourceType',
            render: (value) => value === 1 ? '建模' : '流程'
        },
        {
            title: '数据来源名称',
            dataIndex: 'resourceTypeName',
        },
        {
            title: '接口名称',
            dataIndex: 'interfaceName',
        },
    ];

    handleClickName = (id)=>{
        this.loadMappingDetail(id);
    }

    handleDialogOk =()=>{
        this.setState({dialogVisible:false}, () => {
            this.loadMappingList(this.state.pageNo, this.state.pageSize);
        });
    }

    handleDialogCancel=()=>{
        this.setState({dialogVisible:false});
    }

    handleClickAdd=()=>{
        this.setState({
            dialogVisible:true,
            isCreate:true,
            confData: {
                confId: '',
                name: '',
                workflow:{id:'',name:''},
                api:{id:'',name:''},
                modelInfo:{id:'',modeName:'',tableName:''},
                dataSource: 'workflow',
                formId: '',
                bodyType: 0,
                bodyDetailNum: null,
                mappingData:{
                    bodyParameters: [],
                    queryParameters: [],
                    headerParameters: [],
                }
            }
        });
    }

    handleConfigChange =(newData)=>{
        this.setState({confData:newData});
    }

    loadMappingList = (pageNo, pageSize)=>{
        const url = '/api/second-dev/extend/apis/mapping?pageNo=' + pageNo + '&pageSize=' + pageSize;
        fetch(url, { method: 'GET' })
            .then(res => res.json())
            .then(result => {
                const data = result && result.success && result.data ? result.data : result;
                const list = data && data.list ? data.list : [];
                this.setState({
                    list: list,
                    total: data.total || 0,
                    pageNo: data.pageNo || pageNo,
                    pageSize: data.pageSize || pageSize
                });
            })
            .catch(() => {
                message.error('获取映射配置列表失败');
            });
    }

    loadMappingDetail = (id)=>{
        if (!id) {
            return;
        }
        const detailUrl = '/api/second-dev/extend/apis/mapping/' + encodeURIComponent(id);
        fetch(detailUrl, { method: 'GET' })
            .then(res => res.json())
            .then(result => {
                const detail = result && result.success && result.data ? result.data : result;
                if (!detail) {
                    message.error('获取配置详情失败');
                    return;
                }
                this.loadInterfaceConf(detail);
            })
            .catch(() => {
                message.error('获取配置详情失败');
            });
    }

    loadInterfaceConf = (detail)=>{
        const interfaceId = detail.interfaceId;
        if (!interfaceId) {
            message.error('接口信息缺失');
            return;
        }
        fetch('/api/second-dev/extend/apis/' + interfaceId, { method: 'GET' })
            .then(res => res.json())
            .then(result => {
                const conf = result && result.data ? result.data : result;
                if (!conf) {
                    message.error('获取接口参数配置失败');
                    return;
                }
                const bodyType = conf.bodyType !== undefined && conf.bodyType !== null ? conf.bodyType : 0;
                const bodyDetailNum = detail.bodyDetailNum !== undefined ? detail.bodyDetailNum : null;
                const mappingData = {
                    headerParameters: this.normalizeInterfaceParams(conf.headerParameters, false),
                    queryParameters: this.normalizeInterfaceParams(conf.queryParameters, false),
                    bodyParameters: this.normalizeInterfaceParams(conf.bodyParameters, true)
                };
                const assignmentMap = this.buildAssignmentMap(detail.mappings || []);
                this.applyAssignments(mappingData.headerParameters, assignmentMap);
                this.applyAssignments(mappingData.queryParameters, assignmentMap);
                this.applyAssignments(mappingData.bodyParameters, assignmentMap);

                const dataSource = detail.resourceType === 1 ? 'model' : 'workflow';
                this.setState({
                    dialogVisible: true,
                    isCreate: false,
                    confData: {
                        confId: detail.confId || '',
                        name: detail.name || '',
                        workflow: {id: detail.workflowId || '', name: detail.workflowName || ''},
                        api: {id: detail.interfaceId || '', name: detail.interfaceName || ''},
                        modelInfo: {id: detail.modeId || '', modeName: detail.modeName || '', tableName: detail.tableName || ''},
                        dataSource: dataSource,
                        formId: detail.workflowFormId || '',
                        bodyType: bodyType,
                        bodyDetailNum: bodyType === 1 ? bodyDetailNum : null,
                        mappingData: mappingData
                    }
                });
            })
            .catch(() => {
                message.error('获取接口参数配置失败');
            });
    }

    normalizeInterfaceParams = (parameters, allowChildren)=>{
        if (!parameters || !Array.isArray(parameters)) {
            return [];
        }
        return parameters.map((param) => {
            const id = param.id !== undefined && param.id !== null ? String(param.id) : (Date.now() + Math.random()) + '';
            const type = param.type !== undefined && param.type !== null ? parseInt(param.type, 10) : 0;
            const children = allowChildren ? this.normalizeInterfaceParams(param.children, allowChildren) : [];
            return {
                id: id,
                name: param.name || '',
                showName: param.showName || '',
                required: param.required === 1 || param.required === true,
                type: isNaN(type) ? 0 : type,
                children: children && children.length > 0 ? children : undefined,
            };
        });
    }

    buildAssignmentMap = (mappings)=>{
        const map = {};
        mappings.forEach(item => {
            if (item.paramId) {
                map[item.paramId] = {
                    assignment: item.assignment || null,
                    detailNum: item.detailNum
                };
            }
        });
        return map;
    }

    applyAssignments = (params, assignmentMap)=>{
        if (!params || params.length === 0) {
            return;
        }
        params.forEach(param => {
            if (assignmentMap[param.id]) {
                const mapping = assignmentMap[param.id];
                if (mapping.assignment) {
                    param.assignment = mapping.assignment;
                }
                if (mapping.detailNum !== undefined && mapping.detailNum !== null && mapping.detailNum !== 0) {
                    param.detailNum = mapping.detailNum;
                }
            }
            if (param.children && param.children.length > 0) {
                this.applyAssignments(param.children, assignmentMap);
            }
        });
    }

    render(){
        const {dialogVisible,confData,isCreate,list,pageNo,pageSize,total} = this.state;

        return (
            <div style={{padding:20}}>
                <div style={{marginBottom: 10, textAlign: 'right'}}>
                    <Button type={'primary'} onClick={()=>{
                        this.handleClickAdd();
                    }}>添加</Button>
                </div>
                <Table
                    columns={this.columns}
                    dataSource={list}
                    pagination={{
                        current: pageNo,
                        pageSize: pageSize,
                        total: total,
                        showSizeChanger: true,
                        onChange: (page, size) => this.loadMappingList(page, size),
                        onShowSizeChange: (page, size) => this.loadMappingList(page, size)
                    }}
                    rowKey="confId"
                />
                <MappingDialog visible={dialogVisible}
                                       data = {confData}
                                       isCreate={isCreate}
                                       onOk={this.handleDialogOk}
                                       onChange={this.handleConfigChange}
                                       onCancel={this.handleDialogCancel}/>
            </div>

        )
    }
}

ecodeSDK.setCom('${appId}','MappingConfList',MappingConfList);
