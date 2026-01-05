package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;

import java.util.List;
import java.util.Map;

/**
 * 题目Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础的CRUD操作
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    // 可以在这里添加自定义的查询方法
    
    /**
     * 获取每个分类的题目数量
     * @return 包含分类ID和题目数量的结果列表
     */
    @Select("SELECT category_id, COUNT(*) as count FROM questions GROUP BY category_id")
    @Results({
        @Result(property = "categoryId", column = "category_id"),
        @Result(property = "count", column = "count")
    })
    List<Map<String, Object>> getCategoryQuestionCount();
} 