
const { Input, Select, Button, message } = antd;
const { WeaHelpfulTip } = ecCom;
const { Option } = Select;
const JsonParametersTable = ecodeSDK.imp(JsonParametersTable);
const COMMON_COMPONENT_APP_ID = '812a0239c1e7469eb27e635cb619a2cb';
const PropTypes = window.PropTypes;


/**
 * props: {
 *     formId?: 1,
 *     workflowId?: 1,  // 流程ID
 *     modeId?: 1,  // 建模ID（可选，如果未传入则从 URL 获取）
 *     dataSource?: 'workflow' | 'mode',  // 字段来源
 *     data: [],
 *     onChange: (data) => {},
 * }
 * 支持配置 JSON 参数结构，并支持字段映射（赋值方式/取值）。
 */
class ParametersMapperTable extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            JsonParametersTable: null,
            AssignmentCell: null,
            // 字段数据相关
            formFieldCategories: [],
            formFieldsByCategory: {},
            loadingFields: false,
            detailTableOptions: [],
            systemParams: [],
        }
    }

    static defaultProps = {
        formId: null,
        workflowId: null,
        modeId: null,
        dataSource: 'workflow',
        data: [],
        onChange: null,
    };

    generateParamId = () => {
        const maxInt = 2147483647;
        const base = Date.now() % maxInt;
        const rand = Math.floor(Math.random() * 1000);
        const id = base + rand;
        return (id >= maxInt ? base : id).toString();
    }

    buildAssignmentValue = (assignment) => {
        if (!assignment) {
            return { method: 1, value: { workflowField: {}, value: '' } };
        }
        const method = assignment.method;
        const value = assignment.value || {};
        if (method === 1 || method === 2 || method === 3) {
            return assignment;
        }
        const methodMap = {
            FORM_FIELD: 1,
            SYSTEM_PARAM: 2,
            FIXED_VALUE: 3
        };
        return {
            method: methodMap[method] || 1,
            value: {
                workflowField: value.workflowField || {},
                value: value.value || ''
            }
        };
    }

    buildAssignmentData = () => {
        const { formFieldsByCategory, systemParams } = this.state;
        const mainFields = formFieldsByCategory.MAIN || [];
        const details = Object.keys(formFieldsByCategory)
            .filter(key => key.indexOf('DT') === 0)
            .map(key => {
                const num = parseInt(key.substring(2), 10);
                return { detailNum: num, fields: formFieldsByCategory[key] || [] };
            })
            .filter(item => !isNaN(item.detailNum));
        return {
            formFields: {
                mainFields: mainFields,
                details: details
            },
            systemParams: systemParams || []
        };
    }


    /**
     * 调用接口获取流程字段信息
     */
    async loadWorkflowFields(workflowId) {
        if (!workflowId || workflowId <= 0) {
            return;
        }
        this.setState({ loadingFields: true });
        try {
            const response = await fetch('/api/second-dev/extend/workflow/fields?workflowId=' + workflowId, {
                method: 'GET'
            });
            const result = await response.json();
            if (result.success && result.data) {
                const fields = result.data;
                // 根据 detailTable 字段分类
                this.categorizeFields(fields);
            } else {
                message.error('获取流程字段失败：' + (result.message || '未知错误'));
            }
        } catch (error) {
            message.error('获取流程字段失败：' + error.message);
        } finally {
            this.setState({ loadingFields: false });
        }
    }

    /**
     * 调用接口获取建模字段信息
     */
    async loadModeFields(modeId) {
        if (!modeId || modeId <= 0) {
            return;
        }
        this.setState({ loadingFields: true });
        try {
            const response = await fetch('/api/second-dev/extend/modes/fields?modeId=' + modeId, {
                method: 'GET'
            });
            const result = await response.json();
            if (result.success && result.data) {
                const fields = result.data;
                this.categorizeFields(fields);
            } else {
                message.error('获取建模字段失败：' + (result.message || '未知错误'));
            }
        } catch (error) {
            message.error('获取建模字段失败：' + error.message);
        } finally {
            this.setState({ loadingFields: false });
        }
    }

    /**
     * 调用接口获取系统参数列表
     */
    async loadSystemParams() {
        try {
            const response = await fetch('/api/second-dev/extend/workflow-action-param/system-params', {
                method: 'GET'
            });
            const result = await response.json();
            if (result.success && result.data) {
                this.setState({ systemParams: result.data });
            }
        } catch (error) {
            console.error('获取系统参数失败:', error);
        }
    }

    /**
     * 根据后端返回的数据结构对流程字段进行分类
     * 后端返回：{ mainFields: [], details: [{ detailNum: 1, fields: [] }, ...] }
     */
    categorizeFields(data) {
        const categories = [];
        const fieldsByCategory = {};
        const detailTableOptions = [];

        // 主表字段
        if (data.mainFields && data.mainFields.length > 0) {
            categories.push({ value: 'MAIN', label: '主表字段' });
            fieldsByCategory['MAIN'] = data.mainFields.map(f => ({
                id: f.fieldName,
                name: f.labelName,
                fieldName: f.fieldName,
                labelName: f.labelName,
                fieldType: f.fieldType,
            }));
        }

        // 明细表字段
        if (data.details && data.details.length > 0) {
            data.details.forEach(detail => {
                if (detail.detailNum && detail.fields && detail.fields.length > 0) {
                    const categoryValue = 'DT' + detail.detailNum;
                    categories.push({ value: categoryValue, label: '明细表' + detail.detailNum });
                    fieldsByCategory[categoryValue] = detail.fields.map(f => ({
                        id: f.fieldName,
                        name: f.labelName,
                        fieldName: f.fieldName,
                        labelName: f.labelName,
                        fieldType: f.fieldType,
                        detailTableNum: detail.detailNum
                    }));
                    // 生成明细表选项
                    detailTableOptions.push({ value: detail.detailNum, label: '明细表' + detail.detailNum });
                }
            });
        }

        this.setState({
            formFieldCategories: categories,
            formFieldsByCategory: fieldsByCategory,
            detailTableOptions: detailTableOptions
        });
    }

    componentDidMount() {
        ecodeSDK.load({
            id: '494a669f59304c6dae317a190d8159a3',
            cb: () => {
                this.setState({
                    JsonParametersTable: ecodeSDK.getCom('494a669f59304c6dae317a190d8159a3', 'JsonParametersTable')
                })
            }
        });
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

        const dataSource = this.props.dataSource;
        if (dataSource === 'mode') {
            const modeId = this.props.modeId;
            if (modeId && modeId > 0) {
                this.loadModeFields(modeId);
            }
        } else {
            const workflowId = this.props.workflowId;
            if (workflowId && workflowId > 0) {
                this.loadWorkflowFields(workflowId);
            }
        }
        // 加载系统参数
        this.loadSystemParams();

    }

    render() {
        const { data, onChange } = this.props;
        const { JsonParametersTable, AssignmentCell, loadingFields } = this.state;
        return (
            <div>
                {JsonParametersTable ?
                    <JsonParametersTable
                        data={data}
                        onChange={onChange}
                        readonly={true}
                        resetFieldValuesOnTypeChange={{ assignment: null, detailTable: '' }}
                        extendColumns={({ handleChange }) => {
                            return [
                                {
                                    title: '说明',
                                    dataIndex: 'desc',
                                    key: 'desc',
                                    width: 50,
                                    render: (text) => {
                                        if (!text) {
                                            return null;
                                        }
                                        return (
                                            <WeaHelpfulTip
                                                title={text}
                                                placement="top"
                                                width={260}
                                            />
                                        );
                                    }
                                },
                                {
                                    title: '赋值',
                                    dataIndex: 'assignment',
                                    key: 'assignment',
                                    width: 250,
                                    render: (text, record) => {
                                        if (!AssignmentCell) {
                                            return null;
                                        }
                                        return (
                                            <AssignmentCell
                                                record={record}
                                                value={this.buildAssignmentValue(record.assignment)}
                                                data={this.buildAssignmentData()}
                                                loadingFields={loadingFields}
                                                onChangeAssignment={handleChange}
                                            />
                                        );
                                    }
                                },
                                {
                                    title: (
                                        <span style={{ display: 'inline-flex', alignItems: 'center' }}>
                                        明细表
                                        <span style={{ marginLeft: 4 }}>
                                            <WeaHelpfulTip
                                                title="如果数组类型参数的子参数需要获取明细表字段，则该数组参数需要选择明细表"
                                                placement="top"
                                                width={260}
                                            />
                                        </span>
                                    </span>
                                    ),
                                    dataIndex: 'detailTable',
                                    key: 'detailTable',
                                    render: (text, record) => {
                                        // 仅当参数类型为数组(4)时显示
                                        if (record.type !== 4) {
                                            return null;
                                        }
                                        return (
                                            <Select
                                                style={{ width: 100 }}
                                                value={text || undefined}
                                                placeholder="请选择"
                                                allowClear
                                                onChange={(v) => handleChange(record.id, 'detailTable', v || null)}
                                            >
                                                {this.state.detailTableOptions.map(o => (
                                                    <Option key={o.value} value={o.value}>{o.label}</Option>
                                                ))}
                                            </Select>
                                        );
                                    }
                                },
                            ];
                        }}
                    />
                    : null
                }
            </div>

        );
    }
}

if (PropTypes) {
    ParametersMapperTable.propTypes = {
        /** 表单ID */
        formId: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
        /** 流程ID */
        workflowId: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
        /** 建模ID */
        modeId: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
        /** 字段来源 */
        dataSource: PropTypes.oneOf(['workflow', 'mode']),
        /** 参数数据 */
        data: PropTypes.array,
        /** 数据变更回调 */
        onChange: PropTypes.func,
    };
}

ecodeSDK.exp(ParametersMapperTable);
ecodeSDK.setCom('${appId}', 'ParametersMapperTable', ParametersMapperTable);


