package qiao.qasys.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtil {
    private final static Pattern pattern = Pattern.compile("<img.*? src=\"");    //匹配开头位置，再寻找下一个引号    .*加? 非贪心地匹配

    /**
     * 获取html中引用的图片的url
     */
    public static List<String> getImageUrls(String html){
        List<String> urls = new ArrayList<>();

        //遍历img标签中的src属性的内容
        Matcher matcher = pattern.matcher(html);
        int start, end;
        while(matcher.find()){
            start = matcher.end();                  //<img ... src=" 的结束即连接地址的开始
            end = html.indexOf('\"', start);
            urls.add(html.substring(start, end));
        }

        return urls;
    }

    /**
     * 获取html中所有的Text
     * (“<>”外的内容)
     */
    public static String getText(String html){
        StringBuilder builder = new StringBuilder();
        boolean in = false; //是否在尖括号内

        char c;
        for(int i = 0; i<html.length(); i++){
            c = html.charAt(i);
            if(c == '<'){
                in = true;
            }else if(c == '>'){
                in = false;

                //空格分割
                if(builder.length() != 0 && builder.charAt(builder.length() - 1) != ' ')
                    builder.append(' ');
            }else{
                if(!in) builder.append(c);
            }
        }

        return builder.toString();
    }
}
