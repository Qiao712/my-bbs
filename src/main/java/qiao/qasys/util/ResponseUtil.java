package qiao.qasys.util;

import com.alibaba.fastjson.JSON;
import qiao.qasys.common.Result;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseUtil {
    public static void response(HttpServletResponse response, Result<?> result) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");
        response.getWriter().write(JSON.toJSONString(result));
        response.getWriter().flush();
    }
}
