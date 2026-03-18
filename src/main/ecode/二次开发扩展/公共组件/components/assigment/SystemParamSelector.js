
const { Input } = antd;

/**
 * 系统参数选择器组件
 * props: {
 *     systemParams: array,  // 系统参数列表
 *     selectedParamCode: string,  // 当前选中的参数代码
 *     onParamSelect: (paramCode) => {},  // 参数选择回调
 * }
 */
class SystemParamSelector extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            searchText: '',
        }
    }

    handleSearchChange = (e) => {
        this.setState({ searchText: e.target.value });
    }

    render() {
        const { systemParams, selectedParamCode, onParamSelect } = this.props;
        const { searchText } = this.state;

        const keyword = (searchText || '').trim();
        const filteredParams = keyword
            ? systemParams.filter(s => (s.name || '').toLowerCase().includes(keyword.toLowerCase()))
            : systemParams;

        const listItemStyle = (active) => ({
            padding: '8px 10px',
            cursor: 'pointer',
            borderRadius: 4,
            background: active ? '#e6f7ff' : 'transparent',
        });

        const listWrapStyle = {
            border: '1px solid #eee',
            borderRadius: 4,
            maxHeight: 220,
            overflowY: 'auto',
            padding: 6,
        };

        return (
            <div>
                <div style={{ marginBottom: 6 }}>系统参数</div>
                <Input
                    style={{ width: '100%', marginBottom: 8 }}
                    value={searchText}
                    placeholder="搜索系统参数"
                    onChange={this.handleSearchChange}
                />
                <div style={listWrapStyle}>
                    {filteredParams.length === 0 ? (
                        <div style={{ color: '#999', padding: '6px 4px' }}>无匹配系统参数</div>
                    ) : (
                        filteredParams.map(s => (
                            <div
                                key={s.code}
                                style={listItemStyle(s.code === selectedParamCode)}
                                onClick={() => {
                                    if (onParamSelect) {
                                        onParamSelect(s.code);
                                    }
                                }}
                                title={s.name}
                            >
                                {s.name}
                            </div>
                        ))
                    )}
                </div>
            </div>
        );
    }
}

ecodeSDK.exp(SystemParamSelector);
ecodeSDK.setCom('${appId}', 'SystemParamSelector', SystemParamSelector);
