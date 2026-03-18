const { Input, Select, Button, Table, Switch, Icon, message } = antd;
const { Option } = Select;
const ParamValue = ecodeSDK.imp(ParamValue);
const JsonParametersTable = ecodeSDK.imp(JsonParametersTable);

/**
 * props: {
 *     formId?: 1,
 *     data: [],
 *     onChange: (data) => {},
 * }
 * 支持配置 JSON 参数结构，并支持字段映射（赋值方式/取值）。
 */
class JsonParametersMapperTable extends React.Component {
    render() {
        const { data, onChange, formId, readonly } = this.props;
        return (
            <JsonParametersTable
                data={data}
                onChange={onChange}
                readonly={readonly}
                resetFieldsOnTypeChange={['assignmentMethod', 'value']}
                extendColumns={({ handleChange }) => ([
                    {
                        title: '赋值方式',
                        dataIndex: 'assignmentMethod',
                        key: 'assignmentMethod',
                        render: (text, record) => (
                            record.type !== 3 && record.type !== 4 ? (
                                <Select
                                    value={text}
                                    onChange={(value) => {
                                        handleChange(record.id, 'value', '');
                                        handleChange(record.id, 'assignmentMethod', value);
                                    }}
                                    style={{ width: 120 }}
                                    disabled={record.type === 3 || record.type === 4}
                                >
                                    <Option value={0}>表单字段</Option>
                                    <Option value={1}>系统变量</Option>
                                    <Option value={2}>固定值</Option>
                                </Select>
                            ) : null
                        ),
                    },
                    {
                        title: '取值',
                        dataIndex: 'value',
                        key: 'value',
                        render: (text, record) => (
                            <ParamValue
                                type={record.assignmentMethod}
                                value={text}
                                formId={formId}
                                onChange={v => handleChange(record.id, 'value', v)}
                            />
                        ),
                    },
                ])}
            />
        );
    }
}

ecodeSDK.exp(JsonParametersMapperTable);
ecodeSDK.setCom('${appId}', 'JsonParametersMapperTable', JsonParametersMapperTable);


