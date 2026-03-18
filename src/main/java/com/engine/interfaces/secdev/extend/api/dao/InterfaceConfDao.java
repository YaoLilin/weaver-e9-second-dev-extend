package com.engine.interfaces.secdev.extend.api.dao;

import cn.hutool.core.convert.Convert;
import com.customization.yll.common.exception.SqlExecuteException;
import com.customization.yll.common.util.ModeUtil;
import com.customization.yll.common.util.SqlUtil;
import com.engine.interfaces.secdev.extend.api.constants.ModeTableInfo;
import com.engine.interfaces.secdev.extend.api.domain.param.InterfaceConfParam;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author 姚礼林
 * @desc 接口配置 Dao
 * @date 2026/1/29
 **/
public class InterfaceConfDao {
    private static final String FIELD_API_ID = "api_id";
    private static final String FIELD_METHOD = "method";
    private static final String FIELD_BODY_TYPE = "body_type";
    private final RecordSet recordSet;

    public InterfaceConfDao(RecordSet recordSet) {
        this.recordSet = recordSet;
    }

    public InterfaceConfRecord getConfById(Integer confId) {
        String sql = "select api_id,name,url,method,body_type from " + ModeTableInfo.API_CONF_TABLE_NAME + " where id=?";
        if (!recordSet.executeQuery(sql, confId)) {
            throw new SqlExecuteException(buildSqlMessage(sql, confId), sql);
        }
        if (!recordSet.next()) {
            return null;
        }
        InterfaceConfRecord confRecord = new InterfaceConfRecord();
        confRecord.setId(confId);
        confRecord.setApiId(recordSet.getString(FIELD_API_ID));
        confRecord.setName(recordSet.getString("name"));
        confRecord.setUrl(recordSet.getString("url"));
        confRecord.setMethod(Convert.toInt(recordSet.getString(FIELD_METHOD), 0));
        confRecord.setBodyType(Convert.toInt(recordSet.getString(FIELD_BODY_TYPE), 0));
        return confRecord;
    }

    public List<InterfaceParamRecord> listParamRecords(Integer apiId) {
        String sql = "select position,api_id,param_id,parent_id,name,show_name,type,required "
            + "from " + ModeTableInfo.API_PARAM_TABLE_NAME + " where api_id=?";
        if (!recordSet.executeQuery(sql, apiId)) {
            throw new SqlExecuteException(buildSqlMessage(sql, apiId), sql);
        }
        List<InterfaceParamRecord> records = new ArrayList<>();
        while (recordSet.next()) {
            InterfaceParamRecord paramRecord = new InterfaceParamRecord();
            paramRecord.setPosition(recordSet.getInt("position"));
            paramRecord.setParamId(recordSet.getString("param_id"));
            paramRecord.setParentId(recordSet.getString("parent_id"));
            paramRecord.setName(recordSet.getString("name"));
            paramRecord.setShowName(recordSet.getString("show_name"));
            paramRecord.setType(Convert.toInt(recordSet.getString("type"), 0));
            paramRecord.setRequired(recordSet.getInt("required"));
            records.add(paramRecord);
        }
        return records;
    }

    public Optional<Integer> findConfIdByApiId(String apiId) {
        String sql = "select id from " + ModeTableInfo.API_CONF_TABLE_NAME + " where " + FIELD_API_ID + "=?";
        if (!recordSet.executeQuery(sql, apiId)) {
            throw new SqlExecuteException(buildSqlMessage(sql, apiId), sql);
        }
        if (recordSet.next()) {
            return Optional.of(recordSet.getInt("id"));
        }
        return Optional.empty();
    }

    public Integer insertConf(InterfaceConfParam param) {
        Map<String, Object> fieldData = getSaveData(param);
        if (!ModeUtil.insertToMode(fieldData, ModeTableInfo.API_CONF_TABLE_NAME, recordSet)) {
            throw new SqlExecuteException("插入接口配置信息失败");
        }
        return findConfIdByApiId(param.getApiId())
            .orElseThrow(() -> new SqlExecuteException("获取接口配置id失败"));
    }

    public void updateConf(Integer id, InterfaceConfParam param) {
        Map<String, Object> fieldData = getSaveData(param);
        fieldData.remove(FIELD_API_ID);
        if (!ModeUtil.updateMode(fieldData, id, ModeTableInfo.API_CONF_TABLE_NAME, recordSet)) {
            throw new SqlExecuteException("更新接口配置信息失败");
        }
    }

    public void deleteParameters(Integer apiId) {
        String sql = "delete from " + ModeTableInfo.API_PARAM_TABLE_NAME + " where api_id = ?";
        if (!recordSet.executeUpdate(sql, apiId)) {
            throw new SqlExecuteException(buildSqlMessage(sql, apiId), sql);
        }
    }

    public void insertParameter(Integer apiConfId, int position, String parentId,
                                InterfaceConfParam.InterfaceParameter param) {
        Map<String, Object> fieldData = getInsertData(apiConfId, position, parentId, param);
        if (!ModeUtil.insertToMode(fieldData, ModeTableInfo.API_PARAM_TABLE_NAME, recordSet)) {
            throw new SqlExecuteException("插入接口参数失败");
        }
    }

    public int countByCondition(String whereSql, List<Object> params) {
        String countSql = "select count(1) as total from " + ModeTableInfo.API_CONF_TABLE_NAME + whereSql;
        Object[] queryParams = params.toArray();
        if (!recordSet.executeQuery(countSql, queryParams)) {
            throw new SqlExecuteException(buildSqlMessage(countSql, queryParams), countSql);
        }
        if (recordSet.next()) {
            return Convert.toInt(recordSet.getString("total"), 0);
        }
        return 0;
    }

    public List<InterfaceListRecord> listByCondition(String whereSql, List<Object> params, int offset, int limit) {
        String baseSql = "select id,api_id,url,method,name from " + ModeTableInfo.API_CONF_TABLE_NAME + whereSql;
        SqlUtil.SqlPageResult pageResult = SqlUtil.buildPageSql(
            recordSet.getDBType(),
            baseSql,
            "order by id desc",
            offset,
            limit
        );
        List<Object> queryParams = new ArrayList<>(params.size() + pageResult.getParams().size());
        queryParams.addAll(params);
        queryParams.addAll(pageResult.getParams());
        Object[] executeParams = queryParams.toArray();
        if (!recordSet.executeQuery(pageResult.getSql(), executeParams)) {
            throw new SqlExecuteException(buildSqlMessage(pageResult.getSql(), executeParams), pageResult.getSql());
        }
        List<InterfaceListRecord> result = new ArrayList<>();
        while (recordSet.next()) {
            InterfaceListRecord listRecord = new InterfaceListRecord();
            listRecord.setId(Convert.toInt(recordSet.getString("id"), 0));
            listRecord.setApiId(recordSet.getString(FIELD_API_ID));
            listRecord.setUrl(recordSet.getString("url"));
            listRecord.setMethod(Convert.toInt(recordSet.getString(FIELD_METHOD), 0));
            listRecord.setName(recordSet.getString("name"));
            result.add(listRecord);
        }
        return result;
    }

    private Map<String, Object> getSaveData(InterfaceConfParam param) {
        Map<String, Object> fieldData = new HashMap<>(5);
        fieldData.put(FIELD_API_ID, param.getApiId());
        fieldData.put("url", param.getUrl());
        fieldData.put("name", param.getName());
        fieldData.put(FIELD_METHOD, param.getMethod());
        fieldData.put(FIELD_BODY_TYPE, param.getBodyType());
        return fieldData;
    }

    private Map<String, Object> getInsertData(int apiConfId, int position, String parentId,
                                              InterfaceConfParam.InterfaceParameter param) {
        Map<String, Object> fieldData = new HashMap<>(10);
        fieldData.put("param_id", param.getId());
        fieldData.put("parent_id", parentId);
        fieldData.put("name", param.getName());
        fieldData.put("show_name", param.getShowName());
        fieldData.put("type", param.getType());
        fieldData.put("required", param.getRequired());
        fieldData.put("position", position);
        fieldData.put(FIELD_API_ID, apiConfId);
        return fieldData;
    }

    private String buildSqlMessage(String sql, Object... params) {
        return "执行sql失败，sql: " + sql + "，params: " + Arrays.toString(params);
    }

    public static class InterfaceConfRecord {
        private Integer id;
        private String apiId;
        private String name;
        private String url;
        private Integer method;
        private Integer bodyType;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getApiId() {
            return apiId;
        }

        public void setApiId(String apiId) {
            this.apiId = apiId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Integer getMethod() {
            return method;
        }

        public void setMethod(Integer method) {
            this.method = method;
        }

        public Integer getBodyType() {
            return bodyType;
        }

        public void setBodyType(Integer bodyType) {
            this.bodyType = bodyType;
        }
    }

    public static class InterfaceListRecord {
        private Integer id;
        private String apiId;
        private String url;
        private Integer method;
        private String name;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getApiId() {
            return apiId;
        }

        public void setApiId(String apiId) {
            this.apiId = apiId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Integer getMethod() {
            return method;
        }

        public void setMethod(Integer method) {
            this.method = method;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class InterfaceParamRecord {
        private Integer position;
        private String paramId;
        private String parentId;
        private String name;
        private String showName;
        private Integer type;
        private Integer required;

        public Integer getPosition() {
            return position;
        }

        public void setPosition(Integer position) {
            this.position = position;
        }

        public String getParamId() {
            return paramId;
        }

        public void setParamId(String paramId) {
            this.paramId = paramId;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getShowName() {
            return showName;
        }

        public void setShowName(String showName) {
            this.showName = showName;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public Integer getRequired() {
            return required;
        }

        public void setRequired(Integer required) {
            this.required = required;
        }
    }
}
