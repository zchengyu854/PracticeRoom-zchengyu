package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.entity.ExamRecord;
import com.exam.service.ExamRecordService;
import com.exam.service.PaperService;
import com.exam.service.KimiGradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
@Tag(name = "AI学习分析", description = "AI智能学习分析相关接口")
public class AnalysisController {

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private PaperService paperService;

    @Autowired
    private KimiGradingService kimiGradingService;

    @GetMapping("/student/{studentName}")
    @Operation(summary = "获取学生学习分析", description = "根据学生姓名获取详细的学习分析数据")
    public Result<Map<String, Object>> getStudentAnalysis(
            @Parameter(description = "学生姓名") @PathVariable String studentName
    ) {
        try {
            QueryWrapper<ExamRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("student_name", studentName);
            wrapper.orderByDesc("create_time");
            List<ExamRecord> records = examRecordService.list(wrapper);

            Map<String, Object> analysis = new HashMap<>();
            analysis.put("studentName", studentName);
            analysis.put("totalExams", records.size());

            if (records.isEmpty()) {
                analysis.put("avgScore", 0);
                analysis.put("maxScore", 0);
                analysis.put("minScore", 0);
                analysis.put("recentScores", new ArrayList<>());
                analysis.put("passRate", 0);
                return Result.success(analysis);
            }

            int totalScore = 0;
            int maxScore = 0;
            int minScore = Integer.MAX_VALUE;
            int passCount = 0;
            List<Map<String, Object>> recentScores = new ArrayList<>();

            for (ExamRecord record : records) {
                totalScore += record.getScore();
                maxScore = Math.max(maxScore, record.getScore());
                minScore = Math.min(minScore, record.getScore());
                if (record.getScore() >= 60) {
                    passCount++;
                }

                Map<String, Object> scoreInfo = new HashMap<>();
                scoreInfo.put("examId", record.getExamId());
                scoreInfo.put("score", record.getScore());
                scoreInfo.put("createTime", record.getCreateTime());
                recentScores.add(scoreInfo);
            }

            double avgScore = (double) totalScore / records.size();
            double passRate = (double) passCount / records.size() * 100;

            analysis.put("avgScore", Math.round(avgScore * 10) / 10.0);
            analysis.put("maxScore", maxScore);
            analysis.put("minScore", minScore);
            analysis.put("passRate", Math.round(passRate * 10) / 10.0);
            analysis.put("recentScores", recentScores);

            return Result.success(analysis);
        } catch (Exception e) {
            log.error("获取学生学习分析失败", e);
            return Result.error("获取学习分析失败: " + e.getMessage());
        }
    }

    @GetMapping("/ai-suggestion/{studentName}")
    @Operation(summary = "获取AI学习建议", description = "基于学生的考试表现，使用AI生成个性化学习建议")
    public Result<String> getAISuggestion(
            @Parameter(description = "学生姓名") @PathVariable String studentName
    ) {
        try {
            QueryWrapper<ExamRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("student_name", studentName);
            wrapper.eq("status", "已批阅");
            wrapper.orderByDesc("create_time");
            wrapper.last("LIMIT 10");
            List<ExamRecord> records = examRecordService.list(wrapper);

            if (records.isEmpty()) {
                return Result.success("暂无考试数据，请先参加考试后获取学习建议。");
            }

            int totalScore = records.stream().mapToInt(ExamRecord::getScore).sum();
            int avgScore = totalScore / records.size();
            int passCount = (int) records.stream().filter(r -> r.getScore() >= 60).count();
            double passRate = (double) passCount / records.size() * 100;

            int[] recentScores = records.stream().limit(5).mapToInt(ExamRecord::getScore).toArray();
            boolean isImproving = true;
            for (int i = 1; i < recentScores.length; i++) {
                if (recentScores[i] < recentScores[i - 1]) {
                    isImproving = false;
                    break;
                }
            }

            StringBuilder prompt = new StringBuilder();
            prompt.append("你是一名专业的教育顾问，请为以下学生提供个性化学习建议：\n\n");
            prompt.append("【学生信息】\n");
            prompt.append("姓名：").append(studentName).append("\n");
            prompt.append("最近10次考试平均分：").append(avgScore).append("\n");
            prompt.append("及格率：").append(String.format("%.1f%%", passRate)).append("\n");
            prompt.append("最近5次成绩：").append(Arrays.toString(recentScores)).append("\n");
            prompt.append("成绩趋势：").append(isImproving ? "呈上升趋势" : "波动较大或呈下降趋势").append("\n\n");

            prompt.append("【要求】\n");
            prompt.append("1. 提供一份200-300字的个性化学习建议\n");
            prompt.append("2. 肯定学生的进步，指出存在的问题\n");
            prompt.append("3. 给出3-5条具体的改进建议\n");
            prompt.append("4. 鼓励学生继续努力学习\n\n");
            prompt.append("请直接返回建议内容，无需特殊格式。");

            String suggestion = kimiGradingService.generateExamSummary(
                    avgScore, 100, records.size(), passCount
            );

            return Result.success(suggestion);
        } catch (Exception e) {
            log.error("获取AI学习建议失败", e);
            return Result.error("获取AI学习建议失败: " + e.getMessage());
        }
    }

    @GetMapping("/mistake-analysis/{studentName}")
    @Operation(summary = "获取错题分析", description = "分析学生的错题情况")
    public Result<Map<String, Object>> getMistakeAnalysis(
            @Parameter(description = "学生姓名") @PathVariable String studentName,
            @Parameter(description = "试卷ID") @RequestParam(required = false) Integer paperId
    ) {
        try {
            QueryWrapper<ExamRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("student_name", studentName);
            wrapper.eq("status", "已批阅");
            if (paperId != null) {
                wrapper.eq("exam_id", paperId);
            }
            wrapper.orderByDesc("create_time");
            wrapper.last("LIMIT 5");
            List<ExamRecord> records = examRecordService.list(wrapper);

            Map<String, Object> analysis = new HashMap<>();
            analysis.put("studentName", studentName);

            if (records.isEmpty()) {
                analysis.put("totalMistakes", 0);
                analysis.put("mistakeDetails", new ArrayList<>());
                return Result.success(analysis);
            }

            int totalMistakes = 0;
            List<Map<String, Object>> mistakeDetails = new ArrayList<>();

            for (ExamRecord record : records) {
                record.setPaper(paperService.getPaperWithQuestions(record.getExamId()));
                List<com.exam.entity.AnswerRecord> answerRecords = record.getAnswerRecords();
                if (answerRecords != null) {
                    for (com.exam.entity.AnswerRecord ar : answerRecords) {
                        if (ar.getScore() != null && ar.getScore() == 0) {
                            totalMistakes++;
                            Map<String, Object> mistake = new HashMap<>();
                            mistake.put("questionId", ar.getQuestionId());
                            mistake.put("questionTitle", getQuestionTitle(record, ar.getQuestionId()));
                            mistake.put("userAnswer", ar.getUserAnswer());
                            mistake.put("aiCorrection", ar.getAiCorrection());
                            mistakeDetails.add(mistake);
                        }
                    }
                }
            }

            analysis.put("totalMistakes", totalMistakes);
            analysis.put("mistakeDetails", mistakeDetails);

            return Result.success(analysis);
        } catch (Exception e) {
            log.error("获取错题分析失败", e);
            return Result.error("获取错题分析失败: " + e.getMessage());
        }
    }

    private String getQuestionTitle(ExamRecord record, Integer questionId) {
        if (record.getPaper() != null && record.getPaper().getQuestions() != null) {
            return record.getPaper().getQuestions().stream()
                    .filter(q -> q.getId().equals(questionId.longValue()))
                    .findFirst()
                    .map(com.exam.entity.Question::getTitle)
                    .orElse("未知题目");
        }
        return "未知题目";
    }

    @GetMapping("/overview")
    @Operation(summary = "获取学习概览", description = "获取整体学习情况概览")
    public Result<Map<String, Object>> getOverview(
            @Parameter(description = "学生姓名") @RequestParam(required = false) String studentName
    ) {
        try {
            Map<String, Object> overview = new HashMap<>();

            QueryWrapper<ExamRecord> wrapper = new QueryWrapper<>();
            if (studentName != null && !studentName.isEmpty()) {
                wrapper.eq("student_name", studentName);
            }

            long totalExams = examRecordService.count(wrapper);
            overview.put("totalExams", totalExams);

            wrapper.eq("status", "已批阅");
            long finishedExams = examRecordService.count(wrapper);
            overview.put("finishedExams", finishedExams);

            if (finishedExams > 0) {
                List<ExamRecord> records = examRecordService.list(wrapper);
                double avgScore = records.stream().mapToInt(ExamRecord::getScore).average().orElse(0);
                overview.put("avgScore", Math.round(avgScore * 10) / 10.0);

                int passCount = (int) records.stream().filter(r -> r.getScore() >= 60).count();
                double passRate = (double) passCount / records.size() * 100;
                overview.put("passRate", Math.round(passRate * 10) / 10.0);

                long excellentCount = records.stream().filter(r -> r.getScore() >= 90).count();
                double excellentRate = (double) excellentCount / records.size() * 100;
                overview.put("excellentRate", Math.round(excellentRate * 10) / 10.0);
            } else {
                overview.put("avgScore", 0);
                overview.put("passRate", 0);
                overview.put("excellentRate", 0);
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekAgo = now.minusDays(7);
            LocalDateTime monthAgo = now.minusDays(30);

            QueryWrapper<ExamRecord> weekWrapper = new QueryWrapper<>();
            if (studentName != null && !studentName.isEmpty()) {
                weekWrapper.eq("student_name", studentName);
            }
            weekWrapper.ge("create_time", weekAgo);
            long weekExams = examRecordService.count(weekWrapper);
            overview.put("weekExams", weekExams);

            QueryWrapper<ExamRecord> monthWrapper = new QueryWrapper<>();
            if (studentName != null && !studentName.isEmpty()) {
                monthWrapper.eq("student_name", studentName);
            }
            monthWrapper.ge("create_time", monthAgo);
            long monthExams = examRecordService.count(monthWrapper);
            overview.put("monthExams", monthExams);

            return Result.success(overview);
        } catch (Exception e) {
            log.error("获取学习概览失败", e);
            return Result.error("获取学习概览失败: " + e.getMessage());
        }
    }
}
