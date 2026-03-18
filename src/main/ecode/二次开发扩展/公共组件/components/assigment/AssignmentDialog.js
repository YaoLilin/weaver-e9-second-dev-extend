
const { Input, Button, Modal, message, Tag } = antd;
const FormFieldSelector = ecodeSDK.imp(FormFieldSelector);
const SystemParamSelector = ecodeSDK.imp(SystemParamSelector);

/**
 * 赋值对话框组件
 * props: {
 *     visible: boolean,  // 是否显示对话框
 *     value: object,  // 赋值数据 { method, value }
 *     data: object,  // 赋值所需数据 { formFields, systemParams }
 *     loadingFields: boolean,  // 是否正在加载字段数据
 *     onOk: (assignment) => {},  // 确定回调
 *     onCancel: () => {},  // 取消回调
 * }
 * 
 */
class AssignmentDialog extends React.Component {
    constructor(props) {
        super(props);
        this.state = this.buildStateFromValue(this.props.value);
    }

    componentDidUpdate(prevProps) {
        // 当 visible 变为 true 时，初始化状态并加载系统参数
        if (this.props.visible && !prevProps.visible) {
            this.setState(this.buildStateFromValue(this.props.value));
        } else if (this.props.visible && prevProps.value !== this.props.value) {
            this.setState(this.buildStateFromValue(this.props.value));
        }
    }

    buildStateFromValue = (assignmentValue) => {
        const method = assignmentValue && assignmentValue.method ? assignmentValue.method : 1;
        const detailValue = assignmentValue && assignmentValue.value ? assignmentValue.value : {};
        const workflowField = detailValue.workflowField || {};
        return {
            selectedMethod: method,
            value: {
                workflowField: {
                    isMainTable: workflowField.isMainTable !== undefined ? workflowField.isMainTable : true,
                    detailTableNum: workflowField.detailTableNum || null,
                    fieldName: workflowField.fieldName || '',
                    displayName: workflowField.displayName || ''
                },
                fixedValue: method === 3 ? (detailValue.value || '') : '',
                selectedSystemParamCode: method === 2 ? (detailValue.value || '') : ''
            }
        };
    }

    setSelectedMethod = (method) => {
        // 切换赋值方式时，不清空当前选择
        this.setState({
            selectedMethod: method
        });
    }

    getCurrentAssignmentDisplay = () => {
        const { selectedMethod, value } = this.state;
        const { data = {} } = this.props;
        const formFields = data.formFields || {};
        const systemParams = data.systemParams || [];
        if (selectedMethod === 1) {
            const workflowField = value.workflowField || {};
            const fields = workflowField.isMainTable
                ? (formFields.mainFields || [])
                : ((formFields.details || []).find(d => d.detailNum === workflowField.detailTableNum) || {}).fields || [];
            const found = fields.find(f => f.fieldName === workflowField.fieldName);
            const nameText = found ? (found.labelName ? found.labelName + '(' + found.fieldName + ')' : found.fieldName) : (workflowField.displayName || workflowField.fieldName);
            if (nameText) {
                const tagLabel = workflowField.isMainTable ? '主表' : (workflowField.detailTableNum ? '明细' + workflowField.detailTableNum : '');
                return (
                    <span style={{ display: 'inline-flex', alignItems: 'center' }}>
                        {tagLabel ? <Tag style={{ marginRight: 6 }}>{tagLabel}</Tag> : null}
                        <span>{nameText}</span>
                    </span>
                );
            }
            return '';
        }
        if (selectedMethod === 2) {
            const found = systemParams.find(s => s.code === value.selectedSystemParamCode);
            return found ? '系统参数：' + found.name : '';
        }
        if (selectedMethod === 3) {
            return value.fixedValue ? '固定值：' + value.fixedValue : '';
        }
        return '';
    }

    handleOk = () => {
        const { selectedMethod, value } = this.state;
        const { onOk, data = {} } = this.props;
        const formFields = data.formFields || {};
        const systemParams = data.systemParams || [];
        let assignment = null;
        if (selectedMethod === 1) {
            const workflowField = value.workflowField || {};
            if (!workflowField.fieldName) {
                if (onOk) {
                    onOk(null);
                }
                return;
            }
            const fields = workflowField.isMainTable
                ? (formFields.mainFields || [])
                : ((formFields.details || []).find(d => d.detailNum === workflowField.detailTableNum) || {}).fields || [];
            const found = fields.find(f => f.fieldName === workflowField.fieldName);
            const displayName = found ? (found.labelName ? found.labelName + '(' + found.fieldName + ')' : found.fieldName)
                : (workflowField.displayName || workflowField.fieldName);
            assignment = {
                method: 1,
                value: {
                    workflowField: {
                        isMainTable: workflowField.isMainTable === true,
                        detailTableNum: workflowField.isMainTable ? null : workflowField.detailTableNum,
                        fieldName: workflowField.fieldName,
                        displayName: displayName
                    },
                    value: ''
                }
            };
        } else if (selectedMethod === 2) {
            if (!value.selectedSystemParamCode) {
                if (onOk) {
                    onOk(null);
                }
                return;
            }
            const found = systemParams.find(s => s.code === value.selectedSystemParamCode);
            if (!found) {
                message.error('请选择有效的系统参数');
                return;
            }
            assignment = {
                method: 2,
                value: {
                    value: found.code
                }
            };
        } else if (selectedMethod === 3) {
            if (!value.fixedValue) {
                if (onOk) {
                    onOk(null);
                }
                return;
            }
            assignment = {
                method: 3,
                value: {
                    value: value.fixedValue
                }
            };
        } else {
            message.error('请选择赋值方式');
            return;
        }

        if (onOk) {
            onOk(assignment);
        }
    }

    handleClear = () => {
        const { selectedMethod, value } = this.state;
        const currentWorkflowField = value.workflowField || {
            isMainTable: true,
            detailTableNum: null,
            fieldName: '',
            displayName: ''
        };
        const nextValue = {
            workflowField: currentWorkflowField,
            fixedValue: value.fixedValue || '',
            selectedSystemParamCode: value.selectedSystemParamCode || ''
        };
        if (selectedMethod === 1) {
            nextValue.workflowField = {
                ...currentWorkflowField,
                fieldName: '',
                displayName: ''
            };
        } else if (selectedMethod === 2) {
            nextValue.selectedSystemParamCode = '';
        } else if (selectedMethod === 3) {
            nextValue.fixedValue = '';
        }
        this.setState({ value: nextValue });
    }

    renderAssignModalRight = () => {
        const {
            selectedMethod,
            value,
        } = this.state;
        const {
            data = {},
            loadingFields = false,
        } = this.props;
        const formFields = data.formFields || {};
        const systemParams = data.systemParams || [];

        if (selectedMethod === 1) {
            const workflowField = value.workflowField || {};
            const selectorData = {
                mainFields: formFields.mainFields || [],
                details: formFields.details || [],
                isMainTable: workflowField.isMainTable === true,
                detailTableNum: workflowField.detailTableNum || null
            };
            const formFieldSelectorValue = workflowField || {};

            return (
                <FormFieldSelector
                    data={selectorData}
                    value={formFieldSelectorValue}
                    loadingFields={loadingFields}
                    onChange={(v) => {
                        this.setState({
                            value: {
                                ...value,
                                workflowField: v
                            }
                        });
                    }}
                />
            );
        }
        if (selectedMethod === 2) {
            return (
                <SystemParamSelector
                    systemParams={systemParams}
                    selectedParamCode={value.selectedSystemParamCode}
                    loading={false}
                    onParamSelect={(paramCode) => {
                        this.setState({
                            value: {
                                ...value,
                                selectedSystemParamCode: paramCode,
                            }
                        });
                    }}
                />
            );
        }
        if (selectedMethod === 3) {
            return (
                <div>
                    <div style={{ marginBottom: 6 }}>固定值</div>
                    <Input
                        style={{ width: '100%' }}
                        value={value.fixedValue}
                        placeholder="请输入固定值"
                        onChange={(e) => {
                            this.setState({
                                value: {
                                    ...value,
                                    fixedValue: e.target.value,
                                }
                            });
                        }}
                    />
                </div>
            );
        }
        return null;
    }

    render() {
        const { visible, onCancel } = this.props;
        const { selectedMethod } = this.state;
        const currentDisplay = this.getCurrentAssignmentDisplay();

        return (
            <Modal
                title="赋值"
                visible={visible}
                onOk={this.handleOk}
                onCancel={onCancel}
                width={720}
                footer={[
                    <Button key="clear" onClick={this.handleClear}>清除</Button>,
                    <Button key="cancel" onClick={onCancel}>取消</Button>,
                    <Button key="ok" type="primary" onClick={this.handleOk}>确定</Button>
                ]}
            >
                <div style={{ marginBottom: 12 }}>
                    当前选择：{currentDisplay || '未选择'}
                </div>
                <div style={{ display: 'flex', minHeight: 280 }}>
                    <div style={{ width: 160, borderRight: '1px solid #eee', paddingRight: 12 }}>
                        <div style={{ marginBottom: 8, color: '#888' }}>赋值方式</div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                            <Button
                                type={selectedMethod === 1 ? 'primary' : 'default'}
                                onClick={() => this.setSelectedMethod(1)}
                            >
                                表单字段
                            </Button>
                            <Button
                                type={selectedMethod === 2 ? 'primary' : 'default'}
                                onClick={() => this.setSelectedMethod(2)}
                            >
                                系统参数
                            </Button>
                            <Button
                                type={selectedMethod === 3 ? 'primary' : 'default'}
                                onClick={() => this.setSelectedMethod(3)}
                            >
                                固定值
                            </Button>
                        </div>
                    </div>
                    <div style={{ flex: 1, paddingLeft: 16, minWidth: 0 }}>
                        {this.renderAssignModalRight()}
                    </div>
                </div>
            </Modal>
        );
    }
}

ecodeSDK.exp(AssignmentDialog);
ecodeSDK.setCom('${appId}', 'AssignmentDialog', AssignmentDialog);
