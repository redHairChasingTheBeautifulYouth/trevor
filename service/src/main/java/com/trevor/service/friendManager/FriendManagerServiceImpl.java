package com.trevor.service.friendManager;

import com.google.common.collect.Lists;
import com.trevor.bo.FriendInfo;
import com.trevor.bo.JsonEntity;
import com.trevor.bo.ResponseHelper;
import com.trevor.enums.MessageCodeEnum;
import com.trevor.dao.FriendManageMapper;
import com.trevor.domain.FriendsManage;
import com.trevor.domain.User;
import com.trevor.service.RoomRecordCacheService;
import com.trevor.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 一句话描述该类作用:【】
 *
 * @author: trevor
 * @create: 2019-03-03 23:56
 **/
@Slf4j
@Service
public class FriendManagerServiceImpl implements FriendManagerService {

    @Resource
    private FriendManageMapper friendManageMapper;

    @Resource
    private UserService userService;

    @Resource
    private RoomRecordCacheService roomRecordCacheService;

    /**
     * 查询好友（申请通过和未通过的）
     * @return
     */
    @Override
    public List<FriendInfo> queryFriends(User user) {
        List<FriendsManage> list =friendManageMapper.findByUserId(user.getId());
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = list.stream().map(friendsManage -> friendsManage.getManageFriendId()).collect(Collectors.toList());
        Map<Long ,Integer> map = list.stream().collect(Collectors.toMap(FriendsManage::getManageFriendId ,FriendsManage::getAllowFlag));
        List<User> users = userService.findUsersByIds(ids);
        List<FriendInfo> friendInfos = Lists.newArrayList();
        users.forEach(user1 -> {
            FriendInfo friendInfo = new FriendInfo();
            friendInfo.setUserId(user1.getId());
            friendInfo.setAppName(user1.getAppName());
            friendInfo.setPictureUrl(user1.getAppPictureUrl());
            friendInfo.setAllowFlag(map.get(user1.getId()));
            friendInfos.add(friendInfo);
        });
        return friendInfos;
    }

    /**
     * 申请好友
     * @param roomId
     * @param applyUserId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonEntity<Object> applyFriend(Long roomId ,Long applyUserId) {
        Long roomAuthId = roomRecordCacheService.findRoomAuthIdByRoomId(roomId);
        friendManageMapper.applyFriend(roomAuthId ,applyUserId);
        return ResponseHelper.createInstanceWithOutData(MessageCodeEnum.HANDLER_SUCCESS);
    }

    /**
     * 通过好友申请
     * @param userId
     * @param passUserId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonEntity<Object> passFriend(Long userId ,Long passUserId) {
        friendManageMapper.passFriend(userId ,passUserId);
        return ResponseHelper.createInstanceWithOutData(MessageCodeEnum.HANDLER_SUCCESS);
    }

    /**
     * 提出好友
     * @param userId
     * @param removeUserId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonEntity<Object> removeFriend(Long userId ,Long removeUserId) {
        friendManageMapper.removeFriend(userId ,removeUserId);
        return ResponseHelper.createInstanceWithOutData(MessageCodeEnum.HANDLER_SUCCESS);
    }
}
