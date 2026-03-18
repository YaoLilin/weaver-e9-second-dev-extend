package com.customization.secdev.extend.api.param.exception;

/**
 * @author 姚礼林
 * @desc 参数值类型转换异常
 * @date 2026/2/5
 **/
public class ParamValueCaseException extends RuntimeException {
    public ParamValueCaseException(String message) {
        super(message);
    }

    public ParamValueCaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
