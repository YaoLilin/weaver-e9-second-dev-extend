package com.customization.secdev.extend.api.param.convert;

/**
 * @author 姚礼林
 * @desc todo
 * @date 2026/1/22
 **/
public enum ParamConvertType {

    HRM_USER_ID_TO_NAME("HRM_USER_ID_TO_NAME","用户id转为用户名称"),
    HRM_USER_ID_TO_WORK_CODE("HRM_USER_ID_TO_WORK_CODE","用户id转为工号"),
    HRM_DEP_ID_TO_NAME("HRM_DEP_ID_TO_NAME","部门id转为部门名称"),
    PRIVATE_SELECTOR_SHOW_NAME("PRIVATE_SELECTOR_SHOW_NAME","获取独立选择框选项名称"),
    PUBLIC_SELECTOR_SHOW_NAME("PUBLIC_SELECTOR_SHOW_NAME", "获取公共选择框选项名称");

    private String key;
    private String name;

    ParamConvertType(String  key, String name) {
        this.key = key;
        this.name = name;
    }

    public String  getKey() {
        return this.key;
    }

    public String getTypeName() {
        return this.name;
    }


}
