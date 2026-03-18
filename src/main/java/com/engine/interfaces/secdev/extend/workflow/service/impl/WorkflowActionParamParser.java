package com.engine.interfaces.secdev.extend.workflow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.customization.secdev.extend.workflow.ActionAdvanceParamAble;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.workflow.anotations.ActionParam;
import com.engine.interfaces.secdev.extend.api.domain.enums.ParamType;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowActionAdvanceParamDTO;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 姚礼林
 * @desc 流程 Acton 参数解析工具类
 * 解析规则：
 * 1. 简单类型（String/数值/Boolean）与 List 字段默认作为参数；
 * 2. 标记 @ActionParam 的字段作为参数；
 * 3. 内嵌对象字段仅在标记 @ActionParam 时才解析其子字段；
 * 4. List<对象> 仅在字段标记 @ActionParam 时解析元素对象字段；
 * 5. Map/静态字段/合成字段不会被解析。
 * @date 2026/1/15
 **/
public class WorkflowActionParamParser {
    private static final IntegrationLog log = new IntegrationLog(WorkflowActionParamParser.class);

    private WorkflowActionParamParser() {
    }

    /**
     * 解析 Action 参数
     *
     * @param actionPath Action 路径
     * @return 参数列表
     * @throws ClassNotFoundException 类未找到
     */
    public static List<WorkflowActionAdvanceParamDTO> parse(String actionPath) throws ClassNotFoundException {
        List<WorkflowActionAdvanceParamDTO> result = new ArrayList<>();

        Class<?> clazz;
        try {
            clazz = Class.forName(actionPath);
        } catch (ClassNotFoundException e) {
            return Collections.emptyList();
        }
        if (!ActionAdvanceParamAble.class.isAssignableFrom(clazz)) {
            return result;
        }

        try {
            // 实例化类
            Object instance = clazz.newInstance();
            // 调用接口中的 getParamType() 方法， 获取参数对象类型
            if (instance instanceof ActionAdvanceParamAble) {
                Class<?> paramType = ((ActionAdvanceParamAble<?>) instance).getParamType();
                if (paramType != null) {
                    result = parseParams(paramType);
                }
            }
        } catch (Exception e) {
            log.error("解析 Action 参数失败: " + actionPath, e);
        }

        return result;
    }

    /**
     * 解析参数类型中的字段，包括父类中的字段
     *
     * @param paramType 参数类型
     * @return 参数列表
     */
    private static List<WorkflowActionAdvanceParamDTO> parseParams(Class<?> paramType) {
        List<WorkflowActionAdvanceParamDTO> result = new ArrayList<>();
        if (paramType == null) {
            return result;
        }

        Class<?> current = paramType;
        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                if (shouldSkip(field)) {
                    continue;
                }

                WorkflowActionAdvanceParamDTO dto = new WorkflowActionAdvanceParamDTO();
                dto.setName(field.getName());

                ActionParam annotation = field.getAnnotation(ActionParam.class);
                boolean isAnnotated = annotation != null;

                Class<?> fieldType = field.getType();
                boolean isSimpleType = isSimpleType(fieldType);
                boolean isList = List.class.isAssignableFrom(fieldType);

                // 简单类型、List 类型或被注解标记的字段才视为 Action 参数
                if (isSimpleType || isList || isAnnotated) {
                    // 设置注解属性
                    if (isAnnotated) {
                        // 显示名读取 ActionParam 注解中的 displayName 字段
                        if (StrUtil.isNotBlank(annotation.displayName())) {
                            dto.setShowName(annotation.displayName());
                        } else {
                            dto.setShowName(field.getName());
                        }
                        dto.setRequired(annotation.required());
                        dto.setDefaultValue(annotation.defaultValue());
                        dto.setDesc(annotation.desc());
                    } else {
                        dto.setShowName(field.getName());
                        dto.setRequired(false);
                    }

                    // 设置参数类型
                    dto.setType(getTypeValue(fieldType));

                    // 处理嵌套对象或 List<T> 中的泛型参数
                    List<WorkflowActionAdvanceParamDTO> children = parseNestedType(field);
                    if (children != null && !children.isEmpty()) {
                        dto.setChildren(children);
                    }

                    result.add(dto);
                }
            }
            current = current.getSuperclass();
        }
        return result;
    }

    /**
     * 解析嵌套类型的字段
     * 支持：普通对象类型、List<T> 泛型类型
     *
     * @param field 字段
     * @return 子参数列表
     */
    private static List<WorkflowActionAdvanceParamDTO> parseNestedType(Field field) {
        Class<?> fieldType = field.getType();

        // 如果是 List 类型，获取泛型参数
        if (List.class.isAssignableFrom(fieldType)) {
            boolean isAnnotated = field.isAnnotationPresent(ActionParam.class);
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class) {
                    Class<?> elementType = (Class<?>) actualTypeArguments[0];
                    // 如果泛型参数不是简单类型，则解析其字段
                    if (isAnnotated && !isSimpleType(elementType) && !Map.class.isAssignableFrom(elementType)) {
                        return parseParams(elementType);
                    }
                }
            }
            return null;
        }

        // 如果是普通对象类型（非简单类型、非 Map、非 List）
        if (!isSimpleType(fieldType) && !Map.class.isAssignableFrom(fieldType) && !List.class.isAssignableFrom(fieldType)) {
            return parseParams(fieldType);
        }

        return null;
    }

    private static boolean shouldSkip(Field field) {
        int modifiers = field.getModifiers();
        // 排除静态字段、合成字段和Map类型
        return Modifier.isStatic(modifiers) || field.isSynthetic() || Map.class.isAssignableFrom(field.getType());
    }

    private static boolean isSimpleType(Class<?> type) {
        return type == String.class || type == Integer.class || type == int.class ||
                type == Boolean.class || type == boolean.class ||
                type == Double.class || type == double.class ||
                type == Float.class || type == float.class ||
                type == BigDecimal.class || type == Long.class || type == long.class;
    }

    private static ParamType getTypeValue(Class<?> type) {
        if (type == Integer.class || type == int.class ||
                type == Double.class || type == double.class ||
                type == Float.class || type == float.class ||
                type == Long.class || type == long.class ||
                type == BigDecimal.class) {
            return ParamType.NUMBER;
        } else if (type == Boolean.class || type == boolean.class) {
            return ParamType.BOOLEAN;
        } else if (List.class.isAssignableFrom(type)) {
            return ParamType.ARRAY;
        } else if (!isSimpleType(type) && !Map.class.isAssignableFrom(type)) {
            return ParamType.OBJECT;
        }
        return ParamType.STRING;
    }
}
