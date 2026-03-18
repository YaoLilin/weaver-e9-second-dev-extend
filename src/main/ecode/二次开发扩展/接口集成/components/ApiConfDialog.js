const {Modal,message} = antd;
const {WeaTools} = ecCom;
const {callApi} = WeaTools;
const ApiConfigure = ecodeSDK.imp(ApiConfigure);

class ApiConfDialog extends React.Component{

    constructor(props){
        super(props);
    }

    removeEmptyNameParam = (params)=>{
        if(params){
            params = params.filter(i => i.name);
            params.forEach(i =>{
                if(i.children){
                    i.children =  this.removeEmptyNameParam(i.children);
                }
            })
        }
        return params;
    }

    removeEmptyNameParams =()=>{
        const {data,onChange} = this.props;
        const newData = {...data};
        const {bodyParameters,queryParameters,headerParameters,returnParameters} = newData;
        newData.bodyParameters = this.removeEmptyNameParam(bodyParameters);
        newData.queryParameters = this.removeEmptyNameParam(queryParameters);
        newData.headerParameters = this.removeEmptyNameParam(headerParameters);
        newData.returnParameters = this.removeEmptyNameParam(returnParameters);
        onChange(newData);
        return newData;
    }

    saveData = (callback)=>{
        const data = this.removeEmptyNameParams();
        const params = {
            url: data.formData.url,
            method: data.formData.method,
            apiId: data.formData.apiId,
            name: data.formData.name,
            bodyType: data.formData.bodyType,
            bodyParameters:data.bodyParameters,
            queryParameters:data.queryParameters,
            headerParameters:data.headerParameters,
            returnParameters:data.returnParameters
        }
        if(!params.url){
            message.error('请填写接口地址');
            callback(false);
            return;
        }
        if(!params.apiId){
            message.error('请填写接口标识');
            callback(false);
            return;
        }
        $.ajax({
            url:'/api/second-dev/extend/apis',
            type:'post',
            data:JSON.stringify(params),
            contentType: "application/json",
            success:(result)=>{
                callback(true);
            },
            error:(xhr, status, error)=>{
                callback(false);
            }
        });
    }

    handleOk = ()=>{
        this.saveData((result) =>{
            if(result){
                message.success('保存成功');
                this.props.onOk();
            }else{
                message.error('保存失败');
            }
        })
    }

    handleCancel = ()=>{
        this.props.onCancel();
    }

    handleChange = (newData)=>{
        const {onChange} = this.props;
        onChange(newData);
    }

    render(){
        const {visible,data} = this.props;
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
                <ApiConfigure data={data} onChange={this.handleChange}/>
            </Modal>
        )
    }
}

ecodeSDK.exp(ApiConfDialog);
