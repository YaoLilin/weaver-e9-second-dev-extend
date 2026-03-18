

const {Table,Button,message} = antd;
const {WeaTools} = ecCom;
const {callApi} = WeaTools;
const ApiConfDialog = ecodeSDK.imp(ApiConfDialog);

class ApiList extends React.Component {

    constructor(props) {
        super(props);
        this.state={
            data: [],
            total: 0,
            pageNo: 1,
            pageSize: 10,
            dialogVisible:false,
            interfaceData:{
                bodyParameters: [],
                queryParameters: [],
                headerParameters: [],
                returnParameters:[],
                formData:{
                    url: '',
                    method: 0,
                    apiId:'',
                    name:'',
                    bodyType: 0
                }
            },
            isCreate:false
        }
    }

    cleanInterfaceData = ()=>{
        this.setState({
            interfaceData:{
                bodyParameters: [],
                queryParameters: [],
                headerParameters: [],
                returnParameters:[],
                formData:{
                    url: '',
                    method: 0,
                    apiId:'',
                    name:'',
                    bodyType: 0
                }
            }
        })
    }

    componentDidMount(){
        this.loadInterfaceList(1, this.state.pageSize);
    }

    loadInterfaceList = (pageNo, pageSize) => {
        const query = '?pageNo=' + pageNo + '&pageSize=' + pageSize;
        callApi('/api/second-dev/extend/apis' + query,'GET').then(result =>{
            const list = result && result.list ? result.list : (Array.isArray(result) ? result : []);
            const total = result && result.total !== undefined ? result.total : list.length;
            const currentPage = result && result.pageNo ? result.pageNo : pageNo;
            const currentSize = result && result.pageSize ? result.pageSize : pageSize;
            this.setState({
                data: list,
                total: total,
                pageNo: currentPage,
                pageSize: currentSize
            });
        }).catch(e =>{
            message.error('获取列表失败');
        })
    }

    handleClickName = (id)=>{
        callApi('/api/second-dev/extend/apis/'+id,'GET').then(result =>{
            const interfaceData = {
                formData:{
                    url : result.url,
                    method : result.method,
                    apiId : result.apiId,
                    name : result.name,
                    bodyType: result.bodyType !== undefined && result.bodyType !== null ? result.bodyType : 0
                },
                bodyParameters :result.bodyParameters,
                queryParameters : result.queryParameters,
                headerParameters : result.headerParameters,
                returnParameters : result.returnParameters
            }
            this.setState({interfaceData,dialogVisible:true,isCreate:false});
        }).catch(e =>{
            message.error('获取接口信息失败');
        })
    }

    handleDialogChange =(newData)=>{
        this.setState({interfaceData:newData});
    }

    columns = [
        {
            title: '接口名称',
            dataIndex: 'name',
            render: (text,data) =>
                <span style={{color:'rgb(77, 122, 216)',cursor:'pointer'}}
                      onClick={()=>this.handleClickName(data.id)}>{text}</span>,
        },
        {
            title: '接口地址',
            dataIndex: 'url',
        },
        {
            title: '请求方法',
            dataIndex: 'method',
        },
    ];

    render() {
        const {data,dialogVisible,interfaceData,isCreate,total,pageNo,pageSize} = this.state;

        return (
            <div style={{padding:20}}>
                <div style={{marginBottom: 10, textAlign: 'right'}}>
                    <Button type={'primary'} onClick={()=>{
                        this.cleanInterfaceData();
                        this.setState({dialogVisible:true,isCreate:true});
                    }}>添加</Button>
                </div>
                <Table
                    columns={this.columns}
                    dataSource={data}
                    pagination={{
                        current: pageNo,
                        pageSize: pageSize,
                        total: total,
                        showSizeChanger: true,
                        onChange: (page, size) => this.loadInterfaceList(page, size),
                        onShowSizeChange: (page, size) => this.loadInterfaceList(page, size)
                    }}
                />
                <ApiConfDialog visible={dialogVisible}
                               data={interfaceData}
                               isCreate={isCreate}
                               onChange={this.handleDialogChange}
                               onOk={()=>this.setState({dialogVisible:false})}
                               onCancel={()=>this.setState({dialogVisible:false})}/>
            </div>
        );
    }
}

ecodeSDK.setCom('${appId}','ApiList',ApiList);
