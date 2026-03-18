const { Input, Modal, Table, Button, message } = antd;
const { WeaInputSearch } = ecCom;
const PropTypes = window.PropTypes;

// 接口选择组件：通过弹窗查询并选择接口信息
class ApiInfoSelector extends React.Component {
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
                name: '',
                url: '',
                apiId: ''
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
        let url = '/api/second-dev/extend/apis?pageNo=' + pageNo + '&pageSize=' + pageSize;
        if (query.name) {
            url += '&name=' + encodeURIComponent(query.name);
        }
        if (query.url) {
            url += '&url=' + encodeURIComponent(query.url);
        }
        if (query.apiId) {
            url += '&apiId=' + encodeURIComponent(query.apiId);
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
            if (result && result.list) {
                this.setState({
                    list: result.list || [],
                    total: result.total || 0,
                    pageNo: result.pageNo || currentPage
                });
            } else if (result && result.success && result.data) {
                this.setState({
                    list: result.data.list || [],
                    total: result.data.total || 0,
                    pageNo: result.data.pageNo || currentPage
                });
            } else {
                message.error('获取接口列表失败：' + (result.message || '未知错误'));
            }
        } catch (error) {
            message.error('获取接口列表失败：' + error.message);
        } finally {
            this.setState({ loading: false });
        }
    }

    openDialog = ()=>{
        const apiInfo = this.props.apiInfo || {};
        const selectedRowKeys = apiInfo.id ? [String(apiInfo.id)] : [];
        this.setState({
            dialogVisible: true,
            selectedRowKeys: selectedRowKeys,
            selectedRow: apiInfo.id ? apiInfo : null
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
            message.error('请选择一条接口数据');
            return;
        }
        if (this.props.onChange) {
            this.props.onChange({
                id: selectedRow.id,
                apiId: selectedRow.apiId,
                name: selectedRow.name,
                url: selectedRow.url,
                method: selectedRow.method
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
                    placeholder="请选择接口"
                    onSearch={this.openDialog}
                    onChange={() => this.setState({ displayValue: this.props.value || '' })}
                    onSearchChange={() => this.setState({ displayValue: this.props.value || '' })}
                />
                <Modal
                    title="选择接口"
                    visible={dialogVisible}
                    onOk={this.handleConfirm}
                    onCancel={this.handleCancel}
                    width={820}
                >
                    <div style={{ marginBottom: 16 }}>
                        <div style={{ display: 'flex', gap: 12 }}>
                            <Input
                                placeholder="接口名称"
                                value={query.name}
                                onChange={(e)=>this.handleQueryChange('name', e.target.value)}
                                style={{ width: 180 }}
                            />
                            <Input
                                placeholder="接口地址"
                                value={query.url}
                                onChange={(e)=>this.handleQueryChange('url', e.target.value)}
                                style={{ width: 240 }}
                            />
                            <Input
                                placeholder="接口标识"
                                value={query.apiId}
                                onChange={(e)=>this.handleQueryChange('apiId', e.target.value)}
                                style={{ width: 160 }}
                            />
                            <Button type="primary" onClick={this.handleSearch}>查询</Button>
                        </div>
                    </div>
                    <Table
                        rowKey="id"
                        columns={[
                            { title: '接口标识', dataIndex: 'apiId', key: 'apiId', width: 160 },
                            { title: '接口名称', dataIndex: 'name', key: 'name', width: 180 },
                            { title: '接口地址', dataIndex: 'url', key: 'url' },
                            { title: '请求方法', dataIndex: 'method', key: 'method', width: 100 }
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
    ApiInfoSelector.propTypes = {
        /** 输入框显示值 */
        value: PropTypes.string,
        /** 当前已选接口信息 */
        apiInfo: PropTypes.shape({
            id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
            apiId: PropTypes.string,
            name: PropTypes.string,
            url: PropTypes.string,
            method: PropTypes.string
        }),
        /** 选择完成回调 */
        onChange: PropTypes.func
    };
}

ApiInfoSelector.defaultProps = {
    value: '',
    apiInfo: {},
    onChange: null
};

ecodeSDK.exp(ApiInfoSelector);
ecodeSDK.setCom('${appId}', 'ApiInfoSelector', ApiInfoSelector);
