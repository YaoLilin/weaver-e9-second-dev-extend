package com.customization.secdev.extend.api.param;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.customization.secdev.extend.api.param.exception.ParamValueCaseException;
import com.customization.secdev.extend.workflow.WorkflowApiParamValueInjector;
import com.customization.secdev.extend.workflow.WorkflowSystemParamProvider;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.exception.ConfigurationException;
import com.customization.yll.common.workflow.WorkflowFieldValueManager;
import com.customization.yll.common.workflow.interfaces.WorkflowFieldValueFetchInterface;
import com.engine.interfaces.secdev.extend.api.constants.ResourceType;
import com.engine.interfaces.secdev.extend.api.dao.InterfaceParamMappingDao;
import com.engine.interfaces.secdev.extend.api.domain.dto.Assignment;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceConfDto;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceConfDto.InterfaceParameter;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceParamMappingDetail;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceParamMappingDetail.ParamMappingItem;
import com.engine.interfaces.secdev.extend.api.domain.enums.AssignmentMethod;
import com.engine.interfaces.secdev.extend.api.domain.enums.ParamType;
import com.engine.interfaces.secdev.extend.api.domain.enums.SystemParamEnum;
import com.engine.interfaces.secdev.extend.api.domain.vo.AssignmentValue;
import com.engine.interfaces.secdev.extend.api.service.InterfaceIntegrationService;
import com.engine.interfaces.secdev.extend.api.service.impl.InterfaceIntegrationServiceImpl;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowFieldValue;
import org.jetbrains.annotations.Nullable;
import weaver.conn.RecordSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 根据系统中配置的 API 参数映射与赋值规则生成 API 接口参数（header/query/body）。
 * 支持流程与建模两种数据源；赋值方式包括表单字段、系统参数（仅流程）、固定值，规则参照 WorkflowApiParamValueInjector。
 *
 * @author 姚礼林
 * @date 2026/2/4
 * @see WorkflowApiParamValueInjector 赋值规则参照此类
 */
public class ApiParamGeneratorImpl implements ApiParamGenerator {

    private final InterfaceParamMappingDao mappingDao;
    private final InterfaceIntegrationService interfaceIntegrationService;
    private final WorkflowFieldValueFetchInterface workflowFieldValueManager;
    private final WorkflowSystemParamProvider systemParamProvider;
    private final ModeFieldValueFetcher modeFieldValueFetcher;
    private final IntegrationLog log = new IntegrationLog(this.getClass());

    public ApiParamGeneratorImpl(InterfaceParamMappingDao mappingDao,
                                 InterfaceIntegrationService interfaceIntegrationService,
                                 WorkflowFieldValueFetchInterface workflowFieldValueManager,
                                 WorkflowSystemParamProvider systemParamProvider,
                                 ModeFieldValueFetcher modeFieldValueFetcher) {
        this.mappingDao = mappingDao;
        this.interfaceIntegrationService = interfaceIntegrationService;
        this.workflowFieldValueManager = workflowFieldValueManager;
        this.systemParamProvider = systemParamProvider;
        this.modeFieldValueFetcher = modeFieldValueFetcher;
    }

    public static ApiParamGenerator instance() {
        InterfaceParamMappingDao mappingDao = new InterfaceParamMappingDao(new RecordSet());
        InterfaceIntegrationService service = new InterfaceIntegrationServiceImpl();
        WorkflowFieldValueFetchInterface workflowFieldValueFetcher = new WorkflowFieldValueManager();
        WorkflowSystemParamProvider systemParamProvider = new WorkflowSystemParamProvider(
                workflowFieldValueFetcher, new RecordSet()
        );
        ModeFieldValueFetcher modeFieldValueFetcher = new ModeFieldValueFetcher(new RecordSet());
        return new ApiParamGeneratorImpl(mappingDao, service, workflowFieldValueFetcher,
                systemParamProvider, modeFieldValueFetcher);
    }

    /**
     * 根据指定的接口参数映射配置，生成接口参数
     *
     * @param confDataId     接口参数映射配置数据id
     * @param dataResourceId 数据资源id，可以是流程请求id或建模数据主表主键id
     * @return 生成接口参数结果，如果找不到参数映射配置则返回空
     * @throws ConfigurationException   如果接口参数映射配置有误则抛出此异常
     * @throws IllegalArgumentException 如果 confDataId 参数小于0则抛出异常
     * @throws ParamValueCaseException  如果参数赋值时参数值转换为该参数对应的类型时失败则抛出此异常
     */
    @Override
    public Optional<ApiParamObject> generateParams(int confDataId, int dataResourceId) {
        if (confDataId < 0) {
            throw new IllegalArgumentException("confDataId 参数不正确，不能小于0");
        }
        if (dataResourceId < 0) {
            log.warn("dataResourceId 小于0，将不生成接口参数");
            return Optional.empty();
        }
        InterfaceParamMappingDetail mappingConfig = interfaceIntegrationService.getApiParamMappingDetail(confDataId);
        if (mappingConfig == null) {
            log.warn("未找到映射配置,confDataId:{}", confDataId);
            return Optional.empty();
        }
        ResourceType resourceType = mappingConfig.getResourceType();
        Objects.requireNonNull(resourceType, "数据源类型不能为空");
        log.info("数据源类型：{}", resourceType);

        return Optional.of(buildParams(mappingConfig, dataResourceId));
    }

    /**
     * 生成接口参数并进行赋值，根据传入的配置id获取接口参数映射配置，按配置进行赋值
     * （根据配置中的数据源类型获取流程或建模的表单数据对字段进行赋值）
     *
     * @param confId         接口参数映射配置的唯一标识
     * @param dataResourceId 数据资源 id（流程请求 id 或建模主表主键 id）
     * @return 生成接口参数结果，如果找不到参数映射配置则返回空
     * @throws ConfigurationException 如果接口参数映射配置有误则抛出此异常
     * @throws ParamValueCaseException  如果参数赋值时参数值转换为该参数对应的类型时失败则抛出此异常
     */
    @Override
    public Optional<ApiParamObject> generateParamsByConfId(String confId, int dataResourceId) {
        Integer id = mappingDao.findIdByConfId(confId);
        if (id == null) {
            return Optional.empty();
        }
        return generateParams(id, dataResourceId);
    }

    /**
     * 生成接口参数并进行赋值，根据传入的配置id获取接口参数映射配置，按配置进行赋值
     * （根据配置中的数据源类型获取流程或建模的表单数据对字段进行赋值）
     *
     * @param confId         接口参数映射配置的唯一标识
     * @param dataResourceId 数据资源 id（流程请求 id 或建模主表主键 id）
     * @param resourceType   数据源类型，传入的数据源类型必需与配置的数据源类型一致
     * @return 生成接口参数结果，如果找不到参数映射配置则返回空
     * @throws IllegalArgumentException 如果传入的 confId 为空则抛出此异常
     * @throws IllegalStateException    如果此接口参数映射的数据源类型与传入的数据源类型不匹配则抛出此异常
     * @throws ConfigurationException   如果接口参数映射配置有误则抛出此异常
     * @throws ParamValueCaseException  如果参数赋值时参数值转换为该参数对应的类型时失败则抛出此异常
     */
    public Optional<ApiParamObject> generateParamsByConfId(String confId, int dataResourceId, ResourceType resourceType) {
        if (StrUtil.isBlank(confId)) {
            throw new IllegalArgumentException("confId 不能为空");
        }
        Objects.requireNonNull(resourceType, "数据源类型不能为空");
        Integer id = mappingDao.findIdByConfId(confId);
        if (id == null) {
            log.warn("未找到接口映射配置，confId：{}", confId);
            return Optional.empty();
        }
        InterfaceParamMappingDetail mappingConfig = interfaceIntegrationService.getApiParamMappingDetail(id);
        if (mappingConfig.getResourceType() != resourceType) {
            throw new IllegalStateException("此接口参数映射的数据源类型与传入的数据源类型不匹配，接口参数映射的数据源类型:" +
                    mappingConfig.getResourceType() + ",传入的数据源类型：" + resourceType + ", confId:" + confId);
        }
        return generateParams(id, dataResourceId);
    }


    /**
     * 根据映射配置与上下文（流程 requestId 或建模 modeMainId）组装 API 参数对象。
     */
    private ApiParamObject buildParams(InterfaceParamMappingDetail detail, int resourceDataId) {
        Integer interfaceId = detail.getInterfaceId();
        if (interfaceId == null || interfaceId < 0) {
            throw new ConfigurationException("接口id不能为空");
        }
        InterfaceConfDto interfaceConf = interfaceIntegrationService.getInterfaceConf(interfaceId);
        if (interfaceConf == null) {
            throw new ConfigurationException("无法根据此id找到对应的接口配置，id:" + interfaceId);
        }
        List<InterfaceParameter> headerParameters = interfaceConf.getHeaderParameters();
        List<InterfaceParameter> queryParameters = interfaceConf.getQueryParameters();
        List<InterfaceParameter> bodyParameters = interfaceConf.getBodyParameters();
        List<ParamMappingItem> paramMappings = detail.getMappings();
        Map<String, ParamMappingItem> paramIdMappingItemMap = new HashMap<>(paramMappings.size());
        for (ParamMappingItem item : paramMappings) {
            paramIdMappingItemMap.put(item.getParamId(), item);
        }

        ApiParamObject paramObject = new ApiParamObject();
        paramObject.setQueryParams(convertToStrValueMap(generateParams(false, null, detail,
                queryParameters, paramIdMappingItemMap, resourceDataId)));
        paramObject.setHeader(convertToStrValueMap(generateParams(false, null,
                detail, headerParameters, paramIdMappingItemMap, resourceDataId)));
        paramObject.setBody(generateParams(false, null, detail, bodyParameters,
                paramIdMappingItemMap, resourceDataId));

        return paramObject;
    }

    private Map<String, String> convertToStrValueMap(Map<String, Object> map) {
        if (CollUtil.isEmpty(map)) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue().toString();
            result.put(entry.getKey(), value);
        }
        return result;
    }

    /**
     * 生成一组 Map 类型的参数，不支持遍历明细表生成多条数据
     *
     * @param detail    为 True 表示需要正在遍历明细数据生成参数，此时不能对赋值为传入的明细表序号对应的明细表字段的参数进行赋值，不由该
     *                  方法处理
     * @param detailNum 如果 detail 参数为 true ,则当前参数必需传入
     */
    private Map<String, Object> generateParams(boolean isDetail, Integer detailNum, InterfaceParamMappingDetail detail,
                                               List<InterfaceParameter> parameters,
                                               Map<String, ParamMappingItem> paramIdMappingItemMap,
                                               int dataSourceId) {
        if (CollUtil.isEmpty(parameters)) {
            return Collections.emptyMap();
        }
        if (isDetail) {
            Objects.requireNonNull(detailNum, "isDetail 为 true 时，明细表序号必需传入");
            if (detailNum < 1) {
                throw new IllegalArgumentException("detailNum 必需大于 0");
            }
        }
        Map<String, Object> result = new HashMap<>(16);
        for (InterfaceParameter parameter : parameters) {
            log.info("生成参数，名称：{},id:{}", parameter.getName(), parameter.getId());
            ParamMappingItem paramMapping = paramIdMappingItemMap.get(parameter.getId());
            if (paramMapping == null) {
                log.info("当前参数没有配置赋值，赋值为 null");
                result.put(parameter.getName(), null);
                continue;
            }
            // 判断当前参数的赋值的表单字段是否是参数传入的明细表的的明细字段，如果是明细字段则跳过，不对明细字段的参数进行赋值
            WorkflowFieldValue workflowFieldValueObj = getWorkflowFieldValueObj(paramMapping.getAssignment());
            if (workflowFieldValueObj != null) {
                if (!workflowFieldValueObj.getIsMainTable() && isDetail &&
                        Objects.equals(workflowFieldValueObj.getDetailTableNum(), detailNum)) {
                    continue;
                }
            }

            Object value = generateParamValue(detail, paramIdMappingItemMap, dataSourceId, parameter, paramMapping);
            log.debug("赋值结果：{}", value);
            result.put(parameter.getName(), value);
        }
        return result;
    }

    @Nullable
    private Object generateParamValue(InterfaceParamMappingDetail detail,
                                      Map<String, ParamMappingItem> paramIdMappingItemMap,
                                      int dataSourceId, InterfaceParameter parameter,
                                      ParamMappingItem paramMappingItem) {
        Object value;
        if (parameter.getType() == ParamType.ARRAY) {
            value = new ArrayList<>();
            if (CollUtil.isNotEmpty(parameter.getChildren())) {
                // 如果该参数数组，并且选择了明细表，则生成该参数的 List 数组，并获取明细表中字段的值
                Integer detailNum = paramMappingItem.getDetailNum();
                if (detailNum != null) {
                    value = generateParamsForList(detail, paramMappingItem.getDetailNum(),
                            parameter.getChildren(), paramIdMappingItemMap, dataSourceId);
                }
            }
        } else if (parameter.getType() == ParamType.OBJECT) {
            // 参数类型属于对象，则该参数值的类型为 Map
            if (CollUtil.isNotEmpty(parameter.getChildren())) {
                value = generateParams(false, null, detail,
                        parameter.getChildren(), paramIdMappingItemMap,
                        dataSourceId);
            } else {
                value = Collections.emptyMap();
            }
        } else {
            // 其它参数类型（基本类型），如文本、Boolean 、数字，以及 List （没有嵌套对象，如：[1,2,3] ）
            value = getParamValueByAssignmentConfig(parameter, paramMappingItem, dataSourceId, detail);
        }
        return value;
    }

    @Nullable
    private WorkflowFieldValue getWorkflowFieldValueObj(Assignment assignment) {
        if (assignment != null) {
            AssignmentValue assignmentValue = assignment.getValue();
            if (assignmentValue != null) {
                return assignmentValue.getWorkflowField();
            }
        }
        return null;
    }

    /**
     * 生成 List 类型的参数，可获取表单的明细数据，添加到 List 参数中，即有多少行明细，List 就有多少条数据
     */
    private List<Map<String, Object>> generateParamsForList(InterfaceParamMappingDetail detail,
                                                            Integer detailNum, List<InterfaceParameter> parameters,
                                                            Map<String, ParamMappingItem> paramIdMappingItemMap,
                                                            int dataSourceId) {
        List<InterfaceParameter> detailFieldParams = new ArrayList<>();
        Set<String> detailFieldNames = new HashSet<>();
        // 获取参数中赋值配置的明细字段
        getDetailFieldNamesFromParams(detailNum, parameters, paramIdMappingItemMap,
                detailFieldNames, detailFieldParams);

        // 获取明细数据
        List<Map<String, String>> detailFields = new ArrayList<>();
        if (detail.getResourceType() == ResourceType.WORKFLOW) {
            detailFields = workflowFieldValueManager.getDetailFields(dataSourceId, detailNum,
                    new ArrayList<>(detailFieldNames));
        } else if (detail.getResourceType() == ResourceType.MODE) {
            detailFields = modeFieldValueFetcher.getDetailFields(detail.getTableName(),
                    dataSourceId, detailNum, new ArrayList<>(detailFieldNames));
        }

        List<Map<String, Object>> paramData = new ArrayList<>();
        // 生成这组参数中非赋值为明细字段的参数值
        Map<String, Object> params = generateParams(true, detailNum, detail,
                parameters, paramIdMappingItemMap, dataSourceId);
        if (CollUtil.isNotEmpty(detailFields)) {
            // 将非赋值为明细字段的参数与获取到的明细数据合并
            for (Map<String, String> detailField : detailFields) {
                Map<String, Object> row = new HashMap<>(16);
                row.putAll(params);

                for (InterfaceParameter p : detailFieldParams) {
                    Object detailFieldValue = getDetailFieldValue(paramIdMappingItemMap, detailField, p);
                    row.put(p.getName(), detailFieldValue);
                }
                paramData.add(row);
            }
        } else {
            // 如果获取到的明细数据为空并且其它赋值为非明细字段的参数有数据，则添加空值，只生成1条数据
            if (CollUtil.isNotEmpty(params)) {
                for (String fieldName : detailFieldNames) {
                    params.put(fieldName, null);
                }
                paramData.add(params);
            }
        }
        return paramData;
    }

    private static Object getDetailFieldValue(Map<String, ParamMappingItem> paramIdMappingItemMap,
                                              Map<String, String> detailField,
                                              InterfaceParameter p) {
        ParamMappingItem paramMapping = paramIdMappingItemMap.get(p.getId());
        if (paramMapping == null) {
            throw new IllegalStateException("无法获取该参数的映射对象，参数名称："
                    + p.getName() + ",参数id：" + p.getId());
        }

        String fieldName = paramMapping.getAssignment().getValue()
                .getWorkflowField().getFieldName();
        return detailField.get(fieldName);
    }

    private void getDetailFieldNamesFromParams(Integer detailNum, List<InterfaceParameter> parameters,
                                               Map<String, ParamMappingItem> paramIdMappingItemMap,
                                               Set<String> detailFieldNames,
                                               List<InterfaceParameter> detailFieldParams) {
        for (InterfaceParameter parameter : parameters) {
            ParamType type = parameter.getType();
            if (type != null) {
                if (type != ParamType.OBJECT && type != ParamType.ARRAY) {
                    ParamMappingItem paramMapping = paramIdMappingItemMap.get(parameter.getId());
                    if (paramMapping != null) {
                        Assignment assignment = paramMapping.getAssignment();
                        WorkflowFieldValue workflowFieldValueObj = getWorkflowFieldValueObj(assignment);
                        if (workflowFieldValueObj != null) {
                            if (!workflowFieldValueObj.getIsMainTable() &&
                                    Objects.equals(workflowFieldValueObj.getDetailTableNum(), detailNum)) {
                                detailFieldNames.add(workflowFieldValueObj.getFieldName());
                                detailFieldParams.add(parameter);
                            }
                        }
                    }
                }
            }
        }
    }

    private Object getParamValueByAssignmentConfig(InterfaceParameter parameter, ParamMappingItem paramMappingItem,
                                                   int dataSourceId, InterfaceParamMappingDetail config) {
        Assignment assignment = paramMappingItem.getAssignment();
        if (assignment == null) {
            log.debug("此参数没有配置赋值方式，参数id：{}，参数名称：{}", parameter.getId(), parameter.getName());
            return null;
        }
        AssignmentMethod method = assignment.getMethod();
        if (method == null) {
            throw new ConfigurationException("参数赋值方式不能为空");
        }
        AssignmentValue assignmentValue = assignment.getValue();
        if (assignmentValue == null) {
            log.warn("此参数的 AssignmentValue 为 null ，参数id：{}，参数名称：{}", parameter.getId(),
                    parameter.getName());
            return null;
        }
        Object value = null;
        switch (method) {
            case FORM_FIELD:
                Object formFieldValue = getFormFieldValue(config.getResourceType(), parameter, dataSourceId,
                        config.getTableName(), assignmentValue);
                if (formFieldValue instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> values = (ArrayList<String>) formFieldValue;
                    if (CollUtil.isNotEmpty(values)) {
                        value = String.join(",", values);
                    }
                } else {
                    value = formFieldValue;
                }
                break;
            case SYSTEM_PARAM:
                if (config.getResourceType() == ResourceType.WORKFLOW) {
                    String selectedSystemParam = assignment.getValue().getValue();
                    if (StrUtil.isBlank(selectedSystemParam)) {
                        throw new ConfigurationException(String.format("未选择系统参数，参数id：%s,参数名称：%s",
                                parameter.getId(), parameter.getName()));
                    }
                    SystemParamEnum systemParamType = SystemParamEnum.getByCode(selectedSystemParam);
                    if (systemParamType == null) {
                        log.warn("系统参数类型不存在，参数id：%s,参数名称：%s", parameter.getId(), parameter.getName());
                        break;
                    }
                    value = systemParamProvider.getSystemParam(dataSourceId, systemParamType);
                }
                break;
            case FIXED_VALUE:
                value = assignmentValue.getValue();
                break;
            default:
                log.warn("不支持的赋值类型：" + method);
        }
        if (value == null) {
            return null;
        }

        return convertToType(parameter, value);
    }

    private Object convertToType(InterfaceParameter parameter, Object value) {
        ParamType paramType = parameter.getType();
        // 请求头参数和查询参数的参数类型会为空，默认为 String
        if (parameter.getType() == null) {
            log.info("该参数的类型为空，当作字符串类型处理，参数名称:{}", parameter.getName());
            paramType = ParamType.STRING;
        }
        switch (paramType) {
            case STRING:
                return String.valueOf(value);
            case NUMBER:
                try {
                    return convertToNumber(value.toString());
                } catch (NumberFormatException e) {
                    throw new ParamValueCaseException("无法将此参数值转换成对应的类型，参数类型：" +
                            parameter.getType() + "，值：" + value);
                }
            case BOOLEAN:
                Boolean bool = Convert.toBool(value);
                if (bool == null) {
                    throw new ParamValueCaseException("无法将此参数值转换成对应的类型，参数类型：" +
                            parameter.getType() + "，值：" + value);
                }
                return bool;
            default:
                throw new IllegalStateException("不支持该参数类型的转换：" + parameter.getType());
        }
    }

    private Number convertToNumber(String strValue) {
        if (StrUtil.isBlank(strValue)) {
            return 0;
        }
        strValue = strValue.replace(",", "");
        try {
            if (strValue.matches("-?\\d+")) {
                return Long.parseLong(strValue);
            }
            if (strValue.matches("-?\\d+\\.\\d*")) {
                return Double.parseDouble(strValue);
            }
            return Double.parseDouble(strValue);
        } catch (NumberFormatException e) {
            throw new ParamValueCaseException("无法将此参数值转换成数字类型，参数值：" + strValue);
        }
    }

    /**
     * 获取表单字段值，如果获取的是明细字段，则返回 List ，即该字段的所有明细数据
     *
     */
    private Object getFormFieldValue(ResourceType resourceType, InterfaceParameter parameter,
                                     int dataSourceId, String tableName, AssignmentValue value) {
        if (resourceType != ResourceType.WORKFLOW && resourceType != ResourceType.MODE) {
            throw new IllegalStateException("获取表单字段值出错，不支持的数据源类型：" + resourceType);
        }
        WorkflowFieldValue workflowField = value.getWorkflowField();
        if (workflowField == null) {
            throw new ConfigurationException("此参数未选择表单字段，参数id：" + parameter.getId() + "参数名称：" +
                    parameter.getName());
        }
        String fieldName = workflowField.getFieldName();
        // 获取主表字段值
        if (workflowField.getIsMainTable()) {
            if (resourceType == ResourceType.WORKFLOW) {
                return workflowFieldValueManager.getFieldValueByFieldName(dataSourceId, fieldName);
            }
            return modeFieldValueFetcher.getMainTableFieldValue(tableName, dataSourceId, fieldName);
        }
        // 获取明细数据
        Integer detailTableNum = workflowField.getDetailTableNum();
        Objects.requireNonNull(detailTableNum, "明细表不能为空");
        if (detailTableNum < 1) {
            throw new ConfigurationException("明细表序号不正确，不能小于1，当前明细表序号：" + detailTableNum);
        }
        List<Map<String, String>> detailFieldValues;
        if (resourceType == ResourceType.WORKFLOW) {
            detailFieldValues = workflowFieldValueManager.getDetailFields(dataSourceId, detailTableNum,
                    CollUtil.toList(fieldName));
        } else {
            detailFieldValues = modeFieldValueFetcher.getDetailFields(tableName, dataSourceId, detailTableNum,
                    CollUtil.toList(fieldName));
        }
        return detailFieldValues.stream()
                .map(detailFieldValue -> detailFieldValue.get(fieldName))
                .collect(Collectors.toList());
    }

}
