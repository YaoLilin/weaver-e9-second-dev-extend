const {Table, Switch} = antd;

/**
 * 重写 Acton 参数列表 table，自动获取 Action 中定义的参数
 * 当组件加载时，将会调用二开接口，获取 Action 中的所有参数
 */
class ParamTable extends React.Component {
    constructor(props) {
        super(props);
        console.log('props',props)
        this.state = {
            data: props.dataSource ? props.dataSource : []
        }
    }

    componentDidMount() {
        // 组件加载时获取流程 Action 参数
        if (this.props.classPath) {
            this.getActionParams(this.props.classPath).then(params => {
                const data = this.state.data;
                const newData = this.changeParams(params, data);
                this.setState({data: newData});
            })
        }
    }

    /**
     * 修改原 table 组件数据（action 参数），如果从接口获取的 action 参数存在原 table 数据中，则更新数据，否则添加数据
     * @param params        从接口获取的 Action 参数
     * @param originalData  原组件数据
     */
    changeParams(params, originalData) {
        const newData = originalData;
        params.forEach(i => {
            const existParam = originalData.find(d => d.fieldname === i.paramName)
            if (existParam) {
                existParam.required = i.required;
                existParam.desc = i.desc;
                existParam.defaultValue = i.defaultValue;
                return
            }
            originalData.push({
                required: i.required,
                desc: i.desc,
                fieldname: i.paramName,
                id: i.paramName,
                fieldvalue: i.required ? '' : i.defaultValue,
                defaultValue: i.defaultValue,
                isdatasource:'0'
            })
        });
        return newData;
    }

    /**
     * 调用接口获取 Action 参数
     */
    async getActionParams(classPath) {
        const response = await fetch('/api/second-dev/extend/workflow-action-param?actionPath=' + classPath,
            {
                method: 'GET'
            });
        return response.json();
    }

    /**
     * 组件接收新参数时更新组件数据
     */
    componentWillReceiveProps(newProps) {
        console.log('接收新参数')

        if (newProps.dataSource) {
            this.setState({data: newProps.dataSource});
        }
    }


    render() {
        const newCols = [
            {
                title: '是否必填',
                key: 'required',
                dataIndex: 'required',
                width: 60,
                render: (text, record) => {
                    return <Switch checked={text} disabled/>
                }
            },
            {
                title: '描述',
                key: 'desc',
                dataIndex: 'desc',
                width: 150
            },
            {
                title: '默认值',
                key: 'defaultValue',
                dataIndex: 'defaultValue',
                width: 100
            }
        ]

        const columns = [...this.props.columns, ...newCols];

        return (
            <>
                <Table {...this.props} dataSource={this.state.data} columns={columns} _noOverwirite/>
            </>

        )
    }
}

ecodeSDK.setCom('${appId}', 'ParamTable', ParamTable);
