package com.customization.secdev.extend.workflow;

import cn.hutool.core.util.StrUtil;
import com.customization.secdev.extend.api.param.ApiParamValueInjector;
import com.customization.yll.common.exception.ActionConfigException;
import com.customization.yll.common.workflow.AbstractWorkflowAction;
import com.customization.yll.common.workflow.anotations.ActionParam;
import com.customization.yll.common.workflow.bean.ActionResult;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import weaver.soa.workflow.request.RequestInfo;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.List;

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
        verifyRequiredParams(param);
        return doExecute(requestInfo, param);
    }

    /**
     * 校验高级 Action 参数对象中标记为必填的属性。
     * 嵌套对象及 List 中的对象会继续校验；未标记 {@link ActionParam} 的属性不参与校验。
     *
     * @param param 注入后的 Action 参数
     */
    private void verifyRequiredParams(T param) {
        verifyRequiredParams(param, "", new IdentityHashMap<>());
    }

    private void verifyRequiredParams(Object param, String path, IdentityHashMap<Object, Boolean> visited) {
        if (param == null || isSimpleValue(param)) {
            return;
        }
        if (visited.put(param, Boolean.TRUE) != null) {
            return;
        }
        if (param instanceof List) {
            List<?> values = (List<?>) param;
            for (int index = 0; index < values.size(); index++) {
                verifyRequiredParams(values.get(index), path + "[" + index + "]", visited);
            }
            return;
        }
        if (param.getClass().isArray()) {
            int length = Array.getLength(param);
            for (int index = 0; index < length; index++) {
                verifyRequiredParams(Array.get(param, index), path + "[" + index + "]", visited);
            }
            return;
        }

        for (Class<?> clazz = param.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())
                        || !field.isAnnotationPresent(ActionParam.class)) {
                    continue;
                }
                ActionParam actionParam = field.getAnnotation(ActionParam.class);
                String fieldPath = StrUtil.isEmpty(path) ? field.getName() : path + "." + field.getName();
                Object value = getFieldValue(param, field, fieldPath);
                if (actionParam.required() && isEmptyRequiredValue(value)) {
                    throw new ActionConfigException(String.format("Action 参数不正确，[%s] 参数必填，请检查 Action 参数配置", fieldPath));
                }
                if (value != null) {
                    verifyRequiredParams(value, fieldPath, visited);
                }
            }
        }
    }

    private Object getFieldValue(Object param, Field field, String fieldPath) {
        try {
            field.setAccessible(true);
            return field.get(param);
        } catch (IllegalAccessException e) {
            throw new ActionConfigException(String.format("Action 参数校验异常，无法读取参数 [%s]", fieldPath));
        }
    }

    private boolean isEmptyRequiredValue(Object value) {
        return value == null || (value instanceof String && StrUtil.isBlank((String) value));
    }

    private boolean isSimpleValue(Object value) {
        Class<?> type = value.getClass();
        if (value instanceof List || type.isArray()) {
            return false;
        }
        return value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character
                || type.isEnum() || type.isPrimitive() || (type.getPackage() != null
                && type.getPackage().getName().startsWith("java."));
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
