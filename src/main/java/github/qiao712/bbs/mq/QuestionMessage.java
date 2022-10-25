package github.qiao712.bbs.mq;

import github.qiao712.bbs.domain.entity.Question;
import lombok.Data;

@Data
public class QuestionMessage {
    public enum QuestionMessageType {
        CREATE,
        DELETE,
        UPDATE
    }

    private Question question;
    private Long questionId;
    private QuestionMessageType questionMessageType;

    public static QuestionMessage buildQuestionAddMessage(Question question){
        QuestionMessage questionMessage = new QuestionMessage();
        questionMessage.questionMessageType = QuestionMessageType.CREATE;
        questionMessage.question = question;
        return questionMessage;
    }

    public static QuestionMessage buildQuestionDeleteMessage(Long questionId){
        QuestionMessage questionMessage = new QuestionMessage();
        questionMessage.questionMessageType = QuestionMessageType.DELETE;
        questionMessage.questionId = questionId;
        return questionMessage;
    }

    public static QuestionMessage buildQuestionUpdateMessage(Question question){
        QuestionMessage questionMessage = new QuestionMessage();
        questionMessage.questionMessageType = QuestionMessageType.UPDATE;
        questionMessage.question = question;
        return questionMessage;
    }
}
