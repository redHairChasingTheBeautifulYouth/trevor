package com.trevor.web.websocket.niuniu;

import com.google.common.collect.Lists;
import com.trevor.bo.*;
import com.trevor.domain.User;
import com.trevor.service.user.UserService;
import com.trevor.util.TokenUtil;
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
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


/**
 * 一句话描述该类作用:【牛牛服务端,每次建立链接就新建了一个对象】
 *
 * @author: trevor
 * @create: 2019-03-05 22:29
 **/
@ServerEndpoint(
            value = "/niuniu/{roomId}",
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
    
    private static Map<Long , CopyOnWriteArrayList<Session>> sessionsMap;

    @Resource(name = "sessionsMap")
    public void setSessions (Map<Long , CopyOnWriteArrayList<Session>> sessions) {
        NiuniuServer.sessionsMap = sessions;
    }

    private static Map<Long , RoomPoke> roomPokeMap;

    @Resource(name = "roomPokeMap")
    public void setRoomPokeMap (Map<Long , RoomPoke> roomPokeMap) {
        NiuniuServer.roomPokeMap = roomPokeMap;
    }

    private static UserService userService;

    @Resource
    public void setUserService (UserService userService) {
        NiuniuServer.userService = userService;
    }

    private Session mySession;

    @OnOpen
    public void onOpen(Session session , EndpointConfig config , @PathParam("roomId") String roomId) throws IOException, EncodeException {
        //设置最大空闲时间为45分钟
        session.setMaxIdleTimeout(1000 * 60 * 45);
        mySession = session;
        //从token中得到token
        String token = session.getRequestParameterMap().get(WebKeys.TOKEN).get(0);
        if (token == null) {
            log.info("有人瞎鸡巴占用老子的连接数，时间是：" + System.currentTimeMillis());
            this.mySession.close();
            return;
        }
        Map<String, Object> claims = TokenUtil.getClaimsFromToken(token);
        String openid = (String) claims.get(WebKeys.OPEN_ID);
        String hash = (String) claims.get("hash");
        Long timestamp = (Long) claims.get("timestamp");
        if (openid == null || hash == null || timestamp == null) {
            log.info("有人想黑爸爸，时间是：" + System.currentTimeMillis());
            this.mySession.close();
            return;
        }
        User user = userService.findUserByOpenid(openid);
        if(user == null || !Objects.equals(user.getHash() ,hash)){
            log.info("有人想黑爸爸，时间是：" + System.currentTimeMillis());
            this.mySession.close();
            return;
        }
        //连接时检查
        String tempRoomId = roomId.intern();
        ReturnMessage<SocketUser> returnMessage;
        CopyOnWriteArrayList<Session> sessions = sessionsMap.get(Long.valueOf(roomId));
        synchronized (tempRoomId) {
            returnMessage = niuniuService.onOpenCheck(roomId, user);
            if (returnMessage.getMessageCode() > 0) {
                //加入sessionsMap
                sessions.add(mySession);
            }
            SocketUser socketUser = returnMessage.getData();
            //检查不能连接
            if (returnMessage.getMessageCode() < 0) {
                WebsocketUtil.sendBasicMessage(mySession, returnMessage);
                mySession.close();
            } else {
                //将用户放入mySession中
                mySession.getUserProperties().put(WebKeys.WEBSOCKET_USER_KEY, socketUser);
                //给自己发所有人信息的消息，给别人发自己的信息
                RoomPoke roomPoke = roomPokeMap.get(Long.valueOf(roomId));
                ReadyReturnMessage readyReturnMessage = new ReadyReturnMessage();
                readyReturnMessage.setRuningNum(roomPoke.getRuningNum());
                readyReturnMessage.setTotalNum(roomPoke.getTotalNum());
                readyReturnMessage.setRoomStatus(roomPoke.getRoomStatus());
                List<SocketUser> mySocketUserList = Lists.newArrayList();
                List<UserScore> userScores = roomPoke.getUserScores();
                Map<Long ,Integer> scoreMap = userScores.stream().collect(Collectors.toMap(UserScore::getUserId ,UserScore::getScore));
                for (Session s : sessions) {
                    SocketUser su = (SocketUser) s.getUserProperties().get(WebKeys.WEBSOCKET_USER_KEY);
                    UserPoke userPoke = getUserPoke(roomPoke ,su);
                    if (userPoke == null) {
                        su.setIsReady(Boolean.FALSE);
                    }else {
                        su.setIsReady(Boolean.TRUE);
                    }
                    su.setScore(scoreMap.get(su.getId()) != null? scoreMap.get(su.getId()):0);
                    mySocketUserList.add(su);
                }
                readyReturnMessage.setSocketUserList(mySocketUserList);
                ReturnMessage<ReadyReturnMessage> myReturnMessage = new ReturnMessage<>(readyReturnMessage, 0);
                WebsocketUtil.sendBasicMessage(mySession, myReturnMessage);
                //给别人发自己的信息
                for (Session s : sessions) {
                    SocketUser su = (SocketUser) s.getUserProperties().get(WebKeys.WEBSOCKET_USER_KEY);
                    socketUser.setScore(0);
                    socketUser.setIsReady(false);
                    if (!Objects.equals(su.getId() ,socketUser.getId())) {
                        List<SocketUser> otherSocketUserList = Lists.newArrayList();
                        otherSocketUserList.add(socketUser);
                        readyReturnMessage.setSocketUserList(otherSocketUserList);
                        ReturnMessage<ReadyReturnMessage> otherReturnMessage = new ReturnMessage<>(readyReturnMessage, 1);
                        WebsocketUtil.sendBasicMessage(s, otherReturnMessage);
                    }
                }
            }
        }
    }
    /**
     * 得到玩家userPoke
     * @return
     */
    private UserPoke getUserPoke(RoomPoke roomPoke ,SocketUser socketUser){
        List<UserPokesIndex> userPokesIndexList = roomPoke.getUserPokes();
        if (userPokesIndexList.isEmpty()) {
            return null;
        }
        UserPokesIndex userPokesIndex = userPokesIndexList.stream().filter(u -> Objects.equals(u.getIndex(), roomPoke.getRuningNum()))
                .collect(Collectors.toList()).get(0);
        List<UserPoke> userPoke = userPokesIndex.getUserPokeList().stream().filter(u -> Objects.equals(socketUser.getId(), u.getUserId()))
                .collect(Collectors.toList());
        if (userPoke.isEmpty()) {
            return null;
        }else {
            return userPoke.get(0);
        }
    }


    @OnMessage
    public void receiveMsg(@PathParam("roomId") String roomId, ReceiveMessage receiveMessage) throws InterruptedException, EncodeException, IOException {
        Integer messageCode = receiveMessage.getMessageCode();
        SocketUser socketUser = (SocketUser) mySession.getUserProperties().get(WebKeys.WEBSOCKET_USER_KEY);
        Long roomIdNum = Long.valueOf(roomId);
        if (Objects.equals(messageCode , 1)) {
            niuniuService.dealReadyMessage( socketUser,roomIdNum);
        }else if (Objects.equals(messageCode ,2)) {
            niuniuService.dealQiangZhuangMessage(socketUser ,roomIdNum ,receiveMessage);
        }else if (Objects.equals(messageCode ,3)) {
            niuniuService.dealXianJiaXiaZhuMessage(socketUser ,roomIdNum ,receiveMessage);
        }else if (Objects.equals(messageCode ,4)) {
            niuniuService.dealTanPaiMessage(socketUser ,roomIdNum);
        }
    }

    @OnClose
    public void disConnect(@PathParam("roomId") String roomName, Session session) {
        CopyOnWriteArrayList<Session> sessionList = sessionsMap.get(Long.valueOf(roomName));
        if (sessionList == null) {
            return;
        }
        Iterator<Session> itrSession = sessionList.iterator();
        while (itrSession.hasNext()) {
            Session targetSession = itrSession.next();
            if (Objects.equals(targetSession ,session)) {
                SocketUser user = (SocketUser) targetSession.getUserProperties().get(WebKeys.WEBSOCKET_USER_KEY);
                log.info("用户断开，用户id:"+user.getId());
                sessionList.remove(targetSession);
                break;
            }
        }
    }

    @OnError
    public void onError(Throwable t){
        t.printStackTrace();
        log.error(t.getMessage());
        //disConnect();
    }

}
