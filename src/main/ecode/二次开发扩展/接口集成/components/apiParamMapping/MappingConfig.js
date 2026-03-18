const { Input, Select, Tabs, message } = antd;
const { Option } = Select;
const { WeaFormItem, WeaForm, WeaInput } = ecCom;
const { TabPane } = Tabs; // 引入 TabPane 组件
const ConfForm = ecodeSDK.imp(ConfForm);
const ParametersTable = ecodeSDK.imp(ParametersTable);
const COMMON_COMPONENT_APP_ID = '812a0239c1e7469eb27e635cb619a2cb';


class MappingConfig extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            formId: '',
            ParametersMapperTable: null,
            assignmentData: {
                formFields: { mainFields: [], details: [] },
                systemParams: []
            }
        }
    }

    componentDidMount() {
        if (COMMON_COMPONENT_APP_ID) {
            ecodeSDK.load({
                id: COMMON_COMPONENT_APP_ID,
                cb: () => {
                    this.setState({
                        ParametersMapperTable: ecodeSDK.getCom(COMMON_COMPONENT_APP_ID, 'ParametersMapperTable')
                    });
                }
            });
        }
        this.refreshAssignmentData(this.props.data);
    }

    // 监听数据来源/流程/建模变化，变化时刷新赋值数据，避免外部更新导致字段不同步
    componentDidUpdate(prevProps) {
        const prevData = prevProps.data || {};
        const currentData = this.props.data || {};
        const prevSource = this.normalizeDataSource(prevData.dataSource);
        const currentSource = this.normalizeDataSource(currentData.dataSource);
        const prevWorkflowId = prevData.workflow && prevData.workflow.id ? prevData.workflow.id : null;
        const currentWorkflowId = currentData.workflow && currentData.workflow.id ? currentData.workflow.id : null;
        const prevModeId = prevData.modelInfo && prevData.modelInfo.id ? prevData.modelInfo.id : null;
        const currentModeId = currentData.modelInfo && currentData.modelInfo.id ? currentData.modelInfo.id : null;

        if (prevSource !== currentSource || prevWorkflowId !== currentWorkflowId || prevModeId !== currentModeId) {
            this.refreshAssignmentData(currentData);
        }
    }

    normalizeDataSource = (value) => {
        if (value === 'model') {
            return 'mode';
        }
        return value || 'workflow';
    }

    resetAssignmentFormFields = () => {
        this.setState(prevState => ({
            assignmentData: {
                formFields: { mainFields: [], details: [] },
                systemParams: prevState.assignmentData.systemParams || []
            }
        }));
    }

    setAssignmentFormFields = (formFields) => {
        this.setState(prevState => ({
            assignmentData: {
                formFields: formFields || { mainFields: [], details: [] },
                systemParams: prevState.assignmentData.systemParams || []
            }
        }));
    }

    setAssignmentSystemParams = (systemParams) => {
        this.setState(prevState => ({
            assignmentData: {
                formFields: prevState.assignmentData.formFields || { mainFields: [], details: [] },
                systemParams: systemParams || []
            }
        }));
    }

    async loadWorkflowFields(workflowId) {
        if (!workflowId || workflowId <= 0) {
            this.resetAssignmentFormFields();
            return;
        }
        try {
            const response = await fetch('/api/second-dev/extend/workflow/fields?workflowId=' + workflowId, {
                method: 'GET'
            });
            const result = await response.json();
            if (result.success && result.data) {
                this.setAssignmentFormFields(result.data);
            } else {
                this.resetAssignmentFormFields();
                message.error('获取流程字段失败：' + (result.message || '未知错误'));
            }
        } catch (error) {
            this.resetAssignmentFormFields();
            message.error('获取流程字段失败：' + error.message);
        }
    }

    async loadModeFields(modeId) {
        if (!modeId || modeId <= 0) {
            this.resetAssignmentFormFields();
            return;
        }
        try {
            const response = await fetch('/api/second-dev/extend/modes/fields?modeId=' + modeId, {
                method: 'GET'
            });
            const result = await response.json();
            if (result.success && result.data) {
                this.setAssignmentFormFields(result.data);
            } else {
                this.resetAssignmentFormFields();
                message.error('获取建模字段失败：' + (result.message || '未知错误'));
            }
        } catch (error) {
            this.resetAssignmentFormFields();
            message.error('获取建模字段失败：' + error.message);
        }
    }

    async loadSystemParams() {
        try {
            const response = await fetch('/api/second-dev/extend/workflow-action-param/system-params', {
                method: 'GET'
            });
            const result = await response.json();
            if (result.success && result.data) {
                this.setAssignmentSystemParams(result.data);
            }
        } catch (error) {
            console.error('获取系统参数失败:', error);
        }
    }

    refreshAssignmentData = (data) => {
        const confData = data || {};
        const dataSource = this.normalizeDataSource(confData.dataSource);
        const workflowId = confData.workflow && confData.workflow.id ? confData.workflow.id : null;
        const modeId = confData.modelInfo && confData.modelInfo.id ? confData.modelInfo.id : null;

        if (dataSource === 'mode') {
            this.setAssignmentSystemParams([]);
            this.loadModeFields(modeId);
        } else {
            this.loadWorkflowFields(workflowId);
            this.loadSystemParams();
        }
    }

    handleHeaderParamChange = (newData) => {
        const { onChange, data } = this.props;
        const newD = JSON.parse(JSON.stringify(data));
        newD.mappingData.headerParameters = newData;
        onChange(newD);
    }

    handleQueryParamChange = (newData) => {
        const { onChange, data } = this.props;
        const newD = JSON.parse(JSON.stringify(data));
        newD.mappingData.queryParameters = newData;
        onChange(newD);
    }

    handleBodyParamChange = (newData) => {
        const { onChange, data } = this.props;
        const newD = JSON.parse(JSON.stringify(data));
        newD.mappingData.bodyParameters = newData;
        onChange(newD);
    }

    handleFormChange = (newData) => {
        const { onChange, data } = this.props;
        const newD = JSON.parse(JSON.stringify(data));
        newD.confId = newData.confId;
        newD.name = newData.name;
        newD.api = newData.api;
        newD.workflow = newData.workflow;
        newD.formId = newData.formId;
        newD.dataSource = newData.dataSource;
        newD.modelInfo = newData.modelInfo;
        newD.bodyType = newData.bodyType;
        if (newData.bodyType !== 1) {
            newD.bodyDetailNum = null;
        }
        if (newData.mappingData) {
            newD.mappingData = newData.mappingData;
        }
        onChange(newD);
        this.setState({ formId: newData.formId })
        this.refreshAssignmentData(newData);
    }

    handleBodyDetailNumChange = (value) => {
        const { onChange, data } = this.props;
        const newD = JSON.parse(JSON.stringify(data));
        newD.bodyDetailNum = value || null;
        onChange(newD);
    }

    renderBodyDetailOptions = () => {
        const { assignmentData } = this.state;
        const details = assignmentData && assignmentData.formFields ? assignmentData.formFields.details : [];
        if (!details || details.length === 0) {
            return [];
        }
        const list = [];
        details.forEach(detail => {
            if (detail && detail.detailNum && detail.fields && detail.fields.length > 0) {
                const value = detail.detailNum;
                list.push(<Option key={value} value={value}>{'明细' + value}</Option>);
            }
        });
        return list;
    }

    render() {
        const { formId, ParametersMapperTable, assignmentData } = this.state;
        const { isCreate } = this.props;
        const { confId, name, api, workflow, dataSource, modelInfo } = this.props.data;
        const { queryParameters, bodyParameters, headerParameters } = this.props.data.mappingData;
        const workflowId = workflow && workflow.id ? workflow.id : null;
        const modeId = modelInfo && modelInfo.id ? modelInfo.id : null;
        const bodyType = this.props.data.bodyType !== undefined && this.props.data.bodyType !== null
            ? this.props.data.bodyType
            : 0;
        const bodyDetailNum = this.props.data.bodyDetailNum || null;

        return (
            <div style={{ overflow: 'scroll', height: '70vh' }}>
                <ConfForm data={this.props.data} isCreate={isCreate} onChange={this.handleFormChange} />
                <Tabs >
                    <TabPane tab="请求头参数" key="1">
                        <ParametersTable type={'mapper'}
                            data={headerParameters}
                            formId={formId}
                            assignmentData={assignmentData}
                            lockBaseFields={true}
                            disableAddDelete={true}
                            onChange={this.handleHeaderParamChange} />
                    </TabPane>
                    <TabPane tab="查询参数" key="2">
                        <ParametersTable type={'mapper'}
                            data={queryParameters}
                            formId={formId}
                            assignmentData={assignmentData}
                            lockBaseFields={true}
                            disableAddDelete={true}
                            onChange={this.handleQueryParamChange} />
                    </TabPane>
                    <TabPane tab="请求体参数" key="3">
                        <div style={{ marginBottom: 12 }}>
                            <span style={{ marginRight: 8 }}>Body 类型：</span>
                            <Select value={bodyType} style={{ width: 160 }} disabled>
                                <Option value={0}>对象</Option>
                                <Option value={1}>数组</Option>
                            </Select>
                            {bodyType === 1 ? (
                                <span style={{ marginLeft: 16 }}>
                                    <span style={{ marginRight: 8 }}>明细表：</span>
                                    <Select
                                        value={bodyDetailNum || undefined}
                                        onChange={this.handleBodyDetailNumChange}
                                        style={{ width: 160 }}
                                        allowClear
                                    >
                                        {this.renderBodyDetailOptions()}
                                    </Select>
                                </span>
                            ) : null}
                        </div>
                        {ParametersMapperTable ? (
                            <ParametersMapperTable
                                data={bodyParameters}
                                dataSource={dataSource}
                                workflowId={workflowId}
                                modeId={modeId}
                                formId={formId}
                                onChange={this.handleBodyParamChange}
                            />
                        ) : null}
                    </TabPane>
                </Tabs>
            </div>
        )
    }
}

ecodeSDK.exp(MappingConfig);
