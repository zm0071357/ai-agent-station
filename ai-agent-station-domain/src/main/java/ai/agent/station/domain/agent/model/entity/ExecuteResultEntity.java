package ai.agent.station.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阶段处理结果实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteResultEntity {

    /**
     * 数据类型：analysis(分析阶段), execution(执行阶段), supervision(监督阶段), summary(总结阶段), error(错误信息), complete(完成标识)
     * 细分类型：analysis_status(任务状态分析), analysis_history(执行历史评估), analysis_strategy(下一步策略), analysis_progress(完成度评估)
     *          execution_target(执行目标), execution_process(执行过程), execution_result(执行结果), execution_quality(质量检查)
     *          supervision_assessment(质量评估), supervision_issues(问题识别), supervision_suggestions(改进建议), supervision_score(质量评分)
     *          supervision_pass(是否通过)
     */
    private String type;

    /**
     * 子类型标识，用于前端细粒度展示 - 细分类型
     */
    private String subType;

    /**
     * 当前执行步数
     */
    private Integer currentStep;

    /**
     * 发送内容
     */
    private String content;

    /**
     * 是否完成
     */
    private Boolean completed;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 创建分析阶段结果
     */
    public static ExecuteResultEntity createAnalysisResult(Integer currentStep, String content, String userId) {
        return ExecuteResultEntity.builder()
                .type("analysis")
                .currentStep(currentStep)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .build();
    }

    /**
     * 创建分析阶段细分结果
     */
    public static ExecuteResultEntity createAnalysisSubResult(Integer currentStep, String subType, String content, String userId) {
        return ExecuteResultEntity.builder()
                .type("analysis")
                .subType(subType)
                .currentStep(currentStep)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .build();
    }

    /**
     * 创建执行阶段结果
     */
    public static ExecuteResultEntity createExecutionResult(Integer currentStep, String content, String userId) {
        return ExecuteResultEntity.builder()
                .type("execution")
                .currentStep(currentStep)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .build();
    }

    /**
     * 创建执行阶段细分结果
     */
    public static ExecuteResultEntity createExecutionSubResult(Integer currentStep, String subType, String content, String userId) {
        return ExecuteResultEntity.builder()
                .type("execution")
                .subType(subType)
                .currentStep(currentStep)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .build();
    }

    /**
     * 创建监督阶段结果
     */
    public static ExecuteResultEntity createSupervisionResult(Integer currentStep, String content, String userId) {
        return ExecuteResultEntity.builder()
                .type("supervision")
                .currentStep(currentStep)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .build();
    }

    /**
     * 创建监督阶段细分结果
     */
    public static ExecuteResultEntity createSupervisionSubResult(Integer currentStep, String subType, String content, String userId) {
        return ExecuteResultEntity.builder()
                .type("supervision")
                .subType(subType)
                .currentStep(currentStep)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .build();
    }

    /**
     * 创建总结阶段细分的结果
     */
    public static ExecuteResultEntity createSummarySubResult(String subType, String content, String userId) {
        return ExecuteResultEntity.builder()
                .type("summary")
                .subType(subType)
                .currentStep(4)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .build();
    }

    /**
     * 创建总结阶段结果
     */
    public static ExecuteResultEntity createSummaryResult(String content, String userId) {
        return ExecuteResultEntity.builder()
                .type("summary")
                .currentStep(null)
                .content(content)
                .completed(true)
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .build();
    }

    /**
     * 创建错误结果
     */
    public static ExecuteResultEntity createErrorResult(String content, String userId) {
        return ExecuteResultEntity.builder()
                .type("error")
                .currentStep(null)
                .content(content)
                .completed(true)
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .build();
    }

    /**
     * 创建完成标识
     */
    public static ExecuteResultEntity createCompleteResult(String userId) {
        return ExecuteResultEntity.builder()
                .type("complete")
                .currentStep(null)
                .content("执行完成")
                .completed(true)
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .build();
    }

}
