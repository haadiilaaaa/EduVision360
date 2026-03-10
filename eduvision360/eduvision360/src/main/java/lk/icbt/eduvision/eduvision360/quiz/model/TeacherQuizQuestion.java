package lk.icbt.eduvision.eduvision360.quiz.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherQuizQuestion {

    private String questionText;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
}