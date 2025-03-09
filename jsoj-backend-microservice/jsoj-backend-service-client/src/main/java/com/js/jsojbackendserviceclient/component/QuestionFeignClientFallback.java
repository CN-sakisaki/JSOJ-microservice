package com.js.jsojbackendserviceclient.component;

import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.exception.BusinessException;
import com.js.jsojbackendmodel.entity.Question;
import com.js.jsojbackendmodel.entity.QuestionSubmit;
import com.js.jsojbackendserviceclient.service.QuestionFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Feign客户端熔断降级
 * @author sakisaki
 * @date 2025/3/9 15:46
 */
@Component
@Slf4j
public class QuestionFeignClientFallback implements QuestionFeignClient {

    @Override
    public Question getQuestionById(Long questionId) {
        return null;
    }

    @Override
    public QuestionSubmit getQuestionSubmitById(Long questionSubmitId) {
        log.error("Question服务降级: 无法获取提交记录");
        throw new BusinessException(ErrorCode.SYSTEM_BUSY);
    }

    @Override
    public boolean updateQuestionSubmitById(QuestionSubmit questionSubmit) {
        return false;
    }
}
