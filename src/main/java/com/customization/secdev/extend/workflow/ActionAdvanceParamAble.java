package com.customization.secdev.extend.workflow;

/**
 * @param <T> 参数对象类型，可以为任意数据实体类，类中属性请使用
 *            {@link com.customization.yll.common.workflow.anotations.ActionParam} 注解标记
 * @author 姚礼林
 * @desc 流程 Action 启用高级配置参数接口，实现此接口即可使用高级配置参数
 * @date 2026/1/15
 **/
public interface ActionAdvanceParamAble<T> {
    /**
     * 获取参数类型
     * @return 参数类型
     */
    Class<T> getParamType();
}
