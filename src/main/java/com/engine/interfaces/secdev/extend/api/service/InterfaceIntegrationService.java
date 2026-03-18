package com.engine.interfaces.secdev.extend.api.service;

import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceConfDto;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceListItem;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceParamMappingDetail;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceParamMappingListItem;
import com.engine.interfaces.secdev.extend.api.domain.param.InterfaceConfParam;
import com.engine.interfaces.secdev.extend.api.domain.param.InterfaceParamMappingParam;
import com.customization.yll.common.web.modal.vo.PageResult;

/**
 * 接口集成配置服务
 *
 * @author yaolilin
 * @date 2025/1/17
 */
public interface InterfaceIntegrationService {

    /**
     * 创建接口配置
     *
     * @param interfaceConfParam 接口配置参数
     * @return 是否成功
     */
    boolean createInterfaceConf(InterfaceConfParam interfaceConfParam);

    /**
     * 分页查询接口列表
     *
     * @param pageNo   页码
     * @param pageSize 每页条数
     * @param name     接口名称
     * @param url      接口地址
     * @param apiId    接口标识
     * @return 分页结果
     */
    PageResult<InterfaceListItem> getInterfaceList(Integer pageNo, Integer pageSize,
                                                   String name, String url, String apiId);

    /**
     * 根据配置ID获取接口配置详情
     *
     * @param confId 配置ID
     * @return 接口配置DTO，不存在则返回null
     */
    InterfaceConfDto getInterfaceConf(Integer confId);

    /**
     * 保存接口参数映射
     *
     * @param mapperParam 参数映射参数
     * @return 是否成功
     */
    boolean saveApiParamMapping(InterfaceParamMappingParam mapperParam);

    /**
     * 分页查询接口参数映射列表
     *
     * @param pageNo   页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<InterfaceParamMappingListItem> getApiParamMappingList(Integer pageNo, Integer pageSize);

    /**
     * 根据ID获取接口参数映射详情
     *
     * @param id 映射ID
     * @return 参数映射详情，不存在则返回null
     */
    InterfaceParamMappingDetail getApiParamMappingDetail(Integer id);
}
