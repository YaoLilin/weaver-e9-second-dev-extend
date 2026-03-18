package com.customization.secdev.extend.api.param;

/**
 * @author 姚礼林
 * @desc 接口参数值注入接口，可创建指定类型的参数，并对参数对象中的属性进行赋值。
 * @date 2026/1/16
 **/
public interface ApiParamValueInjector {

    /**
     * 创建指定类型的参数对象，并对对象中的属性进行赋值。如何赋值需根据配置指定，比如读取一个配置源中的参数赋值配置，按配置对参数对象属性
     * 进行赋值。
     *
     * @param paramType 参数类型，可以为任意的数据实体类
     * @param requestId 流程请求ID
     * @param actionId  Action ID，为调用该参数赋值的动作ID，比如流程的 Action ID，可用此 ID 来匹配到对应的参数配置源
     * @param <T>       参数的类型
     * @return 创建的参数对象，已进行赋值处理
     */
    <T> T injectParam(Class<T> paramType, Integer requestId, String actionId);

}
