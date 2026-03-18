const { Input, Select, Button, Table, Switch, Icon, Tabs } = antd;
const {TabPane} = Tabs;
const {WeaFormItem} = ecCom;
const { Option } = Select;
const ParametersTable = ecodeSDK.imp(ParametersTable);
const JsonParametersTable = ecodeSDK.imp(JsonParametersTable);
const ParameterForm = ecodeSDK.imp(ParameterForm);

class ApiConfigure extends React.Component {
    constructor(props) {
        super(props);
    }

    handleQueryParamChange = (newData) =>{
        const {onChange,data} = this.props;
        const newConfData = {...data};
        newConfData.queryParameters = newData;
        onChange(newConfData);
    }

    handleHeaderParamChange = (newData) =>{
        const {onChange,data} = this.props;
        const newConfData = {...data};
        newConfData.headerParameters = newData;
        onChange(newConfData);
    }

    handleBodyParamChange = (newData) =>{
        const {onChange,data} = this.props;
        const newConfData = {...data};
        newConfData.bodyParameters = newData;
        onChange(newConfData);
    }

    handleReturnParamChange = (newData) =>{
        const {onChange,data} = this.props;
        const newConfData = {...data};
        newConfData.returnParameters = newData;
        onChange(newConfData);
    }

    handleFormDataChange = (newData) =>{
        const {onChange,data} = this.props;
        const newConfData = {...data};
        newConfData.formData = newData;
        onChange(newConfData);
    }

    handleBodyTypeChange = (value) => {
        const { onChange, data } = this.props;
        const newConfData = { ...data };
        newConfData.formData = { ...(newConfData.formData || {}), bodyType: value };
        onChange(newConfData);
    }

    render() {
        const {  bodyParameters, queryParameters,
            headerParameters,returnParameters, formData} = this.props.data;
        const bodyType = formData && formData.bodyType !== undefined && formData.bodyType !== null
            ? formData.bodyType
            : 0;
        const {onChange} = this.props;

        return (
            <div style={{ padding: 20 ,height:700,overflow:'scroll'}}>
                <ParameterForm
                    data={formData}
                    onChange={this.handleFormDataChange}
                />
                <Tabs defaultActiveKey="1">
                    <TabPane tab="查询参数" key="1">
                        <ParametersTable
                            data={queryParameters}
                            onChange={this.handleQueryParamChange}
                        />
                    </TabPane>
                    <TabPane tab="请求头" key="2">
                        <ParametersTable
                            data={headerParameters}
                            onChange={this.handleHeaderParamChange}
                        />
                    </TabPane>
                    <TabPane tab="请求体" key="3">
                        <div style={{ marginBottom: 12 }}>
                            <span style={{ marginRight: 8 }}>Body 类型：</span>
                            <Select
                                value={bodyType}
                                onChange={this.handleBodyTypeChange}
                                style={{ width: 160 }}
                            >
                                <Option value={0}>对象</Option>
                                <Option value={1}>数组</Option>
                            </Select>
                        </div>
                        <JsonParametersTable
                            data={bodyParameters}
                            onChange={this.handleBodyParamChange}
                        />
                    </TabPane>
                    <TabPane tab="返回参数" key="4">
                        <JsonParametersTable
                            data={returnParameters}
                            onChange={this.handleReturnParamChange}
                        />
                    </TabPane>
                </Tabs>
            </div>
        );
    }
}

ecodeSDK.exp(ApiConfigure);

// 确保 ecodeSDK.setCom 方法正确调用
ecodeSDK.setCom('${appId}', 'ParameterMapping', ApiConfigure);
