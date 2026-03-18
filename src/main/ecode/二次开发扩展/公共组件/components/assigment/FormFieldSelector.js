
const { Input, Select, Table } = antd;
const { Option } = Select;

/**
 * 表单字段选择器组件
 * props: {
 *     categories: array,  // 字段分类选项 [{ categoryName: '主表字段', id: 'MAIN' }, ...]
 *     fields: array,  // 当前选中分类的字段列表
 *     selectedCategory: string,  // 当前选中的分类ID
 *     selectedFieldId: string,  // 当前选中的字段ID
 *     loadingFields: boolean,  // 是否正在加载字段
 *     onCategoryChange: (categoryId) => {},  // 分类变化回调
 *     onFieldSelect: (fieldId) => {},  // 字段选择回调
 * }
 */
class FormFieldSelector extends React.Component {
    constructor(props) {
        super(props);
        let selectedCategory = 0;
        if(props.data){
            if(props.data.isMainTable){
                selectedCategory = 0;
            }else if(props.data.detailTableNum){
                selectedCategory = props.data.detailTableNum;
            }
        }
        this.state = {
            searchText: '',
            selectedCategory: selectedCategory,
        }
    }

    handleSearchChange = (e) => {
        this.setState({ searchText: e.target.value });
    }

    getOptions = () => {
        const { data } = this.props;
        if (!data) {
            return [];
        }
        const options = [];
        if (data.mainFields) {
            options.push(<Option key={0} value={0}>主表</Option>);
        }
        if (data.details) {
            data.details.forEach(detail => {
                options.push(<Option key={detail.detailNum} value={detail.detailNum}>{`明细表${detail.detailNum}`}</Option>);
            });
        }
        return options;
    }

    getTableData = () => {
        const { data } = this.props;
        if (!data) {
            return [];
        }
        const isMainTable = data.isMainTable;
        const detailTableNum = data.detailTableNum;
        if (isMainTable) {
            return data.mainFields || [];
        }
        if (detailTableNum) {
            const detail = (data.details || []).find(d => d.detailNum === detailTableNum);
            return detail ? detail.fields || [] : [];
        }
        return [];

    }

    handleCategoryChange = (v) => {
        this.setState({ searchText: '' ,selectedCategory: v});
        const {onChange} = this.props;
        const newData = {...this.props.data};
        newData.isMainTable = v === 0;
        newData.detailTableNum = v === 0 ? null : v;
        onChange(newData);
    }

    render() {
        const { loadingFields, onChange } = this.props;
        const { searchText, selectedCategory } = this.state;
        const selectedFieldId = this.props.value ? this.props.value.fieldName : '';

        const fields = this.getTableData();
        const keyword = (searchText || '').trim();
        const filteredFields = keyword
            ? fields.filter(f =>
                (f.labelName || '').toLowerCase().includes(keyword.toLowerCase()) ||
                (f.fieldName || '').toLowerCase().includes(keyword.toLowerCase())
            )
            : fields;

        // Table 列定义
        const columns = [
            {
                title: '字段中文名',
                dataIndex: 'labelName',
                key: 'labelName',
                width: 120,
            },
            {
                title: '字段数据库名',
                dataIndex: 'fieldName',
                key: 'fieldName',
                width: 150,
            },
            {
                title: '字段类型',
                dataIndex: 'fieldType',
                key: 'fieldType',
                width: 100,
            },
        ];

        // Table 行选择配置
        const rowSelection = {
            type: 'radio',
            selectedRowKeys: selectedFieldId ? [selectedFieldId] : [],
            onSelect: (record, selected) => {
                if (selected && onChange) {
                    const newValue = {...this.props.value};
                    newValue.fieldName = record.fieldName;
                    this.props.onChange(newValue);
                }
            },
        };

        return (
            <div>
                <div style={{ marginBottom: 12 }}>
                    <div style={{ marginBottom: 6 }}>分类</div>
                    <Select
                        style={{ width: '100%' }}
                        value={selectedCategory}
                        onChange={this.handleCategoryChange}
                    >
                        {this.getOptions()}
                    </Select>
                </div>
                <div>
                    <div style={{ marginBottom: 6 }}>字段</div>
                    <Input
                        style={{ width: '100%', marginBottom: 8 }}
                        value={searchText}
                        placeholder="搜索字段（支持中文名和数据库名）"
                        onChange={this.handleSearchChange}
                    />
                    {loadingFields ? (
                        <div style={{ color: '#999', padding: '6px 4px' }}>加载中...</div>
                    ) : filteredFields.length === 0 ? (
                        <div style={{ color: '#999', padding: '6px 4px' }}>无匹配字段</div>
                    ) : (
                        <Table
                            columns={columns}
                            dataSource={filteredFields}
                            rowKey="fieldName"
                            size="small"
                            pagination={false}
                            rowSelection={rowSelection}
                            style={{ width: '100%' }}
                            scroll={{ y: 200 }}
                        />
                    )}
                </div>
            </div>
        );
    }
}

ecodeSDK.exp(FormFieldSelector);
ecodeSDK.setCom('${appId}', 'FormFieldSelector', FormFieldSelector);
