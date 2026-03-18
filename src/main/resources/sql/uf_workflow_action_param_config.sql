-- 流程Action高级参数配置表
CREATE TABLE uf_workflow_action_param_config (
    id INT IDENTITY(1,1) PRIMARY KEY,
    action_id VARCHAR(500) NOT NULL,
    parent_id INT NULL,
    param_name VARCHAR(200) NULL,
    display_name VARCHAR(200) NULL,
    required INT DEFAULT 0,
    param_type INT NULL,
    detail_table INT NULL,
    sort_order INT DEFAULT 0,
    assignment_method VARCHAR(50) NULL,
    assignment_is_main_table INT NULL,
    assignment_detail_table_num INT NULL,
    assignment_field_name VARCHAR(200) NULL,
    assignment_display_name VARCHAR(500) NULL,
    assignment_value VARCHAR(500) NULL
);

-- 创建索引
CREATE INDEX idx_action_id ON uf_workflow_action_param_config(action_id);
CREATE INDEX idx_parent_id ON uf_workflow_action_param_config(parent_id);
