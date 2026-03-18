const { Input, Modal, Table, Button, message } = antd;
const { WeaInputSearch } = ecCom;
const PropTypes = window.PropTypes;

class ModelInfoSelector extends React.Component {
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
                modeName: '',
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
        let url = '/api/second-dev/extend/modes?pageNo=' + pageNo + '&pageSize=' + pageSize;
        if (query.id) {
            url += '&id=' + encodeURIComponent(query.id);
        }
        if (query.modeName) {
            url += '&modeName=' + encodeURIComponent(query.modeName);
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
                message.error('获取建模信息失败：' + (result.message || '未知错误'));
            }
        } catch (error) {
            message.error('获取建模信息失败：' + error.message);
        } finally {
            this.setState({ loading: false });
        }
    }

    openDialog = ()=>{
        const modelInfo = this.props.modelInfo || {};
        const selectedRowKeys = modelInfo.id ? [String(modelInfo.id)] : [];
        this.setState({
            dialogVisible: true,
            selectedRowKeys: selectedRowKeys,
            selectedRow: modelInfo.id ? modelInfo : null
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
            message.error('请选择一条建模数据');
            return;
        }
        if (this.props.onChange) {
            this.props.onChange({
                id: selectedRow.id,
                modeName: selectedRow.modeName,
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
                    placeholder="请选择建模信息"
                    onSearch={this.openDialog}
                    onChange={() => this.setState({ displayValue: this.props.value || '' })}
                    onSearchChange={() => this.setState({ displayValue: this.props.value || '' })}
                />
                <Modal
                    title="选择建模信息"
                    visible={dialogVisible}
                    onOk={this.handleConfirm}
                    onCancel={this.handleCancel}
                    width={720}
                >
                    <div style={{ marginBottom: 16 }}>
                        <div style={{ display: 'flex', gap: 12 }}>
                            <Input
                                placeholder="模块ID"
                                value={query.id}
                                onChange={(e)=>this.handleQueryChange('id', e.target.value)}
                                style={{ width: 120 }}
                            />
                            <Input
                                placeholder="模块名称"
                                value={query.modeName}
                                onChange={(e)=>this.handleQueryChange('modeName', e.target.value)}
                                style={{ width: 200 }}
                            />
                            <Input
                                placeholder="表单名称"
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
                            { title: '模块ID', dataIndex: 'id', key: 'id', width: 100 },
                            { title: '模块名称', dataIndex: 'modeName', key: 'modeName' },
                            { title: '表单名称', dataIndex: 'tableName', key: 'tableName' }
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
    ModelInfoSelector.propTypes = {
        /** 输入框显示值 */
        value: PropTypes.string,
        /** 当前已选建模信息 */
        modelInfo: PropTypes.shape({
            id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
            modeName: PropTypes.string,
            tableName: PropTypes.string
        }),
        /** 选择完成回调 */
        onChange: PropTypes.func
    };
}

ModelInfoSelector.defaultProps = {
    value: '',
    modelInfo: {},
    onChange: null
};

ecodeSDK.exp(ModelInfoSelector);
ecodeSDK.setCom('${appId}', 'ModelInfoSelector', ModelInfoSelector);
