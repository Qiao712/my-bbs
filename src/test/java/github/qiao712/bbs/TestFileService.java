package github.qiao712.bbs;

import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.service.impl.AliOSSFileServiceImpl;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestFileService {
    @Autowired
    private AliOSSFileServiceImpl fileService;

    @Test
    public void testService(){
//        fileService.deleteFile(13L);
//        String filepathFromUrl = fileService.getFilepathFromUrl("https://qiao712-my-bbs.oss-cn-beijing.aliyuncs.com/user_avatar/16ca96c8-f82d-490a-95a6-234f82f3f964.png");
//        System.out.println(filepathFromUrl);

        FileIdentity fileIdentity = fileService.getFileIdentityByUrl("https://qiao712-my-bbs.oss-cn-beijing.aliyuncs.com/user_avatar/16ca96c8-f82d-490a-95a6-234f82f3f964.png");
        System.out.println(fileIdentity);
    }

    @Test
    public void testDeleteTemporaryFile(){
        fileService.clearTemporaryFile();
    }
}
