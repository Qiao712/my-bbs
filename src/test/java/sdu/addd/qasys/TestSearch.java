package sdu.addd.qasys;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sdu.addd.qasys.service.SearchService;

@SpringBootTest
public class TestSearch {
    @Autowired
    private SearchService searchService;

    @Test
    void sync(){
        searchService.syncAllQuestion();
    }
}
