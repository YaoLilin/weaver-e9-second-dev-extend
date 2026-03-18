const { Input, Modal, Table, Button, message } = antd;
const { WeaInputSearch } = ecCom;
const PropTypes = window.PropTypes;

// 流程选择组件：通过弹窗查询并选择流程信息
class WorkflowInfoSelector extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            displayValue: props.value || '',
            dialogVisible: false,
            loading: false,
            list: [],
            pageNo: 1,
            pageSize: 10,
            total: 0,
            query: {
                id: '',
                workflowName: '',
                tableName: ''
            },
            selectedRowKeys: [],
            selectedRow: null
        };
    }

    componentDidUpdate(prevProps) {
        if (prevProps.value !== this.props.value) {
            this.setState({ displayValue: this.props.value || '' });
        }
    }

    handleQueryChange = (field, value)=>{
        const { query } = this.state;
        this.setState({
            query: {
                ...query,
                [field]: value
            }
        });
    }

    buildQueryUrl = (pageNo, pageSize)=>{
        const { query } = this.state;
        let url = '/api/second-dev/extend/workflows?pageNo=' + pageNo + '&pageSize=' + pageSize;
        if (query.id) {
            url += '&id=' + encodeURIComponent(query.id);
        }
        if (query.workflowName) {
            url += '&workflowName=' + encodeURIComponent(query.workflowName);
        }
        if (query.tableName) {
            url += '&tableName=' + encodeURIComponent(query.tableName);
        }
        return url;
    }

    fetchList = async (pageNo)=>{
        const currentPage = pageNo || 1;
        const { pageSize } = this.state;
        this.setState({ loading: true });
        try {
            const url = this.buildQueryUrl(currentPage, pageSize);
            const response = await fetch(url, { method: 'GET' });
            const result = await response.json();
            if (result && result.success && result.data) {
                this.setState({
                    list: result.data.list || [],
                    total: result.data.total || 0,
                    pageNo: result.data.pageNo || currentPage
                });
            } else {
                message.error('获取流程信息失败：' + (result.message || '未知错误'));
            }
        } catch (error) {
            message.error('获取流程信息失败：' + error.message);
        } finally {
            this.setState({ loading: false });
        }
    }

    openDialog = ()=>{
        const workflowInfo = this.props.workflowInfo || {};
        const selectedRowKeys = workflowInfo.id ? [String(workflowInfo.id)] : [];
        this.setState({
            dialogVisible: true,
            selectedRowKeys: selectedRowKeys,
            selectedRow: workflowInfo.id ? workflowInfo : null
        }, () => {
            this.fetchList(1);
        });
    }

    handleSearch = ()=>{
        this.fetchList(1);
    }

    handleSelectChange = (selectedRowKeys, selectedRows)=>{
        this.setState({
            selectedRowKeys: selectedRowKeys,
            selectedRow: selectedRows && selectedRows.length > 0 ? selectedRows[0] : null
        });
    }

    handleConfirm = ()=>{
        const { selectedRow } = this.state;
        if (!selectedRow) {
            message.error('请选择一条流程数据');
            return;
        }
        if (this.props.onChange) {
            this.props.onChange({
                id: selectedRow.id,
                workflowName: selectedRow.workflowName,
                typeName: selectedRow.typeName,
                version: selectedRow.version,
                formId: selectedRow.formId,
                tableName: selectedRow.tableName
            });
        }
        this.setState({ dialogVisible: false });
    }

    handleCancel = ()=>{
        this.setState({ dialogVisible: false });
    }

    render() {
        const {
            displayValue,
            dialogVisible,
            loading,
            list,
            pageNo,
            pageSize,
            total,
            query,
            selectedRowKeys
        } = this.state;
        return (
            <span>
                <WeaInputSearch
                    value={displayValue}
                    placeholder="请选择流程"
                    onSearch={this.openDialog}
                    onChange={() => this.setState({ displayValue: this.props.value || '' })}
                    onSearchChange={() => this.setState({ displayValue: this.props.value || '' })}
                />
                <Modal
                    title="选择流程"
                    visible={dialogVisible}
                    onOk={this.handleConfirm}
                    onCancel={this.handleCancel}
                    width={860}
                >
                    <div style={{ marginBottom: 16 }}>
                        <div style={{ display: 'flex', gap: 12 }}>
                            <Input
                                placeholder="流程ID"
                                value={query.id}
                                onChange={(e)=>this.handleQueryChange('id', e.target.value)}
                                style={{ width: 140 }}
                            />
                            <Input
                                placeholder="流程名称"
                                value={query.workflowName}
                                onChange={(e)=>this.handleQueryChange('workflowName', e.target.value)}
                                style={{ width: 200 }}
                            />
                            <Input
                                placeholder="流程表名"
                                value={query.tableName}
                                onChange={(e)=>this.handleQueryChange('tableName', e.target.value)}
                                style={{ width: 200 }}
                            />
                            <Button type="primary" onClick={this.handleSearch}>查询</Button>
                        </div>
                    </div>
                    <Table
                        rowKey="id"
                        columns={[
                            { title: '流程ID', dataIndex: 'id', key: 'id', width: 90 },
                            { title: '流程名称', dataIndex: 'workflowName', key: 'workflowName', width: 180 },
                            { title: '流程类型', dataIndex: 'typeName', key: 'typeName', width: 140 },
                            { title: '版本', dataIndex: 'version', key: 'version', width: 80 },
                            { title: '表单ID', dataIndex: 'formId', key: 'formId', width: 90 },
                            { title: '流程表名', dataIndex: 'tableName', key: 'tableName' }
                        ]}
                        dataSource={list}
                        loading={loading}
                        pagination={{
                            current: pageNo,
                            pageSize: pageSize,
                            total: total,
                            showSizeChanger: true,
                            pageSizeOptions: ['10', '20', '50'],
                            onChange: (page) => this.fetchList(page),
                            onShowSizeChange: (page, size) => {
                                this.setState({ pageSize: size }, () => {
                                    this.fetchList(1);
                                });
                            }
                        }}
                        rowSelection={{
                            type: 'radio',
                            selectedRowKeys: selectedRowKeys,
                            onChange: this.handleSelectChange
                        }}
                    />
                </Modal>
            </span>
        );
    }
}

if (PropTypes) {
    WorkflowInfoSelector.propTypes = {
        /** 输入框显示值 */
        value: PropTypes.string,
        /** 当前已选流程信息 */
        workflowInfo: PropTypes.shape({
            id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
            workflowName: PropTypes.string,
            typeName: PropTypes.string,
            version: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
            formId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
            tableName: PropTypes.string
        }),
        /** 选择完成回调 */
        onChange: PropTypes.func
    };
}

WorkflowInfoSelector.defaultProps = {
    value: '',
    workflowInfo: {},
    onChange: null
};

ecodeSDK.exp(WorkflowInfoSelector);
ecodeSDK.setCom('${appId}', 'WorkflowInfoSelector', WorkflowInfoSelector);
