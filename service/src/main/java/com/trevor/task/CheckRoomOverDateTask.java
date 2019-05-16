package com.trevor.task;

import com.trevor.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

/**
 * @author trevor
 * @date 05/14/19 17:24
 */
@Component
@Slf4j
public class CheckRoomOverDateTask{

    @Resource
    private TaskService taskService;

    @Scheduled(initialDelay = 8000 ,fixedRate = 5000 * 60)
    public void checkRoom(){
        log.info("检查房间开始");
        //房间半小时内未使用会被关闭
        try {
            //taskService.checkRoomRecord();
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
        }
    }
}
