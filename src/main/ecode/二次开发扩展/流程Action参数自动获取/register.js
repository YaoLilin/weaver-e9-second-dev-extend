
let classPath = '';

/**
 * 获取 Action 配置对话框中的接口动作类文件输入框参数，获取到 Action 路径
 */
ecodeSDK.overwritePropsFnQueueMapSet('Input',{
    fn:(props)=>{
        if (props.ecId && props.ecId.includes('integrationWorkflowRegisterCustomFormclassname_InputItem')) {
            classPath = props.value;
        }
    }
})

/**
 * 重写 Action 配置对话框中的参数表格组件，在组件中获取 Action 中定义的所有参数（标准功能需要手动添加）
 */
ecodeSDK.overwriteClassFnQueueMapSet('Table',{
    fn:(Com,props)=>{
        if (props.ecId && props.ecId.includes('integrationWorkflowRegisterCustomForm1_Table')
            && !props._noOverwirite) {
            console.log('重写 table ',props);
            // 传入 action 路径
            props.classPath = classPath;
            return{
                com:paramTable,
                props:props
            }
        }
    }
});

const paramTable = (props)=>{
    const params ={
        appId:'${appId}',
        name:'ParamTable',
        isPage:false,
        noCss:true,
        props
    }
    const NewCom = props.Com;
    return window.comsMobx?ecodeSDK.getAsyncCom(params):(<NewCom {...props}/>);
}
