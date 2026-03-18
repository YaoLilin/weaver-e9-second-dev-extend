package com.customization.secdev.extend.api.param;

import java.util.Optional;

/**
 * @author 姚礼林
 * @desc API 参数生成接口，根据系统中配置的 API 参数映射，生成 API 接口参数
 * @date 2026/2/4
 **/
public interface ApiParamGenerator {

    /**
     * 根据指定的接口参数映射配置，生成接口参数
     *
     * @param confDataId 接口参数映射配置数据id
     * @param dataResourceId 数据资源id，可以是流程请求id或建模数据主表主键id
     * @return 生成接口参数结果，如果找不到参数映射配置则返回空
     */
    Optional<ApiParamObject> generateParams(int confDataId,int dataResourceId);

    /**
     * 根据接口参数映射配置的唯一标识与数据资源 id 生成接口参数（根据数据源类型自动走流程或建模）
     *
     * @param confId         接口参数映射配置的唯一标识
     * @param dataResourceId 数据资源 id（流程请求 id 或建模主表主键 id）
     * @return 生成接口参数结果，如果找不到参数映射配置则返回空
     */
    Optional<ApiParamObject> generateParamsByConfId(String confId, int dataResourceId);
}
