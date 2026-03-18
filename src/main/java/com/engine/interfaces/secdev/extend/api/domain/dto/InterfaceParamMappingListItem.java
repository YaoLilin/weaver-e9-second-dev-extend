package com.engine.interfaces.secdev.extend.api.domain.dto;

import lombok.Data;

/**
 * @author 姚礼林
 * @desc 接口参数映射配置列表项
 * @date 2026/1/29
 **/
@Data
public class InterfaceParamMappingListItem {
    private Integer id;
    private String confId;
    private String name;
    private Integer resourceType;
    private String resourceTypeName;
    private Integer workflowId;
    private String workflowName;
    private Integer modeId;
    private String modeName;
    private Integer interfaceId;
    private String interfaceName;
}
