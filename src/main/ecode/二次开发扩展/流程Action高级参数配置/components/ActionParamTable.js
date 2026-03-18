const { message } = antd;
const COMMON_COMPONENT_APP_ID = '812a0239c1e7469eb27e635cb619a2cb';
const PropTypes = window.PropTypes;

/**
 * Action 参数配置表格
 * props: {
 *     workflowId?: number | string,  // 流程ID
 *     data: [],
 *     onChange: (data) => {},
 *     actionPath?: string,
 * }
 */
class ActionParamTable extends React.Component {
    constructor(props) {
        super(props);
        this.actionId = null;
        this.actionPath = null;
        this.state = {
            ParametersMapperTable: null
        };
    }

    componentDidMount() {
        if (COMMON_COMPONENT_APP_ID) {
            ecodeSDK.load({
                id: COMMON_COMPONENT_APP_ID,
                cb: () => {
                    this.setState({
                        ParametersMapperTable: ecodeSDK.getCom(COMMON_COMPONENT_APP_ID, 'ParametersMapperTable')
                    });
                }
            });
        }
        if (!window.workflowActionConfigStore) {
            return;
        }
        window.workflowActionConfigStore.onActionIdChange = (actionId) => {
            if (this.actionId !== actionId) {
                this.actionId = actionId;
                this.fetchAdvanceParams();
            }
        };
        window.workflowActionConfigStore.onActionPathChange = (actionPath) => {
            if (this.actionPath !== actionPath) {
                this.actionPath = actionPath;
                this.fetchAdvanceParams();
            }
        };
        this.fetchAdvanceParams();
    }

    getActionPath = () => {
        const actionPath = this.props.actionPath
            || (window.workflowActionConfigStore ? window.workflowActionConfigStore.actionPath : null);
        return actionPath || '';
    }

    generateParamId = () => {
        const maxInt = 2147483647;
        const base = Date.now() % maxInt;
        const rand = Math.floor(Math.random() * 1000);
        const id = base + rand;
        return (id >= maxInt ? base : id).toString();
    }

    convertBackendDataToFrontend = (backendData, isRootLevel) => {
        if (!backendData || !Array.isArray(backendData)) {
            return [];
        }
        return backendData.map(item => {
            const converted = {
                id: item.id ? item.id.toString() : this.generateParamId(),
                name: item.name || '',
                showName: item.showName || '',
                type: item.type !== undefined ? (typeof item.type === 'object' ? item.type.value : item.type) : 0,
                required: item.required !== undefined ? (item.required === true || item.required === 1) : false,
                assignment: item.assignment,
                detailTable: item.detailTable !== undefined && item.detailTable !== null ? item.detailTable : null,
                desc: item.desc || ''
            };
            if (isRootLevel && converted.name === 'actionId' && this.actionId) {
                converted.assignment = {
                    method: 3,
                    value: { value: this.actionId }
                };
            }
            if (item.children && Array.isArray(item.children) && item.children.length > 0) {
                converted.children = this.convertBackendDataToFrontend(item.children, false);
            }
            return converted;
        });
    }

    async fetchAdvanceParams() {
        const actionId = this.actionId;
        const workflowId = this.props.workflowId;
        const actionPath = this.getActionPath();
        if (!actionId || !workflowId || !actionPath) {
            return;
        }
        try {
            let url = '/api/second-dev/extend/workflow-action-param/config?actionId=' + encodeURIComponent(actionId);
            if (workflowId) {
                url += '&workflowId=' + workflowId;
            }
            if (actionPath) {
                url += '&actionPath=' + encodeURIComponent(actionPath);
            }
            const response = await fetch(url, { method: 'GET' });
            const result = await response.json();
            if (result.success && result.data) {
                const convertedData = this.convertBackendDataToFrontend(result.data, true);
                if (this.props.onChange && convertedData && convertedData.length > 0) {
                    this.props.onChange(convertedData);
                } else if (this.props.onChange) {
                    this.props.onChange([]);
                }
            } else if (!result.success) {
                message.error('获取高级 Action 参数配置失败：' + (result.message || '未知错误'));
            }
        } catch (error) {
            console.error('获取高级 Action 参数配置失败:', error);
        }
    }

    render() {
        const { data, onChange, workflowId } = this.props;
        const { ParametersMapperTable } = this.state;
        if (!ParametersMapperTable) {
            return null;
        }
        return (
            <ParametersMapperTable
                workflowId={workflowId}
                data={data}
                onChange={onChange}
            />
        );
    }
}

ActionParamTable.defaultProps = {
    workflowId: null,
    data: [],
    onChange: null,
    actionPath: ''
};

if (PropTypes) {
    ActionParamTable.propTypes = {
        /** 流程ID */
        workflowId: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
        /** 参数数据 */
        data: PropTypes.array,
        /** 数据变更回调 */
        onChange: PropTypes.func,
        /** Action 路径 */
        actionPath: PropTypes.string
    };
}

ecodeSDK.exp(ActionParamTable);
ecodeSDK.setCom('${appId}', 'ActionParamTable', ActionParamTable);
