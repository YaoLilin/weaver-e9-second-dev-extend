

function getWorkflowIdFromUrl() {
    const hash = window.location.hash || '';
    let workflowId = null;
    if (hash) {
        const hashIndex = hash.indexOf('?');
        if (hashIndex !== -1) {
            const hashQuery = hash.substring(hashIndex + 1);
            const hashParams = new URLSearchParams(hashQuery);
            workflowId = hashParams.get('workflowId');
        }
    }
    if (!workflowId) {
        const search = window.location.search || '';
        if (search) {
            const params = new URLSearchParams(search);
            workflowId = params.get('workflowId');
        }
    }
    if (!workflowId) {
        return null;
    }
    const id = parseInt(workflowId, 10);
    return isNaN(id) ? null : id;
}

/**
 * 替换流程 Action 配置对话框中的 WeaRightMenu 组件（此组件是用来显示 Acton 配置表单的），在新组件内新增流程Action参数高级配置表
 * @author yaolilin
 */
ecodeSDK.overwriteClassFnQueueMapSet('WeaRightMenu',{
    fn:(Com,props)=>{
        if (props.ecId && props.ecId.includes('WeaRightMenu@s7resx') && !props._noOverwrite){
            const workflowId = getWorkflowIdFromUrl();
            const actionPath = window.workflowActionConfigStore ? window.workflowActionConfigStore.actionPath : null;
            return{
                com:NewWeaRightMenu,
                props:{
                    ...props,
                    workflowId: workflowId,
                    actionPath: actionPath
                }
            }
        }
    }
});

// 创建全局存储对象
if (!window.workflowActionConfigStore) {
    window.workflowActionConfigStore = {
        actionId :null,
        actionPath: null,
        workflowActionParamMenuInstance: null,
        onActionIdChange : null,
        onActionPathChange: null
    };
}

/**
 * 获取 Action 配置对话框中的接口动作标识
 * @author yaolilin
 */
ecodeSDK.overwritePropsFnQueueMapSet('Input',{
    fn:(props)=>{
        if (props.ecId && props.ecId.endsWith('InputItem@ujzpqo_WeaInput@bhq4j0_WeaInputNormal@5zat0z_Input@y16ozl')
            && props.name && props.name === 'actionid') {
            if(window.workflowActionConfigStore.onActionIdChange){
                window.workflowActionConfigStore.onActionIdChange(props.value);
                window.workflowActionConfigStore.actionId = props.value;
            }
        }
    }
});

/**
 * 获取 Action 配置对话框中的接口动作类文件输入框参数，获取到 Action 路径
 * @author yaolilin
 */
ecodeSDK.overwritePropsFnQueueMapSet('Input',{
    fn:(props)=>{
        if (props.ecId && props.ecId.includes('integrationWorkflowRegisterCustomFormclassname_InputItem')) {
            if (window.workflowActionConfigStore) {
                if (window.workflowActionConfigStore.actionPath !== props.value) {
                    window.workflowActionConfigStore.actionPath = props.value;
                    if (window.workflowActionConfigStore.onActionPathChange) {
                        window.workflowActionConfigStore.onActionPathChange(props.value);
                    }
                }
            }
        }
    }
});

/**
 * 拦截流程 Action 配置接口，获取 ActionId
 * @author yaolilin
 */
ecodeSDK.rewriteApiDataQueueSet({
    fn: (url, params, datas) => {
        try {
            if (url.indexOf('/api/integration/workflowflow/getInterfaceRegisterCustomAddForm') !== -1) {
                if (datas && datas.fieldValues) {
                    if (window.workflowActionConfigStore) {
                        window.workflowActionConfigStore.actionId = datas.fieldValues.actionid;
                        if (window.workflowActionConfigStore.onActionIdChange) {
                            window.workflowActionConfigStore.onActionIdChange(datas.fieldValues.actionid);
                        }
                        console.info('已通过接口拦截获取 ActionId ,id:' + datas.fieldValues.actionid);
                    }
                }
            }
        } catch (e) {
            console.error('拦截流程 Action 配置接口出错', e);
        }

        return datas;
    },
    desc: '拦截流程 Action 配置接口，获取 ActionId'
})

/**
 * 将前端数据转换为后端结构
 * 前端格式：{ name, showName, type (int), required (boolean), assignment (前端格式), detailTable (int), children }
 * 后端格式：{ name, showName, type (枚举), required (boolean), assignment (后端结构), detailTable (int), children }
 */
function convertFrontendDataToBackend(data, parentId) {
    if (!data || !Array.isArray(data)) {
        return [];
    }
    return data.map(item => {
        // 公共 ParametersMapperTable 使用 detailNum 保存明细表选择值，
        // 保存接口则使用 detailTable；优先使用当前组件字段，兼容公共组件字段。
        const detailTable = item.detailTable !== undefined && item.detailTable !== null
            ? item.detailTable
            : (item.detailNum !== undefined && item.detailNum !== null ? item.detailNum : null);
        const parsedId = item.id !== undefined && item.id !== null ? parseInt(item.id, 10) : null;
        const currentId = Number.isNaN(parsedId) ? null : parsedId;
        const parsedParentId = parentId !== undefined && parentId !== null ? parseInt(parentId, 10) : null;
        const currentParentId = Number.isNaN(parsedParentId) ? null : parsedParentId;
        const converted = {
            id: currentId,
            parentId: currentParentId,
            name: item.name || '',
            showName: item.showName || '',
            type: item.type !== undefined ? (typeof item.type === 'number' ? item.type : (item.type.value || item.type)) : 0,
            required: item.required !== undefined ? (item.required === true || item.required === 1) : false,
            detailTable: detailTable,
            assignment: item.assignment,
        };
        // 递归处理 children
        if (Array.isArray(item.children)) {
            converted.children = convertFrontendDataToBackend(item.children, currentId);
        }
        return converted;
    });
}

function getParamTypeValue(type) {
    if (type === null || type === undefined) {
        return 0;
    }
    if (typeof type === 'number') {
        return type;
    }
    if (typeof type === 'object' && type.value !== undefined) {
        return type.value;
    }
    return type;
}

function hasAssignmentValue(assignment) {
    if(!assignment || assignment.method === undefined || assignment.method === null || !assignment.value) {
        return false;
    }
    if(assignment.method === 1) {
        return assignment.value.workflowField && assignment.value.workflowField.fieldName;
    }
    if(assignment.method === 2 || assignment.method === 3) {
        return assignment.value.value !== undefined && assignment.value.value !== null && assignment.value.value !== '';
    }
}

function validateParamLevel(params) {
    if (!params || !Array.isArray(params)) {
        return null;
    }
    const nameMap = {};
    for (let i = 0; i < params.length; i++) {
        const param = params[i];
        const name = (param.name || '').trim();
        if (!name) {
            return '参数名称不能为空';
        }
        if (nameMap[name]) {
            return '同一层级参数名称重复：' + name;
        }
        nameMap[name] = true;

        const typeValue = getParamTypeValue(param.type);
        const isContainer = typeValue === 3 || typeValue === 4;
        if (param.required && !isContainer && !hasAssignmentValue(param.assignment)) {
            return {
                type: 'assignment',
                message: '必需参数必须选择赋值：' + name
            };
        }

        if (param.children && param.children.length > 0) {
            const childError = validateParamLevel(param.children);
            if (childError) {
                return childError;
            }
        }
    }
    return null;
}

/**
 * 重写在流程 Action 配置对话框的确定按钮的点击事件，点击后保存 Action 高级参数配置
 * @author yaolilin
 */
ecodeSDK.overwritePropsFnQueueMapSet('Button',{
    fn:(props)=>{
        if (props.ecId && props.ecId.includes('Button@7p1v0n@integrationWorkflowRegisterCustomButtonBTN_SAVE')){
            const originalOnClick = props.onClick;
            return {
                ...props,
                onClick: () => {
                    // 检查 actionId 是否存在
                    const actionId = window.workflowActionConfigStore.actionId;
                    if (!actionId) {
                        antd.message.error('请先输入Action标识');
                        return;
                    }

                    // 获取组件实例
                    const menuInstance = window.workflowActionConfigStore.workflowActionParamMenuInstance;
                    if (!menuInstance) {
                        antd.message.error('无法获取参数配置数据');
                        return;
                    }

                    // 获取参数数据
                    const paramData = menuInstance.getParamData();
                    if (!paramData || paramData.length === 0) {
                        originalOnClick();
                        return;
                    }

                    const saveAdvancedParams = () => {
                        // 将前端数据转换为后端结构
                        const convertedData = convertFrontendDataToBackend(paramData);
                        const url = '/api/second-dev/extend/workflow-action-param/config';
                        const loadingMsg = antd.message.loading('正在保存配置...', 0);
                        fetch(url, {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                            },
                            body: JSON.stringify({
                                actionId: actionId,
                                configs: convertedData
                            })
                        })
                            .then(response => response.json())
                            .then(result => {
                                loadingMsg();
                                if (!result || !result.success) {
                                    const errorMsg = result && result.message ? result.message : '未知错误';
                                    antd.message.error('保存高级 Action 参数失败：' + errorMsg, 5);
                                    return;
                                }

                                originalOnClick();
                            })
                            .catch(error => {
                                loadingMsg();
                                console.error('保存配置失败:', error);
                                antd.message.error('保存高级 Action 参数失败：' + (error.message || '未知错误'), 5);
                            });
                    };

                    const validationError = validateParamLevel(paramData);
                    if (validationError) {
                        if (validationError.type === 'assignment') {
                            antd.Modal.confirm({
                                title: '提示',
                                content: validationError.message + '，确认是否继续保存。',
                                okText: '确定',
                                cancelText: '取消',
                                onOk: saveAdvancedParams
                            });
                            return;
                        }
                        antd.message.error(validationError);
                        return;
                    }

                    saveAdvancedParams();
                }
            };
        }
    }
});

const NewWeaRightMenu = (props)=>{
    const params ={
        appId:'${appId}',
        name:'NewWeaRightMenu',
        isPage:false,
        noCss:true,
        props
    }
    const NewCom = props.Com;
    return window.comsMobx?ecodeSDK.getAsyncCom(params):(<NewCom {...props}/>);
}
