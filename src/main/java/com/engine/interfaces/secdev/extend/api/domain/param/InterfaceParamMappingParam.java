package com.engine.interfaces.secdev.extend.api.domain.param;

import com.engine.interfaces.secdev.extend.api.constants.ResourceType;
import com.engine.interfaces.secdev.extend.api.domain.dto.Assignment;
import lombok.Data;

import java.util.List;

/**
 * @author yaolilin
 * @desc 接口参数映射信息
 * @date 2025/2/14
 **/
@Data
public class InterfaceParamMappingParam {
    private String confId;
    private String name;
    private Integer apiId;
    private ResourceType dataSource;
    private Integer workflow;
    private Integer modeId;
    /**
     * 请求体明细表序号
     */
    private Integer bodyDetailNum;
    private List<ParamMapper> headerParameters;
    private List<ParamMapper> queryParameters;
    private List<ParamMapper> bodyParameters;

    @Data
    public static class ParamMapper{
        private String name;
        private String paramId;
        private Assignment assignment;
        private List<ParamMapper> children;
        private Integer detailNum;
    }
}
