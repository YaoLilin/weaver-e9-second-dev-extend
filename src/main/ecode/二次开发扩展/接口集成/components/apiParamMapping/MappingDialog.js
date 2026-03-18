

const {Modal,message} = antd;
const {WeaTools} = ecCom;
const {callApi} = WeaTools;
const MappingConfig = ecodeSDK.imp(MappingConfig);

class MappingDialog extends React.Component{

    constructor(props){
        super(props);
    }

    handleOk = ()=>{
        const { data, onOk } = this.props;
        const payload = this.buildSavePayload(data);
        if (!payload) {
            return;
        }
        const url = '/api/second-dev/extend/apis/mapping';
        const loading = message.loading('正在保存配置...', 0);
        fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        }).then(res => res.json())
            .then(result => {
                loading();
                if (!result || !result.success) {
                    message.error('保存配置失败：' + ((result && result.message) || '未知错误'));
                    return;
                }
                if (onOk) {
                    onOk();
                }
            })
            .catch(error => {
                loading();
                message.error('保存配置失败：' + (error.message || '未知错误'));
            });
    }

    handleCancel = ()=>{
        this.props.onCancel();
    }

    handleChange = (newData)=>{
        const {onChange} = this.props;
        onChange(newData);
    }

    buildSavePayload = (data)=>{
        if (!data) {
            message.error('配置数据不能为空');
            return null;
        }
        const mappingData = data.mappingData || {};
        const apiId = data.api && data.api.id ? data.api.id : null;
        if (!apiId) {
            message.error('请选择接口');
            return null;
        }
        const dataSource = data.dataSource === 'model' ? 1 : 0;
        return {
            confId: data.confId || '',
            name: data.name || '',
            apiId: apiId,
            dataSource: dataSource,
            workflow: data.workflow && data.workflow.id ? data.workflow.id : null,
            modeId: data.modelInfo && data.modelInfo.id ? data.modelInfo.id : null,
            bodyDetailNum: data.bodyDetailNum || null,
            headerParameters: this.convertParams(mappingData.headerParameters || []),
            queryParameters: this.convertParams(mappingData.queryParameters || []),
            bodyParameters: this.convertParams(mappingData.bodyParameters || [])
        };
    }

    convertParams = (params)=>{
        if (!params || !Array.isArray(params)) {
            return [];
        }
        return params.map(item => {
            const normalizedAssignment = this.normalizeAssignment(item.assignment);
            const result = {
                name: item.name || '',
                paramId: item.id || '',
                assignment: normalizedAssignment,
                detailNum: this.normalizeDetailNum(item.detailNum)
            };
            if (item.children && item.children.length > 0) {
                result.children = this.convertParams(item.children);
            }
            return result;
        });
    }

    normalizeAssignment = (assignment)=>{
        if (!assignment) {
            return null;
        }
        let method = assignment.method;
        if (method === undefined || method === null) {
            return assignment;
        }
        if (method && typeof method === 'object' && method.value !== undefined) {
            method = parseInt(method.value, 10);
            return Object.assign({}, assignment, { method: method });
        }
        if (typeof method === 'number') {
            return assignment;
        }
        if (typeof method === 'string' && /^\d+$/.test(method)) {
            return Object.assign({}, assignment, { method: parseInt(method, 10) });
        }
        return null;
    }

    normalizeDetailNum = (detailTable)=>{
        if (detailTable === undefined || detailTable === null || detailTable === '') {
            return null;
        }
        const value = parseInt(detailTable, 10);
        return isNaN(value) ? null : value;
    }
 
    render(){
        const {visible,data,isCreate} = this.props;
        const footer =[
            <Button onClick={this.handleCancel}>取消</Button>,
            <Button type={'primary'} onClick={this.handleOk}>保存</Button>
        ]

        return (
            <Modal title="接口配置" width={1000} visible={visible} 
                   footer={footer}
                   maskClosable={false}
                   onOk={this.handleOk} onCancel={this.handleCancel}
            >
                <MappingConfig data={data} isCreate={isCreate} onChange ={this.handleChange} />
            </Modal>
        )
    }
}

ecodeSDK.exp(MappingDialog);
