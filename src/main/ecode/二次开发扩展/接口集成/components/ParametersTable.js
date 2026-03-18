
const { Input, Select, Button, Table, Switch, Icon, message } = antd;
const { Option } = Select;
const COMMON_COMPONENT_APP_ID = '812a0239c1e7469eb27e635cb619a2cb';
const PropTypes = window.PropTypes;

/**
 * 参数表格组件，用于在接口参数配置页面中，显示接口参数列表
 * props: {
 *     type?: 'api' | 'mapper',
 *     data?: [],
 *     onChange?: (data) => {},
 *     lockBaseFields?: boolean,
 *     disableAddDelete?: boolean,
 *     workflowId?: number | string,
 *     assignmentData?: {
 *         formFields: { mainFields: [], details: [] },
 *         systemParams: []
 *     }
 * }
 */
class ParametersTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            expandedRowKeys: [],
            selectedRowKeys: [],
            AssignmentCell: null
        }
    }

    buildAssignmentValue = (assignment) => {
        if (!assignment) {
            return { method: 1, value: { workflowField: {}, value: '' } };
        }
        let method = assignment.method;
        const value = assignment.value || {};
        if (method && typeof method === 'object' && method.value !== undefined) {
            method = parseInt(method.value, 10);
        }
        if (method === 1 || method === 2 || method === 3) {
            return Object.assign({}, assignment, { method: method });
        }
        return {
            method: 1,
            value: {
                workflowField: {},
                value: ''
            }
        };
    }

    static defaultProps = {
        type: 'api',
        data: [],
        onChange: null,
        lockBaseFields: false,
        disableAddDelete: false,
        workflowId: null,
        assignmentData: {
            formFields: { mainFields: [], details: [] },
            systemParams: []
        }
    };

    componentDidMount() {
        if (COMMON_COMPONENT_APP_ID) {
            ecodeSDK.load({
                id: COMMON_COMPONENT_APP_ID,
                cb: () => {
                    this.setState({
                        AssignmentCell: ecodeSDK.getCom(COMMON_COMPONENT_APP_ID, 'AssignmentCell')
                    })
                }
            });
        }
    }

    columns = [
        {
            title: '参数名称',
            dataIndex: 'name',
            key: 'name',
            className: 'param-name',
            render: (text, record, index) => {
                if (this.props.lockBaseFields) {
                    return <div style={{ maxWidth: 250, minHeight: 32, lineHeight: '32px' }}>{text}</div>;
                }
                return (
                    <div style={{maxWidth: 250, display: 'flex', alignItems: 'center'}}>
                        <Input
                            value={text}
                            onChange={(e) => this.handleChange(record.id, 'name', e.target.value)}
                            onBlur={(e) => this.handleBlur(record.id, 'name', e.target.value)} // 添加 onBlur 事件处理程序
                            placeholder="参数名称"
                        />
                        <Icon type="plus-circle"
                              style={{
                                  textAlign: 'left',
                                  marginLeft: '10px',
                                  visibility: record.type === 4 || record.type === 3 ? 'visible' : 'hidden'
                              }}
                              onClick={() => this.handleAddParameter(record.id)}
                        />
                    </div>
                );
            },
        },
        {
            title: '显示名称',
            dataIndex: 'showName',
            key: 'showName',
            render: (text, record) => {
                if (this.props.lockBaseFields) {
                    return <div style={{ maxWidth: 250, minHeight: 32, lineHeight: '32px' }}>{text}</div>;
                }
                return (
                    <Input
                        style={{maxWidth: 250}}
                        value={text}
                        onChange={(e) => this.handleChange(record.id, 'showName', e.target.value, 'header')}
                        placeholder="显示名称"
                    />
                );
            },
        },
        {
            title: '必需',
            dataIndex: 'operation',
            key: 'operation',
            render: (text, record) => (
                <div>
                    <Switch
                        checked={record.required}
                        disabled={this.props.lockBaseFields}
                        onChange={(checked) => {
                            this.handleChange(record.id, 'required', checked, 'header');
                        }}
                    />
                </div>
            ),
        }
    ];

    getColumns = () => {
        if(this.props.type === 'mapper'){
            return [...this.columns, 
                {
                    title: '赋值',
                    dataIndex: 'assignment',
                    key: 'assignment',
                    width: 250,
                    render: (text, record) => {
                        const { AssignmentCell } = this.state;
                        if (!AssignmentCell) {
                            return null;
                        }
                        return (
                            <AssignmentCell
                                record={record}
                                value={this.buildAssignmentValue(record.assignment)}
                                data={this.props.assignmentData || { formFields: { mainFields: [], details: [] }, systemParams: [] }}
                                onChangeAssignment={this.handleChange}
                            />
                        );
                    }
                }
            ];
        }
        return this.columns;
    }

    handleChange = (id, field, value) => {
        const { data, onChange } = this.props;
        const newData = JSON.parse(JSON.stringify(data));

        newData.forEach((i, index) => {
            if (i.id === id) {
                i[field] = value;
            }
        });

        onChange(newData);
    };

    handleBlur = (id, field, value) => {
        const { data, onChange } = this.props;
        const newData = JSON.parse(JSON.stringify(data));
        let isDuplicate = false;

        if (field === 'name' && value !== '' && i.name === value && i.id !== id) {
            isDuplicate = true;
        }

        if (isDuplicate) {
            newData.forEach((i, index) => {
                if (i.id === id) {
                    i.name = ''; // 删除重复的参数名
                }
            });
            message.error('参数名称重复，请重新输入');
        }

        onChange(newData);
    };

    handleAddParameter = () => {
        const newParameter = {
            id: Date.now()+'',
            name: '',
            chineseName: '',
            required: false,
            assignment: null
        };
        const {data, onChange} = this.props;
        const newData = [...data];

        newData.push(newParameter);
        onChange(newData);
    };

    handleDeleteParameters=()=>{
        const {selectedRowKeys} = this.state;
        const {data,onChange} = this.props;
        const newData = data.filter(param => !selectedRowKeys.includes(param.id));
        onChange(newData);
    }

    render() {
        const {expandedRowKeys, selectedRowKeys} = this.state;
        const {data, disableAddDelete} = this.props;
        return (
            <div>
                {!disableAddDelete && (
                    <div style={{paddingBottom: 10}}>
                        <Button type="primary" onClick={() => this.handleAddParameter(null)}>
                            添加参数
                        </Button>
                        <Button type="danger" onClick={() => this.handleDeleteParameters()}
                                disabled={selectedRowKeys.length === 0} style={{marginLeft: 10}}>
                            删除参数
                        </Button>
                    </div>
                )}
                <Table
                    className={'a669f59304c6dae317a190d8159a3-table'}
                    columns={this.getColumns()}
                    dataSource={data}
                    pagination={false}
                    showHeader={true}
                    rowKey="id"
                    size="small"
                    indentSize={20}
                    childrenColumnName="children"
                    expandedRowKeys={expandedRowKeys}
                    rowSelection={disableAddDelete ? null : {
                        selectedRowKeys,
                        onChange: (selectedRowKeys) => this.setState({selectedRowKeys}),
                    }}
                />
            </div>
        );
    }
}

if (PropTypes) {
    ParametersTable.propTypes = {
        /** 表格类型(api/mapper) */
        type: PropTypes.string,
        /** 参数列表 */
        data: PropTypes.array,
        /** 数据变更回调 */
        onChange: PropTypes.func,
        /** 基础字段是否只读 */
        lockBaseFields: PropTypes.bool,
        /** 是否禁用新增/删除 */
        disableAddDelete: PropTypes.bool,
        /** 流程ID */
        workflowId: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
        /** 赋值数据(表单字段/系统参数) */
        assignmentData: PropTypes.object
    };
}

ecodeSDK.exp(ParametersTable);
