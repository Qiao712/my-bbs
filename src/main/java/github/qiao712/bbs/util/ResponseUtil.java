package github.qiao712.bbs.util;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.domain.base.Result;

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
