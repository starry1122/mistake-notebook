package com.wsy.mistake_notebook.service;

import com.wsy.mistake_notebook.dto.ImageWrongQuestionCreateDTO;
import com.wsy.mistake_notebook.dto.ManualWrongQuestionCreateDTO;
import com.wsy.mistake_notebook.dto.QuestionTagsUpdateDTO;
import com.wsy.mistake_notebook.dto.WrongQuestionQueryDTO;
import com.wsy.mistake_notebook.dto.WrongQuestionUpdateDTO;
import com.wsy.mistake_notebook.vo.WrongQuestionManageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface WrongQuestionService {

    Map<String, Object> createManual(Long userId, ManualWrongQuestionCreateDTO dto);

    Map<String, Object> createImage(Long userId, ImageWrongQuestionCreateDTO dto, MultipartFile file);

    void updateQuestionTags(Long userId, Long questionId, QuestionTagsUpdateDTO dto);

    Map<String, Object> listByCondition(Long userId, WrongQuestionQueryDTO dto);

    WrongQuestionManageVO getDetail(Long userId, Long questionId);

    void updateQuestion(Long userId, Long questionId, WrongQuestionUpdateDTO dto);

    void deleteQuestion(Long userId, Long questionId);

    void updateFavorite(Long userId, Long questionId, Boolean favorite);

    List<String> listSubjects(Long userId);

    List<String> listKnowledgePoints(Long userId, String subject);
}
