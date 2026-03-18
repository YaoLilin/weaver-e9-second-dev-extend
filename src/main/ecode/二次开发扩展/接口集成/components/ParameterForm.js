
const { Input, Select  } = antd;
const {WeaFormItem,WeaForm,WeaInput} = ecCom;

class ParameterForm extends React.Component {

    handleApiUrlChange = (v)=>{
        const {data,onChange} = this.props;
        const newData = {...data};
        newData.url = v;
        onChange(newData);
    }

    handleApiIdChange = (v)=>{
        const {data,onChange} = this.props;
        const newData = {...data};
        newData.apiId = v;
        onChange(newData);
    }

    handleMethodChange = (v)=>{
        const {data,onChange} = this.props;
        const newData = {...data};
        newData.method = v;
        onChange(newData);
    }

    handleNameChange = (v)=>{
        const {data,onChange} = this.props;
        const newData = {...data};
        newData.name = v;
        onChange(newData);
    }

    render() {
        const {data : {apiId,name,url,method},onChange} = this.props;
        const methodValue = method !== undefined && method !== null ? Number(method) : undefined;

        return (
            <div style={{maxWidth:600}}>
                <WeaFormItem
                    label="接口标识"
                    labelCol={{span: 6}}
                    wrapperCol={{span: 18}}
                    viewAttr={3}
                    style={{padding:'10px 0'}}
                >
                    <WeaInput
                        viewAttr="3"
                        value={apiId}
                        onChange={this.handleApiIdChange}
                        style={{ width: '100%' }}
                    />
                </WeaFormItem>
                <WeaFormItem
                    label="接口名称"
                    labelCol={{span: 6}}
                    wrapperCol={{span: 18}}
                    viewAttr={3}
                    style={{padding:'10px 0'}}
                >
                    <WeaInput
                        viewAttr="3"
                        value={name}
                        onChange={this.handleNameChange}
                        style={{ width: '100%' }}
                    />
                </WeaFormItem>
                <WeaFormItem
                    label="接口地址"
                    labelCol={{span: 6}}
                    wrapperCol={{span: 18}}
                    viewAttr={3}
                    style={{padding:'10px 0'}}
                >
                    <WeaInput
                        viewAttr="3"
                        value={url}
                        onChange={this.handleApiUrlChange}
                        placeholder="请输入接口地址"
                        style={{ width: '100%' }}
                    />
                </WeaFormItem>
                <WeaFormItem
                    label="请求方法"
                    labelCol={{span: 6}}
                    wrapperCol={{span: 18}}
                    viewAttr={3}
                    style={{padding:'10px 0'}}
                >
                    <Select
                        style={{width:200}}
                        value={methodValue}
                        onChange={this.handleMethodChange}
                        placeholder="请求方法"
                    >
                        <Option value={0}>GET</Option>
                        <Option value={1}>POST</Option>
                        <Option value={2}>PUT</Option>
                        <Option value={3}>DELETE</Option>
                    </Select>
                </WeaFormItem>
            </div>
        );
    }
}

ecodeSDK.exp(ParameterForm);
