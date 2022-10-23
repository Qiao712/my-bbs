package github.qiao712.bbs.websocket;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.PrivateMessageDto;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.security.TokenManager;
import github.qiao712.bbs.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@Component
@Slf4j
@ServerEndpoint("/api/chat/{token}")
public class ChatServerEndpoint {
    private static TokenManager tokenManager;
    private static ChatService chatService;

    //封装后的Session与用户信息
    private ChatChannel channel;

    /**
     * 用于注入TokenManager到静态变量
     * 每次连接，都会生成一个该类对象，所以不能使用@Autowaired注入普通字段，只能使用静态变量
     */
    @Autowired
    public void setTokenManager(TokenManager tokenManager){
        ChatServerEndpoint.tokenManager = tokenManager;
    }
    @Autowired
    public void setChatService(ChatService chatService){
        ChatServerEndpoint.chatService = chatService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token){
        //设置会话
//        session.setMaxBinaryMessageBufferSize();
//        session.setMaxIdleTimeout();

        //获取用户登录信息
        AuthUser user = tokenManager.getUser(token);
        if(user == null){
            try {
                session.close();
            } catch (IOException e) {
                log.error("Websocket Close:", e);
            }
            return;
        }

        this.channel = new ChatChannel(session, user.getId());
        chatService.addChannel(channel);
    }

    @OnClose
    public void onClose(){
        chatService.removeChannel(channel);
    }

    @OnMessage
    public void onMessage(String message){
        try {
            PrivateMessageDto privateMessageDto = JSON.parseObject(message, PrivateMessageDto.class);
            chatService.receiveMessage(channel, privateMessageDto);
        }catch (ServiceException e){
            //返回错误提示
            channel.send(Result.fail(e.getMessage()));
        }
    }

    @OnError
    public void onError(Throwable e){
        //未知异常
        log.error("ChatService:", e);
    }
}
