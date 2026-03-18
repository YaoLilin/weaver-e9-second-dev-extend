package com.engine.interfaces.secdev.extend.api.domain.dto;

import lombok.Data;

import java.util.List;

import com.engine.interfaces.secdev.extend.api.constants.ResourceType;

/**
 * @author 姚礼林
 * @desc 接口参数映射配置详情
 * @date 2026/1/29
 **/
@Data
public class InterfaceParamMappingDetail {
    private Integer id;
    private String confId;
    private String name;
    private ResourceType resourceType;
    private Integer workflowId;
    private String workflowName;
    private Integer workflowFormId;
    private Integer modeId;
    private String modeName;
    private String tableName;
    private Integer interfaceId;
    private String interfaceName;
    /**
     * 请求体明细表序号
     */
    private Integer bodyDetailNum;
    private List<ParamMappingItem> mappings;

    @Data
    public static class ParamMappingItem {
        private String paramId;
        private String paramName;
        private Assignment assignment;
        private Integer detailNum;
    }
}
