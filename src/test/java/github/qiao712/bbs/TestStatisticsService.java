package github.qiao712.bbs;

import github.qiao712.bbs.domain.entity.Question;
import github.qiao712.bbs.mapper.QuestionMapper;
import github.qiao712.bbs.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class TestStatisticsService {
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private QuestionMapper questionMapper;

    @Test
    public void testSyncQuestionViewCount(){
        statisticsService.syncQuestionViewCount();
    }

    @Test
    public void testListQuestionViewCounts(){
        List<Question> questions = questionMapper.selectList(null);
        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        List<Long> counts = statisticsService.listQuestionViewCounts(questionIds);
        for (Long count : counts) {
            System.out.println(count);
        }
    }

    @Test
    public void testRefreshQuestionScores(){
        statisticsService.refreshQuestionScores();
    }


}
