package com.engine.interfaces.secdev.extend.api.domain.vo;

import lombok.Data;

/**
 * @author 姚礼林
 * @desc 系统参数 VO
 * @date 2026/1/15
 **/
@Data
public class SystemParamVO {
    private String code;
    private String name;

    public SystemParamVO() {
    }

    public SystemParamVO(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
