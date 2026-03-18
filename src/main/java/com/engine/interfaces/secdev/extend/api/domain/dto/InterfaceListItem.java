package com.engine.interfaces.secdev.extend.api.domain.dto;

import lombok.Data;

/**
 * @author yaolilin
 * @desc todo
 * @date 2025/1/23
 **/
@Data
public class InterfaceListItem {
    private Integer id;
    private String apiId;
    private String url;
    private String method;
    private String name;
}
