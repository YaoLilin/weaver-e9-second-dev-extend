package com.customization.secdev.extend.api.param;

import lombok.Data;

import java.util.Map;

/**
 * @author 姚礼林
 * @desc API 参数对象
 * @date 2026/2/4
 **/
@Data
public class ApiParamObject {
    private Map<String,String > header;
    private Map<String ,String > queryParams;
    private Map<String, Object> body;
}
