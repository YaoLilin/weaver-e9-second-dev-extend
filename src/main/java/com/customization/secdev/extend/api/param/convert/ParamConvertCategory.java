package com.customization.secdev.extend.api.param.convert;

import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

/**
 * @author 姚礼林
 * @desc 参数转换分类
 * @date 2026/1/22
 **/
public enum ParamConvertCategory {
    /**
     * 人力资源类别
     */
    HRM("人力资源",ParamConvertType.HRM_USER_ID_TO_NAME, ParamConvertType.HRM_USER_ID_TO_WORK_CODE,
            ParamConvertType.HRM_DEP_ID_TO_NAME),
    /**
     * 表单字段类别
     */
    FORM_FIELD("表单字段", ParamConvertType.PRIVATE_SELECTOR_SHOW_NAME,
            ParamConvertType.PUBLIC_SELECTOR_SHOW_NAME);


    private final ParamConvertType[] values;
    private final String categoryName;

    ParamConvertCategory(String categoryName, ParamConvertType... values) {
        this.categoryName = categoryName;
        this.values = values;
    }

    private static final EnumMap<ParamConvertType, ParamConvertCategory> categories =
            new EnumMap<>(ParamConvertType.class);
    static {
        for (ParamConvertCategory c : ParamConvertCategory.class.getEnumConstants()) {
            for (ParamConvertType type : c.values) {
                categories.put(type, c);
            }
        }
    }

    @Nullable
    public static ParamConvertCategory categorize(ParamConvertType type) {
        return categories.get(type);
    }

    public String getCategoryName() {
        return categoryName;
    }

    public ParamConvertType[] getValues() {
        return values;
    }
}
