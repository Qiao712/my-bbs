package github.qiao712.bbs;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.qiao712.bbs.domain.entity.Conversation;
import github.qiao712.bbs.mapper.ConversationMapper;
import github.qiao712.bbs.util.HtmlUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
public class TestSomething {
    @Test
    public void testFindSrcUrl() throws IOException {
//        String html = "<p><img src=\"https://pic2.zhimg.com/80/v2-6452e1411706bb8113f8d78e49f3fcd2_720w.png\" alt=\"1\" data-href=\"1\" style=\"width: 262.00px;height: 249.83px;\"/></p> ";
        File file = new File("D://desktop//test.htm");
        FileReader fileReader = new FileReader(file);
        char[] buf = new char[(int) file.length()];
        fileReader.read(buf);
        String html = new String(buf);

//        Pattern pattern = Pattern.compile("<img(.*?)/?>");   //匹配img标签内的属性(bug: 引号内的'>'导致其提前结束)
        Pattern pattern = Pattern.compile("<img.*? src=\"");    //匹配开头位置，再寻找下一个引号    .*加? 非贪心地匹配
        Matcher matcher = pattern.matcher(html);

        int start, end;
        while(matcher.find()){
            System.out.println(matcher.start());

            start = matcher.end();  //<img ... src=" 的结束即连接地址的开始
            end = html.indexOf('\"', start);
            System.out.println(html.substring(start, end));
        }

    }

    @Test
    public void testGetTextFromHtml() throws IOException {
        File file = new File("D://desktop//test.htm");
        FileReader fileReader = new FileReader(file);
        char[] buf = new char[(int) file.length()];
        fileReader.read(buf);
        String html = new String(buf);

        System.out.println(html);
        System.out.println(HtmlUtil.getText(html));
    }

    @Autowired
    private ConversationMapper conversationMapper;

    @Test
    public void testSql(){
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Conversation::getUser1Id, 1)
                .or()
                .eq(Conversation::getUser2Id, 1);

        System.out.println(queryWrapper.getTargetSql());
    }

    public static void main(String[] args) {
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Conversation::getUser1Id, 1)
                .or()
                .eq(Conversation::getUser2Id, 1);

        System.out.println(queryWrapper.getTargetSql());
    }


    @Test
    public void testLua(){

    }
}
