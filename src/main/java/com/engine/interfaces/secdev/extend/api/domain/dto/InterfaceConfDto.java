package com.engine.interfaces.secdev.extend.api.domain.dto;

import lombok.Data;

import java.util.List;

import com.engine.interfaces.secdev.extend.api.domain.enums.ParamType;

/**
 * @author yaolilin
 * @desc 接口配置信息
 * @date 2025/1/18
 **/
@Data
public class InterfaceConfDto {
    private Integer id;
    private String apiId;
    private String url;
    private Integer method;
    private String name;
    /**
     * 请求体类型：0-对象，1-数组
     */
    private Integer bodyType;
    private List<InterfaceParameter> queryParameters;
    private List<InterfaceParameter> bodyParameters;
    private List<InterfaceParameter> headerParameters;
    private List<InterfaceParameter> returnParameters;

    @Data
    public static class InterfaceParameter{
        private String id;
        private String name;
        private Integer required;
        private String showName;
        private ParamType type;
        private String parentId;
        private List<InterfaceParameter> children;
    }
}
