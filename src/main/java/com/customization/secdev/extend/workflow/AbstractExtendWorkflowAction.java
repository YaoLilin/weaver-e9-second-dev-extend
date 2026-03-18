package com.customization.secdev.extend.workflow;

import cn.hutool.core.util.StrUtil;
import com.customization.secdev.extend.api.param.ApiParamValueInjector;
import com.customization.yll.common.exception.ActionConfigException;
import com.customization.yll.common.workflow.AbstractWorkflowAction;
import com.customization.yll.common.workflow.bean.ActionResult;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import weaver.soa.workflow.request.RequestInfo;

/**
 * @author 姚礼林
 * @desc 扩展的流程 Action 类，可使用高级 Action 参数
 * @param <T> Action 参数类型，为任意实体数据类，类中属性需使用
 * {@link com.customization.yll.common.workflow.anotations.ActionParam} 注解标记，被标记的属性会被视为 Action 参数
 *
 * @date 2026/1/15
 **/
@Setter
public abstract class AbstractExtendWorkflowAction<T> extends AbstractWorkflowAction
        implements ActionAdvanceParamAble<T> {
    private ApiParamValueInjector paramInjector;

    @NotNull
    @Override
    protected ActionResult doExecute(RequestInfo requestInfo) {
        if (paramInjector == null) {
            paramInjector = WorkflowApiParamValueInjector.instance();
        }
        Class<T> paramType = getParamType();
        String actionId = getActionId();
        if (StrUtil.isBlank(actionId)) {
            throw new ActionConfigException("ActionId 为空，请传入 ActionId");
        }
        T param = paramInjector.injectParam(paramType, Integer.parseInt(requestInfo.getRequestid()), actionId );
        return doExecute(requestInfo, param);
    }

    /**
     * 执行 Action
     *
     * @param requestInfo 流程请求信息
     * @param param       Acton 参数
     * @return 执行结果
     */
    abstract protected ActionResult doExecute(RequestInfo requestInfo, T param);

    /**
     * 获取 ActionId ，可以通过添加 Action 参数传入 ActionId 获取
     * @return ActionId
     */
    abstract protected String getActionId();
}
