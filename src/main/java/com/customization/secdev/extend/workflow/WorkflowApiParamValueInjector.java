package com.customization.secdev.extend.workflow;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.customization.secdev.extend.api.param.ApiParamValueInjector;
import com.customization.yll.common.workflow.WorkflowFieldValueManager;
import com.customization.yll.common.workflow.anotations.ActionParam;
import com.customization.yll.common.workflow.interfaces.WorkflowFieldValueFetchInterface;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowActionAdvanceParamDTO;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowFieldValue;
import com.engine.interfaces.secdev.extend.api.domain.dto.Assignment;
import com.engine.interfaces.secdev.extend.api.domain.vo.AssignmentValue;
import com.engine.interfaces.secdev.extend.workflow.service.impl.WorkflowActionParamConfigServiceImpl;
import com.engine.interfaces.secdev.extend.api.domain.enums.AssignmentMethod;
import com.engine.interfaces.secdev.extend.api.domain.enums.SystemParamEnum;
import com.customization.yll.common.RecordSetFactory;
import com.engine.interfaces.secdev.extend.workflow.service.WorkflowActionParamConfigService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 姚礼林
 * @desc Workflow Action 参数注入器, 根据在流程 Action 配置的参数赋值，对 Action 高级参数对象内的属性进行赋值。<br>
 * <p>参数识别规则
 * <ul>
 *     <li>字段类型为基本类型或 List (元素类型为基本类型) ，可以不使用 {@link ActionParam} 注解，可被作为 Action 参数></li>
 *     <li>对于参数对象内的内嵌对象，以及 List 的元素类型为基本类型之外的类型，必需使用 {@link ActionParam} 注解 才可被认为是 Action 参数</li>
 *     <li>可识别参数对象的父类，嵌套对象，List 对象内的属性字段作为 Action 参数 </li>
 * </ul>
 *  <br>
 *  参数内的基本类型包括：String,Integer,Double,Float,Long,Boolean,BigDecimal  <br>
 *  <br>
 *  <p>赋值规则
 *  <ul>
 *      <li>如果字段类型为 List<Integer> ,获取的值为字符串且包含逗号，如：1,2,3 ，将会对字符串进行拆分转为 List</li>
 *      <li>如果字段类型为 List ,元素类型不为 Integer，且为基本类型，则将获取到的值生成一个元素，存入 List</li>
 *      <li>如果字段类型为 List ,对于元素对象内的字段，如果在前端 Action 参数配置中该 List 配置了明细表，List 下的参数的赋值
 *      选择明细表字段，则按每一条明细数据生成一个 List 元素对象，并获取明细字段值存入 List 中</li>
 *      <li>如果字段类型不为 List，赋值配置中选择了明细表字段，则将获取所有行的明细字段值，使用逗号拼接成字符串进行赋值</li>
 *  </ul>
 * @date 2026/1/15
 * @see WorkflowActionParamConfigService 高级 Action 参数的保存
 * @see WorkflowActionParamConfigDao 保存Action配置的持久化类
 **/
public class WorkflowApiParamValueInjector implements ApiParamValueInjector {
    private final WorkflowFieldValueFetchInterface workflowFieldValueManager;
    private final WorkflowSystemParamProvider systemParamProvider;
    private final WorkflowActionParamConfigServiceImpl configService;

    public WorkflowApiParamValueInjector(WorkflowFieldValueFetchInterface workflowFieldValueManager,
                                         WorkflowSystemParamProvider systemParamProvider,
                                         WorkflowActionParamConfigServiceImpl configService) {
        this.workflowFieldValueManager = workflowFieldValueManager;
        this.systemParamProvider = systemParamProvider;
        this.configService = configService;
    }

    public static WorkflowApiParamValueInjector instance() {
        WorkflowFieldValueManager fieldValueManager = new WorkflowFieldValueManager();
        return new WorkflowApiParamValueInjector(fieldValueManager, new WorkflowSystemParamProvider(fieldValueManager,
                RecordSetFactory.instance()), new WorkflowActionParamConfigServiceImpl());
    }


    /**
     * 创建指定类型的参数对象，并对对象中的属性进行赋值。根据在流程 Action 参数中配置的赋值，对 Action 高级参数进行赋值处理
     *
     * @param paramType 参数类型，可以为任意的数据实体类
     * @param requestId 流程请求ID
     * @param actionId  流程的 Action ID，可用此 ID 来匹配到对应的参数配置源
     * @param <T>       参数的类型
     * @return 创建的参数对象，已进行赋值处理
     */
    @Override
    public <T> T injectParam(Class<T> paramType, Integer requestId, String actionId) {
        if (paramType == null) {
            throw new IllegalArgumentException("paramType 不能为空");
        }
        if (requestId == null || requestId <= 0) {
            throw new IllegalArgumentException("requestId 不能为空");
        }
        if (StrUtil.isBlank(actionId)) {
            throw new IllegalArgumentException("actionId 不能为空");
        }

        try {
            List<WorkflowActionAdvanceParamDTO> configs = configService.getConfig(actionId);
            List<WorkflowActionAdvanceParamDTO> allConfigs = new ArrayList<>();
            flattenConfigs(configs, allConfigs);
            List<ConfigNode> configTree = buildConfigTree(allConfigs);
            Map<String, ConfigNode> configMap = new HashMap<>(allConfigs.size());
            buildConfigPathMap(configTree, "", configMap);

            T param = paramType.newInstance();
            injectObjectFields(param, paramType, "", configMap, requestId, null, null);
            return param;
        } catch (Exception e) {
            throw new IllegalArgumentException("注入 Action 参数失败:" + e.getMessage(), e);
        }
    }

    private boolean injectObjectFields(Object target, Class<?> targetType, String prefix,
                                       Map<String, ConfigNode> configMap,
                                       int requestId,
                                       Map<String, String> detailRow,
                                       Integer detailTableNum) throws IllegalAccessException {
        boolean hasValue = false;
        Field[] fields = targetType.getDeclaredFields();
        for (Field field : fields) {
            if (shouldSkip(field)) {
                continue;
            }
            String fieldPath = buildPath(prefix, field.getName());
            ConfigNode node = configMap.get(fieldPath);
            if (node == null) {
                continue;
            }

            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            boolean isList = List.class.isAssignableFrom(fieldType);
            boolean isSimple = isSimpleType(fieldType);
            boolean isAnnotated = field.isAnnotationPresent(ActionParam.class);

            if (isList) {
                List<?> listValue = resolveListValue(field, node, requestId, configMap, fieldPath);
                if (listValue != null) {
                    field.set(target, listValue);
                    hasValue = true;
                }
                continue;
            }

            if (isSimple) {
                Object value = resolveSimpleValue(fieldType, node, requestId, detailRow, detailTableNum);
                if (value != null || fieldType.isPrimitive()) {
                    field.set(target, value);
                    hasValue = value != null || fieldType.isPrimitive();
                }
                continue;
            }

            // 内嵌对象仅在标记 @ActionParam 时解析
            if (!isAnnotated) {
                continue;
            }

            Object child = createInstance(fieldType);
            boolean childHasValue = injectObjectFields(child, fieldType, fieldPath, configMap, requestId, detailRow, detailTableNum);
            if (childHasValue) {
                field.set(target, child);
                hasValue = true;
            }
        }
        return hasValue;
    }

    private List<?> resolveListValue(Field field, ConfigNode node, int requestId,
                                     Map<String, ConfigNode> configMap, String fieldPath) throws IllegalAccessException {
        Class<?> elementType = getListElementType(field);
        if (elementType == null) {
            return null;
        }

        boolean elementSimple = isSimpleType(elementType);
        boolean isAnnotated = field.isAnnotationPresent(ActionParam.class);

        if (elementSimple) {
            return resolveSimpleListValue(elementType, node, requestId);
        }

        // List<对象> 必须使用 @ActionParam 注解标记
        if (!isAnnotated) {
            return null;
        }
        return resolveObjectListValue(elementType, node, requestId, configMap, fieldPath);
    }

    private List<?> resolveSimpleListValue(Class<?> elementType, ConfigNode node, int requestId) {
        AssignmentMethod method = node.assignmentMethod;
        if (method == null) {
            return null;
        }
        List<Object> result = new ArrayList<>();
        if (method == AssignmentMethod.FORM_FIELD) {
            if (node.assignmentIsMainTable != null && node.assignmentIsMainTable) {
                String value = workflowFieldValueManager.getFieldValueByFieldName(requestId, node.assignmentFieldName);
                addConvertedValues(result, elementType, value);
                return result;
            }
            if (node.assignmentDetailTableNum != null) {
                List<Map<String, String>> detailRows = workflowFieldValueManager
                        .getDetailFields(requestId, node.assignmentDetailTableNum,
                                java.util.Collections.singletonList(node.assignmentFieldName));
                for (Map<String, String> row : detailRows) {
                    addConvertedValues(result, elementType, row.get(node.assignmentFieldName));
                }
                return result;
            }
        } else if (method == AssignmentMethod.SYSTEM_PARAM) {
            String value = systemParamProvider.getSystemParam(requestId, getSystemParamEnum(node.assignmentValue));
            addConvertedValues(result, elementType, value);
            return result;
        } else if (method == AssignmentMethod.FIXED_VALUE) {
            addConvertedValues(result, elementType, node.assignmentValue);
            return result;
        }
        return null;
    }

    private List<?> resolveObjectListValue(Class<?> elementType, ConfigNode node, int requestId,
                                           Map<String, ConfigNode> configMap, String fieldPath) throws IllegalAccessException {
        if (node.detailTable == null || node.detailTable <= 0) {
            throw new IllegalArgumentException("数组参数未配置明细表，参数：" + node.name);
        }
        List<String> detailFieldNames = collectDetailFieldNames(configMap, fieldPath, node.detailTable);
        if (CollUtil.isEmpty(detailFieldNames)) {
            return new ArrayList<>();
        }
        List<Map<String, String>> detailRows = workflowFieldValueManager.getDetailFields(requestId, node.detailTable, detailFieldNames);
        List<Object> result = new ArrayList<>();
        for (Map<String, String> row : detailRows) {
            Object element = createInstance(elementType);
            boolean hasValue = injectObjectFields(element, elementType, fieldPath, configMap, requestId, row, node.detailTable);
            if (hasValue) {
                result.add(element);
            }
        }
        return result;
    }

    private Object resolveSimpleValue(Class<?> fieldType, ConfigNode node, int requestId,
                                      Map<String, String> detailRow, Integer detailTableNum) {
        AssignmentMethod method = node.assignmentMethod;
        if (method == null) {
            return null;
        }
        if (method == AssignmentMethod.FORM_FIELD) {
            String value = resolveFormFieldValue(fieldType, node, requestId, detailRow, detailTableNum);
            return convertToTargetType(fieldType, value);
        }
        if (method == AssignmentMethod.SYSTEM_PARAM) {
            String value = systemParamProvider.getSystemParam(requestId, getSystemParamEnum(node.assignmentValue));
            return convertToTargetType(fieldType, value);
        }
        if (method == AssignmentMethod.FIXED_VALUE) {
            return convertToTargetType(fieldType, node.assignmentValue);
        }
        return null;
    }

    private String resolveFormFieldValue(Class<?> fieldType, ConfigNode node, int requestId,
                                         Map<String, String> detailRow, Integer detailTableNum) {
        if (node.assignmentIsMainTable != null && node.assignmentIsMainTable) {
            return workflowFieldValueManager.getFieldValueByFieldName(requestId, node.assignmentFieldName);
        }
        if (node.assignmentDetailTableNum == null) {
            return "";
        }
        if (detailRow != null) {
            if (detailTableNum == null || !detailTableNum.equals(node.assignmentDetailTableNum)) {
                throw new IllegalArgumentException("明细表序号不一致，配置明细表：" + node.assignmentDetailTableNum);
            }
            return detailRow.get(node.assignmentFieldName);
        }

        List<Map<String, String>> rows = workflowFieldValueManager.getDetailFields(requestId, node.assignmentDetailTableNum,
                java.util.Collections.singletonList(node.assignmentFieldName));
        if (CollUtil.isEmpty(rows)) {
            return "";
        }
        if (rows.size() == 1) {
            return rows.get(0).get(node.assignmentFieldName);
        }
        if (!String.class.equals(fieldType)) {
            throw new IllegalArgumentException("非字符串类型无法合并多行明细字段，字段：" + node.assignmentFieldName);
        }
        List<String> values = new ArrayList<>();
        for (Map<String, String> row : rows) {
            String value = row.get(node.assignmentFieldName);
            if (StrUtil.isNotBlank(value)) {
                values.add(value);
            }
        }
        return String.join(",", values);
    }

    private void addConvertedValue(List<Object> list, Class<?> elementType, String rawValue) {
        if (StrUtil.isBlank(rawValue)) {
            return;
        }
        Object value = convertToTargetType(elementType, rawValue);
        if (value != null) {
            list.add(value);
        }
    }

    private void addConvertedValues(List<Object> list, Class<?> elementType, String rawValue) {
        if (StrUtil.isBlank(rawValue)) {
            return;
        }
        boolean splitAsInteger = Integer.class.equals(elementType) || int.class.equals(elementType);
        if (splitAsInteger && rawValue.contains(",")) {
            String[] values = rawValue.split(",");
            for (String value : values) {
                addConvertedValue(list, elementType, value.trim());
            }
            return;
        }
        addConvertedValue(list, elementType, rawValue);
    }

    private Object convertToTargetType(Class<?> targetType, String rawValue) {
        if (StrUtil.isBlank(rawValue)) {
            return null;
        }
        try {
            if (String.class.equals(targetType)) {
                return rawValue;
            }
            if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
                return Integer.parseInt(rawValue);
            }
            if (Long.class.equals(targetType) || long.class.equals(targetType)) {
                return Long.parseLong(rawValue);
            }
            if (Double.class.equals(targetType) || double.class.equals(targetType)) {
                return Double.parseDouble(rawValue);
            }
            if (Float.class.equals(targetType) || float.class.equals(targetType)) {
                return Float.parseFloat(rawValue);
            }
            if (BigDecimal.class.equals(targetType)) {
                return new BigDecimal(rawValue);
            }
            if (Boolean.class.equals(targetType) || boolean.class.equals(targetType)) {
                return "1".equals(rawValue) || "true".equalsIgnoreCase(rawValue);
            }
            return rawValue;
        } catch (Exception e) {
            throw new IllegalArgumentException("参数值转换失败，目标类型：" + targetType.getSimpleName() + "，值：" + rawValue);
        }
    }

    private List<String> collectDetailFieldNames(Map<String, ConfigNode> configMap, String prefix, Integer detailTable) {
        List<String> fieldNames = new ArrayList<>();
        for (Map.Entry<String, ConfigNode> entry : configMap.entrySet()) {
            String path = entry.getKey();
            ConfigNode node = entry.getValue();
            if (!path.startsWith(prefix + ".")) {
                continue;
            }
            if (node.assignmentMethod == AssignmentMethod.FORM_FIELD
                    && node.assignmentIsMainTable != null
                    && !node.assignmentIsMainTable
                    && node.assignmentDetailTableNum != null
                    && node.assignmentDetailTableNum.equals(detailTable)) {
                if (StrUtil.isNotBlank(node.assignmentFieldName)) {
                    fieldNames.add(node.assignmentFieldName);
                }
            }
        }
        return fieldNames;
    }

    private AssignmentMethod getAssignmentMethod(Integer value) {
        if (value == null) {
            return null;
        }
        for (AssignmentMethod method : AssignmentMethod.values()) {
            if (method.getValue() == value) {
                return method;
            }
        }
        return null;
    }

    private SystemParamEnum getSystemParamEnum(String code) {
        if (StrUtil.isBlank(code)) {
            throw new IllegalArgumentException("系统参数代码不能为空");
        }
        for (SystemParamEnum paramEnum : SystemParamEnum.values()) {
            if (code.equals(paramEnum.getCode())) {
                return paramEnum;
            }
        }
        throw new IllegalArgumentException("未定义系统参数：" + code);
    }

    private List<ConfigNode> buildConfigTree(List<WorkflowActionAdvanceParamDTO> configs) {
        if (CollUtil.isEmpty(configs)) {
            return new ArrayList<>();
        }
        Map<Integer, ConfigNode> nodeMap = new HashMap<>(configs.size());
        for (WorkflowActionAdvanceParamDTO config : configs) {
            nodeMap.put(config.getId(), buildNode(config));
        }
        List<ConfigNode> roots = new ArrayList<>();
        for (WorkflowActionAdvanceParamDTO config : configs) {
            ConfigNode node = nodeMap.get(config.getId());
            Integer parentId = config.getParentId();
            if (parentId == null || parentId == 0) {
                roots.add(node);
                continue;
            }
            ConfigNode parent = nodeMap.get(parentId);
            if (parent != null) {
                parent.children.add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    private void flattenConfigs(List<WorkflowActionAdvanceParamDTO> configs, List<WorkflowActionAdvanceParamDTO> output) {
        if (CollUtil.isEmpty(configs)) {
            return;
        }
        for (WorkflowActionAdvanceParamDTO config : configs) {
            output.add(config);
            flattenConfigs(config.getChildren(), output);
        }
    }

    private ConfigNode buildNode(WorkflowActionAdvanceParamDTO config) {
        ConfigNode node = new ConfigNode();
        node.name = config.getName();
        node.detailTable = config.getDetailTable();
        Assignment assignment = config.getAssignment();
        if (assignment != null && assignment.getMethod() != null) {
            node.assignmentMethod = assignment.getMethod();
            AssignmentValue value = assignment.getValue();
            if (node.assignmentMethod == AssignmentMethod.FORM_FIELD && value != null
                && value.getWorkflowField() != null) {
                WorkflowFieldValue workflowField = value.getWorkflowField();
                node.assignmentIsMainTable = workflowField.getIsMainTable() != null && workflowField.getIsMainTable();
                node.assignmentDetailTableNum = workflowField.getDetailTableNum();
                node.assignmentFieldName = workflowField.getFieldName();
            } else if (value != null) {
                node.assignmentValue = value.getValue();
            }
        }
        return node;
    }

    private void buildConfigPathMap(List<ConfigNode> nodes, String prefix, Map<String, ConfigNode> map) {
        if (CollUtil.isEmpty(nodes)) {
            return;
        }
        for (ConfigNode node : nodes) {
            String path = buildPath(prefix, node.name);
            map.put(path, node);
            buildConfigPathMap(node.children, path, map);
        }
    }

    private String buildPath(String prefix, String name) {
        if (StrUtil.isBlank(prefix)) {
            return name;
        }
        return prefix + "." + name;
    }

    private Object createInstance(Class<?> type) {
        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("参数对象实例化失败:" + type.getName(), e);
        }
    }

    private boolean shouldSkip(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers) || field.isSynthetic();
    }

    private boolean isSimpleType(Class<?> type) {
        return type == String.class || type == Integer.class || type == int.class ||
                type == Boolean.class || type == boolean.class ||
                type == Double.class || type == double.class ||
                type == Float.class || type == float.class ||
                type == BigDecimal.class || type == Long.class || type == long.class;
    }

    private Class<?> getListElementType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class) {
                return (Class<?>) actualTypeArguments[0];
            }
        }
        return null;
    }

    private static class ConfigNode {
        private String name;
        private Integer detailTable;
        private AssignmentMethod assignmentMethod;
        private Boolean assignmentIsMainTable;
        private Integer assignmentDetailTableNum;
        private String assignmentFieldName;
        private String assignmentValue;
        private final List<ConfigNode> children = new ArrayList<>();
    }
}
