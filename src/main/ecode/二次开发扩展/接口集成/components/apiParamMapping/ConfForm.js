const { Input, Select, message } = antd;
const { Option } = Select;
const {WeaFormItem,WeaForm,WeaInput} = ecCom;
const ModelInfoSelector = ecodeSDK.imp(ModelInfoSelector);
const ApiInfoSelector = ecodeSDK.imp(ApiInfoSelector);
const WorkflowInfoSelector = ecodeSDK.imp(WorkflowInfoSelector);

class ConfForm extends React.Component {
    handleChange = (name, value)=>{
        const {onChange,data} = this.props;
        const newData = {...data}
        newData[name] = value;
        onChange(newData);
    }

    handleSelectWorkflowChange = (value,formId)=>{
        const {onChange,data} = this.props;
        const newData = {...data}
        newData.workflow = value;
        newData.formId = formId;
        onChange(newData);
    }

    handleWorkflowInfoChange = (workflowInfo)=>{
        const info = workflowInfo || {};
        this.handleSelectWorkflowChange({id: info.id || '', name: info.workflowName || ''}, info.formId || '');
    }

    getEmptyMappingData = ()=>{
        return {
            bodyParameters: [],
            queryParameters: [],
            headerParameters: [],
        };
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

    handleSelectApiChange = async (ids, names)=>{
        const {onChange,data} = this.props;
        const newData = {...data};
        newData.api = {id: ids, name: names};

        if (!ids) {
            newData.mappingData = this.getEmptyMappingData();
            newData.bodyType = 0;
            newData.bodyDetailNum = null;
            onChange(newData);
            return;
        }
        try {
            const response = await fetch('/api/second-dev/extend/apis/' + encodeURIComponent(ids), {
                method: 'GET'
            });
            const result = await response.json();
            const conf = result && result.data ? result.data : result;
            if (!conf) {
                message.error('获取接口参数配置失败');
                return;
            }
            const bodyType = conf.bodyType !== undefined && conf.bodyType !== null ? conf.bodyType : 0;
            newData.mappingData = {
                headerParameters: this.normalizeInterfaceParams(conf.headerParameters, false),
                queryParameters: this.normalizeInterfaceParams(conf.queryParameters, false),
                bodyParameters: this.normalizeInterfaceParams(conf.bodyParameters, true),
            };
            newData.bodyType = bodyType;
            if (bodyType !== 1) {
                newData.bodyDetailNum = null;
            }
            onChange(newData);
        } catch (error) {
            message.error('获取接口参数配置失败：' + error.message);
        }
    }

    handleApiInfoChange = (apiInfo)=>{
        const info = apiInfo || {};
        this.handleSelectApiChange(info.id || '', info.name || '');
    }

    handleDataSourceChange = (value)=>{
        const {onChange,data} = this.props;
        const newData = {...data}
        newData.dataSource = value;
        if (value === 'workflow') {
            newData.modelInfo = {};
        } else {
            newData.workflow = {};
            newData.formId = '';
        }
        onChange(newData);
    }

    handleModelInfoChange = (modelInfo)=>{
        const {onChange,data} = this.props;
        const newData = {...data};
        newData.modelInfo = modelInfo;
        onChange(newData);
    }

    render() {
        const {isCreate} = this.props;
        const {confId,name,api,workflow} = this.props.data;
        const dataSource = this.props.data.dataSource || 'workflow';
        const modelInfo = this.props.data.modelInfo || {};
        const modelValue = modelInfo.modeName || '';
        const needModelInfo = dataSource === 'model';
        const isModelEmpty = needModelInfo && !modelInfo.id;

        return(<div style={{maxWidth:600}}>
            <WeaFormItem
                label="唯一标识"
                labelCol={{span: 6}}
                wrapperCol={{span: 18}}
                viewAttr={3}
                style={{padding: '10px 0'}}
            >
                <WeaInput
                    viewAttr={isCreate ? '3' : '1'}
                    value={confId}
                    onChange={(value)=>{this.handleChange('confId',value)}}
                    style={{width: '100%'}}
                />
            </WeaFormItem>
            <WeaFormItem
                label="名称"
                labelCol={{span: 6}}
                wrapperCol={{span: 18}}
                viewAttr={3}
                style={{padding: '10px 0'}}
            >
                <WeaInput
                    viewAttr="3"
                    value={name}
                    onChange={(value)=>{this.handleChange('name',value)}}
                    style={{width: '100%'}}
                />
            </WeaFormItem>
            <WeaFormItem
                label="选择接口"
                labelCol={{span: 6}}
                wrapperCol={{span: 18}}
                viewAttr={3}
                style={{padding: '10px 0'}}
            >
                <ApiInfoSelector
                    value={api && api.name ? api.name : ''}
                    apiInfo={api || {}}
                    onChange={this.handleApiInfoChange}
                />
            </WeaFormItem>
            <WeaFormItem
                label="数据来源"
                labelCol={{span: 6}}
                wrapperCol={{span: 18}}
                viewAttr={3}
                style={{padding: '10px 0'}}
            >
                <Select
                    value={dataSource}
                    onChange={(value)=>{this.handleDataSourceChange(value)}}
                >
                    <Option value="workflow">流程</Option>
                    <Option value="model">建模</Option>
                </Select>
            </WeaFormItem>
        
            {dataSource === 'workflow' && (
                <WeaFormItem
                    label="选择流程"
                    labelCol={{span: 6}}
                    wrapperCol={{span: 18}}
                    viewAttr={3}
                    style={{padding: '10px 0'}}
                >
                    <WorkflowInfoSelector
                        value={workflow && workflow.name ? workflow.name : ''}
                        workflowInfo={workflow || {}}
                        onChange={this.handleWorkflowInfoChange}
                    />
                </WeaFormItem>
            )}
            {dataSource === 'model' && (
                <WeaFormItem
                    label="选择建模"
                    labelCol={{span: 6}}
                    wrapperCol={{span: 18}}
                    viewAttr={3}
                    style={{padding: '10px 0'}}
                >
                    <ModelInfoSelector
                        value={modelValue}
                        modelInfo={modelInfo}
                        onChange={this.handleModelInfoChange}
                    />
                    {isModelEmpty ? (
                        <div style={{ color: '#f5222d', marginTop: 6 }}>请选择建模</div>
                    ) : null}
                </WeaFormItem>
            )}
        
        </div>)
    }
}

ecodeSDK.exp(ConfForm);
