package com.trevor.service.user;

import com.trevor.bo.Authentication;
import com.trevor.dao.UserMapper;
import com.trevor.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService{

    @Resource
    private UserMapper userMapper;

    /**
     * 查询玩家是否开启好友管理功能,1为是，0为否
     * @param userId
     * @return
     */
    @Override
    public Boolean isFriendManage(Long userId) {
        Integer userFriendManage = userMapper.isFriendManage(userId);
        if (Objects.equals(1 ,userFriendManage)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 根据openid查找用户是否存在
     * @param openid
     * @return
     */
    @Override
    public Boolean isExistByOpnenId(String openid) {
        Long existByOpnenId = userMapper.isExistByOpnenId(openid);
        if (Objects.equals(existByOpnenId ,0L)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 新增一个用户
     * @param user
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertOne(User user) {
        userMapper.insertOne(user);
    }


    /**
     * 根据微信id查询用户，包含openid和hash字段
     * @param openid
     * @return
     */
    @Override
    public User findUserByOpenid(String openid) {
        return userMapper.findUserByOpenid(openid);
    }



    /**
     * 更新user
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(User user) {
        userMapper.updateUser(user);
    }

    /**
     * 根据phoneNum查找用户是否存在
     * @param phoneNum
     * @return
     */
    @Override
    public Boolean isExistByPhoneNum(String phoneNum) {
        Long existByPhoneNum = userMapper.isExistByPhoneNum(phoneNum);
        if (Objects.equals(existByPhoneNum ,0L)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 根据phoneNum查询WebSessionUser
     * @param phoneNum
     * @return
     */
    @Override
    public User getUserByPhoneNumContainOpenidAndHash(String phoneNum) {
        User user = userMapper.findUserByPhoneNumContainOpenidAndHash(phoneNum);
        return user;
    }

    /**
     * 根据用户id绑定手机号
     * @param userId
     * @param phoneNum
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePhoneByUserId(Long userId, String phoneNum) {
        User user = new User();
        user.setId(userId);
        user.setPhoneNumber(phoneNum);
        userMapper.updateUser(user);
    }


    /**
     * 实名认证
     * @param authentication
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void realNameAuth(Long userId, Authentication authentication) {
        User user = new User();
        user.setUserId(userId);
        user.setIdCard(authentication.getIdCard());
        user.setRealName(authentication.getRealName());
        userMapper.updateUser(user);
    }

    /**
     * 退出登录
     * @param userId
     */
    @Override
    public void loginOut(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setHash("loginOut");
        userMapper.updateUser(user);
    }

    @Override
    public List<User> findUsersByIds(List<Long> ids) {
        return userMapper.findUsersByIds(ids);
    }
}
