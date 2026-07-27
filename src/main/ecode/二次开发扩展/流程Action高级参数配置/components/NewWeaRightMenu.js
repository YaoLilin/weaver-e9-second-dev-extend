const {WeaRightMenu} = ecCom;
const {Button} = antd;
const ActionParamTable = ecodeSDK.imp(ActionParamTable);

/**
 * 流程 Action 配置表单组件，添加高级 Action 参数配置表格
 * @author yaolilin
 */
class NewWeaRightMenu extends React.Component {

    constructor( props) {
        super(props);
        this.state ={
            paramData:[],
        }
    }

    componentDidMount(){
        // 初始化全局存储对象
        if (!window.workflowActionConfigStore) {
            window.workflowActionConfigStore = {
                actionId: null,
                workflowActionParamMenuInstance: null
            };
        }
        // 将组件实例存储到全局变量
        window.workflowActionConfigStore.workflowActionParamMenuInstance = this;
    }

    componentWillUnmount(){
        // 清理全局变量
        if (window.workflowActionConfigStore && window.workflowActionConfigStore.workflowActionParamMenuInstance === this) {
            window.workflowActionConfigStore.workflowActionParamMenuInstance = null;
        }
    }

    /**
     * 获取参数数据
     */
    getParamData(){
        return this.state.paramData;
    }

    getAdvancedParamContent(){
        const {paramData} = this.state;
        return (
            <div style={{padding:'0 25px'}}>
                <p style={{paddingBottom: 10}}>高级参数配置</p>
                <ActionParamTable
                    data={paramData}
                    onChange={newData => this.setState({paramData:newData})}
                    dataSource={this.props.dataSource}
                    workflowId={this.props.workflowId}
                    modeId={this.props.modeId}
                    actionPath={this.props.actionPath}
                />
            </div>
        )
    }

    render() {
        const children = [];
        if(this.props.children && this.props.children.length > 0){
            children.push(this.props.children[0]);
            children.push(this.getAdvancedParamContent());
            children.push(this.props.children.slice(1));
        }

        return  <WeaRightMenu {...this.props} children={children} _noOverwrite/>
    }
}

ecodeSDK.setCom('${appId}', 'NewWeaRightMenu', NewWeaRightMenu);
