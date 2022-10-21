package github.qiao712.bbs.websocket;

import com.alibaba.fastjson.JSON;
import lombok.Getter;

import javax.websocket.Session;
import java.io.Closeable;
import java.io.IOException;

/**
 * 封装WebSocket的Session
 */
@Getter
public class ChatChannel implements Closeable {
    private final Session session;
    private final Long userId;

    public ChatChannel(Session session, Long userId) {
        this.session = session;
        this.userId = userId;
    }

    /**
     * 将对象序号化为JSON格式，并异步发送
     */
    public void send(Object object){
        session.getAsyncRemote().sendText(JSON.toJSONString(object));
    }

    @Override
    public void close() throws IOException {
        session.close();
    }
}
