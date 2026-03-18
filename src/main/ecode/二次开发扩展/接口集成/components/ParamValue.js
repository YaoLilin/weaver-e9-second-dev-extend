const { WeaInput ,WeaFormItem,WeaBrowser} = ecCom;
const {Select} = antd;
const {Option} = Select;

class ParamValue extends React.Component {

    handleChange=(value)=>{
        const {onChange} = this.props;
        onChange(value);
    }

    getInputComponent =()=>{
        const {type,value} = this.props;
        if (type === 0){
            return this.getSelectFieldBrowser(value)
        }else if (type === 1) {
            return this.getSystemVarBrowser(value)
        }else if (type === 2){
            return (
                <WeaInput viewAttr="3" value={value} onChange={this.handleChange}/>
            )
        }
    }

    getSystemVarBrowser(value) {
        return (
            <WeaBrowser
                type={161}
                viewAttr="3"
                title={'系统变量'}
                inputStyle={{width: 200}}
                replaceDatas={[{id: value.id ? value.id : '', name: value.name ? value.name : ''}]}
                onChange={(ids, names, datas) => {
                    this.handleChange({id: ids, name: names})
                }}
                dataParams={
                    {
                        f_weaver_belongto_usertype: 0,
                        type: 'browser.param_sys_var',
                    }
                }
                conditionDataParams={{
                    fielddbtype: 'browser.param_sys_var',
                    type: 'browser.param_sys_var'
                }}
                completeParams={{
                    fielddbtype: "browser.param_sys_var",
                }}
                conditionURL={'/api/public/browser/condition/'}
                hasAdvanceSerach={true}
            />
        );
    }

    getSelectFieldBrowser(value) {
        const {formId} = this.props;
        let replaceDatas = [];
        if(value){
            replaceDatas = [{id: value.id, name: value.name }]
        }

        return (
            <WeaBrowser
                type={161}
                viewAttr="3"
                title={'选择表单字段'}
                inputStyle={{width: 200}}
                replaceDatas={replaceDatas}
                onChange={(ids, names, datas) => {
                    this.handleChange({id: ids, name: names})
                }}
                dataParams={
                    {
                        f_weaver_belongto_usertype: 0,
                        type: 'browser.select_bill_field',
                        billId:formId
                    }
                }
                conditionDataParams={{
                    fielddbtype: 'browser.select_bill_field',
                    type: 'browser.select_bill_field'
                }}
                completeParams={{
                    fielddbtype: "browser.select_bill_field",
                }}
                conditionURL={'/api/public/browser/condition/'}
                hasAdvanceSerach={true}
            />
        );
    }

    render() {
        return(
            <>
                {this.getInputComponent()}
            </>
        )
    }
}

ecodeSDK.exp(ParamValue);
