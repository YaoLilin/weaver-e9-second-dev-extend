package com.customization.secdev.extend.api.param.convert;

/**
 * @author 姚礼林
 * @desc 接口参数值转换接口
 * @date 2026/1/22
 **/
public interface ParamValueConvertable {

    /**
     * 对参数值进行转换
     *
     * @param type          转换类型
     * @param originalValue 原始参数值
     * @return 转换结果
     */
    Object convert(ParamConvertType type, String originalValue);
}
