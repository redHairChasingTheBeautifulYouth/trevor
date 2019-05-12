package com.trevor.web.websocket.niuniu;

import com.trevor.bo.WebKeys;
import com.trevor.common.MessageCodeEnum;
import com.trevor.domain.User;
import com.trevor.util.WebsocketUtil;
import com.trevor.web.websocket.config.NiuniuServerConfigurator;
import com.trevor.web.websocket.decoder.MessageDecoder;
import com.trevor.web.websocket.encoder.MessageEncoder;
import com.trevor.websocket.bo.ReceiveMessage;
import com.trevor.websocket.bo.ReturnMessage;
import com.trevor.websocket.bo.SocketUser;
import com.trevor.websocket.niuniu.NiuniuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * 一句话描述该类作用:【牛牛服务端,每次建立链接就新建了一个对象】
 *
 * @author: trevor
 * @create: 2019-03-05 22:29
 **/
@ServerEndpoint(
            value = "/niuniu/{rooId}",
        configurator = NiuniuServerConfigurator.class,
        encoders= {MessageEncoder.class},
        decoders = {MessageDecoder.class}
        )
@Component
@Slf4j
public class NiuniuServer {


    private static NiuniuService niuniuService;

    @Resource
    public void setNiuniuService (NiuniuService niuniuService) {
        NiuniuServer.niuniuService = niuniuService;
    }
    
    private static Map<Long , CopyOnWriteArrayList<Session>> sessions;

    @Resource(name = "niuniuRooms")
    public void setSessions (Map<Long , CopyOnWriteArrayList<Session>> sessions) {
        NiuniuServer.sessions = sessions;
    }

    private Session mySession;

    private HttpSession httpSession;



    /**
     * 连接时调用
     * @param session
     * @param config
     * @param rooId
     */
    @OnOpen
    public void onOpen(Session session , EndpointConfig config , @PathParam("rooId") String rooId) throws IOException, EncodeException {
        mySession = session;
        httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        User user = (User) httpSession.getAttribute(WebKeys.SESSION_USER_KEY);
        if (user == null) {
            WebsocketUtil.sendBasicMessage(mySession , new ReturnMessage(MessageCodeEnum.SESSION_TIRED));
            mySession.close();
            return;
        }
        String tempRoomId = rooId.intern();
        ReturnMessage<SocketUser> returnMessage;
        synchronized (tempRoomId) {
            returnMessage = niuniuService.onOpenCheck(rooId, user);
            if (returnMessage.getMessageCode() > 0) {
                sessions.get(Long.valueOf(rooId)).add(mySession);
            }
        }
        if (returnMessage.getMessageCode() < 0) {
            WebsocketUtil.sendBasicMessage(mySession , returnMessage);
            mySession.close();
        }else {
            mySession.getUserProperties().put(WebKeys.WEBSOCKET_USER_KEY ,returnMessage.getData());
            WebsocketUtil.sendAllBasicMessage(sessions.get(Long.valueOf(rooId)) ,returnMessage);
        }
    }

    @OnMessage
    public void receiveMsg(@PathParam("rooId") String roomId, ReceiveMessage receiveMessage) throws InterruptedException, EncodeException, IOException {
        Integer messageCode = receiveMessage.getMessageCode();
        SocketUser socketUser = (SocketUser) mySession.getUserProperties().get(WebKeys.WEBSOCKET_USER_KEY);
        Long roomIdNum = Long.valueOf(roomId);
        if (Objects.equals(messageCode , MessageCodeEnum.READY.getCode())) {
            niuniuService.dealReadyMessage( socketUser,roomIdNum);
        }else if (Objects.equals(messageCode ,MessageCodeEnum.QIANG_ZHUANG.getCode())) {
            niuniuService.dealQiangZhuangMessage(socketUser ,roomIdNum ,receiveMessage);
        }else if (Objects.equals(messageCode ,MessageCodeEnum.XIAN_JIA_XIA_ZHU.getCode())) {
            niuniuService.dealXianJiaXiaZhuMessage(socketUser ,roomIdNum ,receiveMessage);
        }
    }

    @OnClose
    public void disConnect(@PathParam("rooId") String roomName, Session session) {
        if (sessions.containsKey(Long.valueOf(roomName))) {
            sessions.get(roomName).remove(session);
            log.info("断开连接");
        }
    }

    @OnError
    public void onError(Throwable t){
        log.error(t.getMessage());
    }

}
