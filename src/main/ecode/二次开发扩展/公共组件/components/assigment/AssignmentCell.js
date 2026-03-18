const { Button } = antd;
const { Tag } = antd;
const AssignmentDialog = ecodeSDK.imp(AssignmentDialog);
const PropTypes = window.PropTypes;

/**
 * 接口参数赋值组件，用于在接口参数配置页面中，为接口参数赋值
 *
 * @example
 * <AssignmentCell
 *     record={{ id: '1', type: 0, children: [] }}
 *     value={{
 *         method: 1,
 *         value: {
 *             workflowField: {
 *                 isMainTable: true,
 *                 detailTableNum: 1,
 *                 fieldName: 'lx',
 *                 displayName: '日期(rq)'
 *             },
 *             value: ''
 *         }
 *     }}
 *     data={{
 *         formFields: {
 *             mainFields: [{ fieldName: 'lx', labelName: '类型', fieldType: '输入框' }],
 *             details: [{ detailNum: 1, fields: [{ fieldName: 'lx', labelName: '类型', fieldType: '输入框' }] }]
 *         },
 *         systemParams: [{ code: 'CREATOR', name: '流程创建人' }]
 *     }}
 *     loadingFields={false}
 *     onChangeAssignment={(id, field, value) => {}}
 * />
 */
class AssignmentCell extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            modalVisible: false
        };
    }

    /**
     * @typedef {Object} AssignmentCellProps
     * @property {Object} record 当前行记录
     * @property {Object} value 赋值数据
     * @property {Object} data 赋值所需数据
     * @property {boolean} loadingFields 字段加载状态
     * @property {Function|null} onChangeAssignment 赋值结果回写方法
     */
    openModal = () => {
        this.setState({
            modalVisible: true
        });
    }

    closeModal = () => {
        this.setState({
            modalVisible: false
        });
    }

    handleOk = (assignment) => {
        const { record, onChangeAssignment } = this.props;
        if (onChangeAssignment) {
            onChangeAssignment(record.id, 'assignment', assignment);
        }
        this.closeModal();
    }

    buildDisplay = () => {
        const { value, data } = this.props;
        const systemParams = (data && data.systemParams) ? data.systemParams : [];
        let display = '';
        let displayNode = '';
        if (value) {
            if (value.display) {
                display = value.display;
                displayNode = display;
            } else if (value.method && value.value) {
                const method = value.method;
                const detailValue = value.value;
                if (method === 1 && detailValue.workflowField) {
                    const workflowField = detailValue.workflowField;
                    const nameText = workflowField.displayName || workflowField.fieldName || '';
                    let tagLabel = '';
                    if (workflowField.isMainTable === true) {
                        tagLabel = '主表';
                    } else if (workflowField.detailTableNum) {
                        tagLabel = '明细' + workflowField.detailTableNum;
                    }
                    display = (tagLabel + (nameText ? ' ' + nameText : '')).trim();
                    displayNode = (
                        <span>
                            {tagLabel ? <Tag>{tagLabel}</Tag> : null}
                            {nameText}
                        </span>
                    );
                } else if (method === 2 && detailValue.value) {
                    const found = (systemParams || []).find(s => s.code === detailValue.value);
                    const paramName = found ? found.name : detailValue.value;
                    display = '系统参数：' + paramName;
                    displayNode = display;
                } else if (method === 3 && detailValue.value) {
                    display = '固定值：' + detailValue.value;
                    displayNode = display;
                }
            }
        }
        return { display, displayNode };
    }

    render() {
        const { record, loadingFields, value, data } = this.props;
        const { modalVisible } = this.state;
        if (record.type === 3) {
            return null;
        }
        if (record.type === 4 && record.children && record.children.length > 0) {
            return null;
        }
        const displayInfo = this.buildDisplay();
        return (
            <div style={{ display: 'flex', alignItems: 'center' }}>
                <Button size="small" onClick={this.openModal}>赋值</Button>
                <div
                    style={{ marginLeft: 8, flex: 1, minWidth: 0, whiteSpace: 'normal', wordBreak: 'break-all' }}
                    title={displayInfo.display}
                >
                    {displayInfo.displayNode || displayInfo.display}
                </div>
                <AssignmentDialog
                    visible={modalVisible}
                    value={value}
                    data={data}
                    loadingFields={loadingFields}
                    onOk={this.handleOk}
                    onCancel={this.closeModal}
                />
            </div>
        );
    }
}

if (PropTypes) {
    AssignmentCell.propTypes = {
        /** 当前行记录 */
        record: PropTypes.object,
        /** 赋值数据 */
        value: PropTypes.object,
        /** 赋值所需数据 */
        data: PropTypes.object,
        /** 字段加载状态 */
        loadingFields: PropTypes.bool,
        /** 赋值结果回写方法 */
        onChangeAssignment: PropTypes.func
    };
}

AssignmentCell.defaultProps = {
    record: {},
    value: null,
    data: {},
    loadingFields: false,
    onChangeAssignment: null
};

// 应用id：812a0239c1e7469eb27e635cb619a2cb
ecodeSDK.exp(AssignmentCell);
ecodeSDK.setCom('${appId}', 'AssignmentCell', AssignmentCell);
