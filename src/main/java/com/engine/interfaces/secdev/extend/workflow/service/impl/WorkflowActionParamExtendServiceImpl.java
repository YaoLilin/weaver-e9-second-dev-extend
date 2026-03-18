package com.engine.interfaces.secdev.extend.workflow.service.impl;

import com.customization.yll.common.workflow.anotations.ActionParam;
import com.engine.core.impl.Service;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowActionParamDto;
import com.engine.interfaces.secdev.extend.workflow.service.WorkflowActionParamExtendService;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 姚礼林
 * @desc 流程 Acton 参数功能扩展业务类
 * @date 2025/10/17
 **/
public class WorkflowActionParamExtendServiceImpl extends Service implements WorkflowActionParamExtendService {

    @Override
    public List<WorkflowActionParamDto> getParams(String actionPath) throws ClassNotFoundException {
        List<WorkflowActionParamDto> result = new ArrayList<>();

        Class<?> clazz = Class.forName(actionPath);
        List<Field> fields = getAllFields(clazz);

        // 创建类的实例来获取字段的初始化值
        Object instance = createInstance(clazz);

        for (Field field : fields) {
            if (!isValidField(field)) {
                continue;
            }
            WorkflowActionParamDto dto = getWorkflowActionParamDto(field, instance);
            result.add(dto);
        }

        return result;
    }

    /**
     * 获取当前类及其父类的所有字段
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        if (declaredFields.length > 0) {
            java.util.Collections.addAll(fields, declaredFields);
        }
        return fields;
    }

    @NotNull
    private static WorkflowActionParamDto getWorkflowActionParamDto(Field field, Object instance) {
        WorkflowActionParamDto dto = new WorkflowActionParamDto();
        dto.setParamName(field.getName());

        ActionParam annotation = field.getAnnotation(ActionParam.class);
        if (annotation != null) {
            dto.setDisplayName(annotation.displayName());
            // 使用注解值
            dto.setDefaultValue(annotation.defaultValue());
            dto.setDesc(annotation.desc());
            dto.setRequired(annotation.required());
        } else {
            // 使用字段初始化值
            dto.setDefaultValue(getFieldInitialValue(field, instance));
            dto.setDesc("");
            dto.setRequired(false);
        }
        return dto;
    }

    /**
     * 获取字段的初始化值
     * 通过实例化对象来获取字段的实际值
     *
     * @param field    字段
     * @param instance 对象实例
     * @return 初始化值，如果无法获取则返回空字符串
     */
    private static String getFieldInitialValue(Field field, Object instance) {
        if (instance == null) {
            return "";
        }

        try {
            // 设置字段可访问
            field.setAccessible(true);

            // 获取字段值
            Object value = field.get(instance);

            // 如果值为null，返回空字符串
            if (value == null) {
                return "";
            }

            // 返回字符串值
            return value.toString();

        } catch (Exception e) {
            // 如果获取失败，返回空字符串
            return "";
        }
    }

    /**
     * 创建类的实例
     *
     * @param clazz 类
     * @return 实例，如果创建失败返回null
     */
    private static Object createInstance(Class<?> clazz) {
        try {
            // 尝试使用无参构造函数创建实例
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // 如果无法创建实例，返回null
            return null;
        }
    }

    /**
     * 验证字段是否符合条件
     * 条件：private、String 类型、非 final、非 static
     *
     * @param field 字段
     * @return 是否符合条件
     */
    private boolean isValidField(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isFinal(modifiers)
                && !Modifier.isStatic(modifiers)
                && field.getType().equals(String.class);
    }
}
