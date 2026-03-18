
const { Input, Select, Button, Table, Switch, Icon, message } = antd;
const { Option } = Select;


/**
 * props: {
 *     data:[],
 *     onChange : (data) => {},
 *     extendColumns?: [] | ((ctx) => []), // 扩展列（可选）。如果传函数，会注入内部操作方法，便于复用。
 *     resetFieldsOnTypeChange?: string[], // 当“参数类型”变化时，需要被重置的字段（可选）
 *     resetFieldValuesOnTypeChange?: object, // 字段重置值映射（可选），例如 { assignment: null, detailTable: '' }
 *     readonly?: boolean, // 只读模式（可选），为 true 时基础列不可编辑，隐藏添加/删除按钮
 * } ,
 * 仅支持配置 JSON 参数结构（名称/显示名称/必需/类型），不包含字段映射能力。
 */
class JsonParametersTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            expandedRowKeys: [],
            selectedRowKeys: [],
        }
    }

    /**
     * 递归收集所有有子节点的参数ID
     * @param {Array} parameters 参数数组
     * @returns {Array} 所有有子节点的参数ID数组
     */
    getAllExpandableIds = (parameters) => {
        const ids = [];
        if (!parameters || !Array.isArray(parameters)) {
            return ids;
        }
        parameters.forEach(param => {
            if (param.children && param.children.length > 0) {
                ids.push(param.id);
                // 递归收集子节点的ID
                ids.push(...this.getAllExpandableIds(param.children));
            }
        });
        return ids;
    }

    componentDidMount() {
        // 初始化时展开所有有子节点的行
        const { data } = this.props;
        const allExpandableIds = this.getAllExpandableIds(data);
        if (allExpandableIds.length > 0) {
            this.setState({ expandedRowKeys: allExpandableIds });
        }
    }

    componentDidUpdate(prevProps) {
        // 当数据变化时，如果数据不同，重新展开所有有子节点的行
        const { data } = this.props;
        if (prevProps.data !== data) {
            const allExpandableIds = this.getAllExpandableIds(data);
            this.setState({ expandedRowKeys: allExpandableIds });
        }
    }

    // 参数类型选项映射
    typeOptions = {
        0: '字符串',
        1: '数字',
        2: '布尔',
        3: '对象',
        4: '数组',
    };

    getColumns = () => {
        const { readonly } = this.props;
        const baseColumns = [
            {
                title: '参数名称',
                dataIndex: 'name',
                key: 'name',
                className: 'param-name',
                render: (text, record) => {
                    if (readonly) {
                        return <div style={{ minHeight: 32, lineHeight: '32px' }}>{text}</div>;
                    }
                    return (
                        <div style={{ maxWidth: 250, display: 'flex', alignItems: 'center' }}>
                            <Input
                                value={text}
                                onChange={(e) => this.handleChange(record.id, 'name', e.target.value)}
                                onBlur={(e) => this.handleBlur(record.id, 'name', e.target.value)}
                                placeholder="参数名称"
                            />
                            <Icon type="plus-circle"
                                  style={{
                                      textAlign: 'left',
                                      marginLeft: '10px',
                                      visibility: record.type === 3 || record.type === 4 ? 'visible' : 'hidden'
                                  }}
                                  onClick={() => {
                                      if (!text) {
                                          message.error('请先填写参数名称');
                                          return;
                                      }
                                      this.handleAddParameter(record.id)
                                  }}
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
                    if (readonly) {
                        return <div style={{ minHeight: 32, lineHeight: '32px' }}>{text}</div>;
                    }
                    return (
                        <Input
                            style={{ maxWidth: 250 }}
                            value={text}
                            onChange={(e) => this.handleChange(record.id, 'showName', e.target.value)}
                            placeholder="显示名称"
                        />
                    );
                },
            },
            {
                title: '必需',
                dataIndex: 'operation',
                key: 'operation',
                render: (text, record) => {
                    return (
                        <div style={{ minHeight: 32, lineHeight: '32px' }}>
                            <Switch
                                checked={record.required}
                                disabled={readonly}
                                onChange={(checked) => {
                                    this.handleChange(record.id, 'required', checked);
                                }}
                            />
                        </div>
                    );
                },
            },
            {
                title: '参数类型',
                dataIndex: 'type',
                key: 'type',
                render: (text, record) => {
                    if (readonly) {
                        return <div style={{ minHeight: 32, lineHeight: '32px' }}>{this.typeOptions[text] || '未知'}</div>;
                    }
                    return (
                        <Select
                            value={text}
                            onChange={(value) => this.handleChange(record.id, 'type', value)}
                            style={{ width: 120 }}
                        >
                            <Option value={0}>字符串</Option>
                            <Option value={1}>数字</Option>
                            <Option value={2}>布尔</Option>
                            <Option value={3}>对象</Option>
                            <Option value={4}>数组</Option>
                        </Select>
                    );
                },
            },
        ];

        let extendColumns = [];
        if (typeof this.props.extendColumns === 'function') {
            extendColumns = this.props.extendColumns({
                handleChange: this.handleChange,
                handleBlur: this.handleBlur,
                props: this.props,
            }) || [];
        } else if (Array.isArray(this.props.extendColumns)) {
            extendColumns = this.props.extendColumns;
        }
        return [...baseColumns, ...extendColumns];
    }

    handleExpand = (expanded, record) => {
        this.setState((prevState) => {
            let updatedExpandedRowKeys = [...prevState.expandedRowKeys];
            if (expanded) {
                if (!updatedExpandedRowKeys.includes(record.id)) {
                    updatedExpandedRowKeys.push(record.id);
                }
            } else {
                updatedExpandedRowKeys = updatedExpandedRowKeys.filter(key => key !== record.id);
            }
            return {expandedRowKeys: updatedExpandedRowKeys};
        });
    };

    handleAddParameter = (parentId) => {
        const newParameter = {
            id: Date.now()+'',
            name: '',
            chineseName: '',
            type: 0, // 设置默认值
            required: false,
            assignmentMethod: '', // 设置默认值
            value: '', // 设置默认值
        };
        const {data,onChange} = this.props;
        const newData = this.addParameter(data, parentId, newParameter);
        const updatedExpandedRowKeys = [...this.state.expandedRowKeys];
        if (parentId !== null && !updatedExpandedRowKeys.includes(parentId)) {
            updatedExpandedRowKeys.push(parentId);
        }
        this.setState({
            expandedRowKeys: updatedExpandedRowKeys,
        });
        onChange(newData);
    };

    handleDeleteParameters = () => {
        const { selectedRowKeys } = this.state;
        const {data,onChange} = this.props;
        let newData = [...data];
        // 递归删除所有选中的参数（包括子参数）
        selectedRowKeys.forEach(id => {
            newData = this.removeParameter(newData, id);
        });
        // 清空选中状态
        this.setState({ selectedRowKeys: [] });
        onChange(newData);
    };

    /**
     * 递归删除指定ID的参数（包括其所有子参数）
     * @param {Array} parameters 参数数组
     * @param {string} id 要删除的参数ID
     * @returns {Array} 删除后的参数数组
     */
    removeParameter = (parameters, id) => {
        return parameters.filter(param => {
            // 如果当前参数就是要删除的参数，直接过滤掉（包括其所有子参数）
            if (param.id === id) {
                return false;
            }
            // 如果有子参数，递归处理子参数
            if (param.children && param.children.length > 0) {
                param.children = this.removeParameter(param.children, id);
            }
            return true;
        });
    };

    addParameter = (parameters, parentId, newParameter) => {
        if (parentId === null) {
            // 如果 parentId 为 null，直接添加到 parameters 数组中
            return [...parameters, newParameter];
        }

        return parameters.map((param) => {
            if (param.id === parentId) {
                // 确保 children 存在
                if (!param.children) {
                    param.children = [];
                }
                return {
                    ...param,
                    children: [...param.children, newParameter],
                };
            }
            if (param.children && param.children.length > 0) {
                return {
                    ...param,
                    children: this.addParameter(param.children, parentId, newParameter),
                };
            }
            return param;
        });
    };

    updateParameter = (parameters, id, field, value) => {
        return parameters.map((param) => {
            if (param.id === id) {
                return {
                    ...param,
                    [field]: value,
                };
            }
            if (param.children && param.children.length > 0) {
                return {
                    ...param,
                    children: this.updateParameter(param.children, id, field, value),
                };
            }
            return param;
        });
    };

    findParameter = (parameters, id) => {
        for (const param of parameters) {
            if (param.id === id) {
                return param;
            }
            if (param.children && param.children.length > 0) {
                const found = this.findParameter(param.children, id);
                if (found) {
                    return found;
                }
            }
        }
        return null;
    };

    removeChildren = (parameters, id) => {
        return parameters.map((param) => {
            if (param.id === id) {
                // 确保 children 存在
                if (!param.children) {
                    param.children = [];
                }
                return {
                    ...param,
                    children: null,
                };
            }
            if (param.children && param.children.length > 0) {
                return {
                    ...param,
                    children: this.removeChildren(param.children, id),
                };
            }
            return param;
        });
    };

    handleChange = (id, field, value) => {
        const {data, onChange} = this.props;
        let newParameters = this.updateParameter(data, id, field, value);

        // 检查是否需要删除子参数
        if (field === 'type') {
            const currentParam = this.findParameter(newParameters, id);
            if (currentParam && (currentParam.type !== 3 && currentParam.type !== 4)) {
                newParameters = this.removeChildren(newParameters, id);
            }
            // 可选：确保在类型变化时，指定字段被重置（例如 mapper 场景：assignmentMethod/value）
            const resetFields = Array.isArray(this.props.resetFieldsOnTypeChange)
                ? this.props.resetFieldsOnTypeChange
                : [];
            const resetValues = this.props.resetFieldValuesOnTypeChange || {};
            if (currentParam && resetFields.length > 0) {
                resetFields.forEach((f) => {
                    const v = Object.prototype.hasOwnProperty.call(resetValues, f) ? resetValues[f] : '';
                    newParameters = this.updateParameter(newParameters, id, f, v);
                });
            }
        }

        onChange(newParameters);
    };

    handleBlur = (id,field, value) => {
        const { data, onChange } = this.props;
        let isDuplicate = false;
        if (field === 'name' && value !== '') {
            if (data.some(param => param.id === id )){
                isDuplicate = data.some(param => param.id !== id && param.name === value);
            }else {
                const sameLevelParams = this.getSameLevelParams(id,data);
                if (sameLevelParams) {
                    isDuplicate = sameLevelParams.some(param => param.id !== id && param.name === value);
                }
            }
        }

        if (isDuplicate) {
            const newParameters = this.updateParameter(data, id, 'name', '');
            onChange(newParameters);
            message.error('参数名称重复，请重新输入');
        }
    };

    getSameLevelParams(id,data){
        let result = null;
        data.forEach(i =>{
            if (result) {
                return;
            }
            if (i.id === id) {
                result = data;
                return;
            }
            if (i.children && i.children.length > 0) {
                const findResult = this.getSameLevelParams(id,i.children);
                if (findResult) {
                    result = findResult;
                }
            }
        });
        return result;
    }

    render() {
        const { expandedRowKeys,selectedRowKeys } = this.state;
        const { data, readonly } = this.props;
        return (
            <div>
                {!readonly && (
                    <div style={{ paddingBottom: 10 }}>
                        <Button type="primary" onClick={() => this.handleAddParameter(null)}>
                            添加参数
                        </Button>
                        <Button type="danger" onClick={() => this.handleDeleteParameters()} disabled={selectedRowKeys.length === 0} style={{ marginLeft: 10 }}>
                            删除参数
                        </Button>
                    </div>
                )}
                <Table
                    className={'a669f59304c6dae317a190d8159a3-table'}
                    columns={this.getColumns()}
                    dataSource={data}
                    pagination={{
                        defaultPageSize: 20,
                        showSizeChanger: true,
                        pageSizeOptions: ['10', '20', '50', '100'],
                        showTotal: (total) => `共 ${total} 条`,
                    }}
                    showHeader={true}
                    rowKey="id"
                    size="small"
                    indentSize={20}
                    childrenColumnName="children"
                    expandedRowKeys={expandedRowKeys}
                    onExpand={this.handleExpand}
                    rowSelection={readonly ? null : {
                        selectedRowKeys,
                        onChange: (selectedRowKeys) => this.setState({ selectedRowKeys }),
                    }}
                />
            </div>
        );
    }
}

ecodeSDK.exp(JsonParametersTable);
ecodeSDK.setCom('${appId}','JsonParametersTable',JsonParametersTable);
