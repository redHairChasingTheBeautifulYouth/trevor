package com.trevor.web.config;

import com.trevor.bo.SimpleUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.websocket.Session;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author trevor
 * @date 2019/3/8 16:56
 */
@Configuration
public class RoomConfig {

    /**
     * 注入牛牛的房间
     * @return
     */
    @Bean(name = "niuniuRooms")
    public Map<Long , Set<Session>> generateNiuNiuRoomMap(){
        Map<Long, Set<Session>> niuniuRooms = new ConcurrentHashMap(2<<15);
        return niuniuRooms;
    }
}
