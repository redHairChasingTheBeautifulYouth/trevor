package com.trevor.service.BrowserLogin;

import com.trevor.bo.JsonEntity;
import com.trevor.bo.ResponseHelper;
import com.trevor.bo.WebSessionUser;
import com.trevor.common.MessageCodeEnum;
import com.trevor.domain.User;
import com.trevor.service.user.UserService;
import com.trevor.util.GetMessageCodeUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author trevor
 * @date 03/22/19 13:15
 */
@Service
public class BrowserLoginServiceImpl implements BrowserLoginService{

    @Resource
    private UserService userService;

    /**
     * 生成验证码发给用户
     * @param phoneNum
     * @return
     */
    @Override
    public JsonEntity<String> generatePhoneCode(String phoneNum) {
        //检查手机号是否已经注册
        Boolean existByPhoneNum = userService.isExistByPhoneNum(phoneNum);
        if (!existByPhoneNum) {
            return ResponseHelper.withErrorInstance(MessageCodeEnum.PHONE_NOT_EXIST);
        }
        String code = GetMessageCodeUtil.getCode(phoneNum);
        return ResponseHelper.createInstance(code ,MessageCodeEnum.CREATE_SUCCESS);
    }

    /**
     * 查询用户
     * @param phoneNum
     * @return
     */
    @Override
    public JsonEntity<WebSessionUser> getWebSessionUserByPhoneNum(String phoneNum) {
        WebSessionUser webSessionUserByPhone = userService.getWebSessionUserByPhone(phoneNum);
        return ResponseHelper.createInstance(webSessionUserByPhone ,MessageCodeEnum.QUERY_SUCCESS);
    }
}
