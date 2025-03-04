package com.js.jsojbackendmodel.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author sakisaki
 * @date 2025/3/4 19:25
 */
@Data
public class QuestionDeleteRequest implements Serializable {

    private static final long serialVersionUID = -4593113949927809608L;

    private List<Long> questionIds;
}
