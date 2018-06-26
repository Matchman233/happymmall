package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.configuration.plist.Token;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by apple on 2018/6/18.
 */

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在！");
        }
        // todo MD5密码加密
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误!");
        }
        user.setPassword(StringUtils.EMPTY); // 删掉密码，为了安全
        return ServerResponse.createBySuccess("登陆成功", user);
    }


    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse validResponse = checkValid(user.getUsername(), Const.USERNAME);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        validResponse = checkValid(user.getEmail(), Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }


        // 设置权限
        user.setRole(Const.Role.Role_CUSTOMER);
        // MD5密码加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败！");
        }
        return ServerResponse.createBySuccessMessage("注册成功");

    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        // 判断需要校验的参数是否为空
        if (StringUtils.isNotBlank(type)) {
            // 校验邮箱
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
            // 校验用户名
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("校验参数出错");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse<String> validResponse = checkValid(username, Const.USERNAME);
        // 这里的validResponse根据判断逻辑是
        // user为空
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("对不起，不存在该用户.");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题为空.");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            // 说明问题及问题答案是该用户的.
            // 生成一个token
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.putKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题回答错误!");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        // 判断token是否存在
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("没有token，需要传递token");
        }
        // 判断用户名是否存在
        ServerResponse validResponse = checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户名不存在!");
        }
        // 判断token是否有效
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或者过期!");
        }
        if (StringUtils.equals(token, forgetToken)) {
            String md5password = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.updatePasswordByUsername(username, md5password);
            if (resultCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功!");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新传回.");
        }
        return ServerResponse.createByErrorMessage("修改密码出错.");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误!");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordOld));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("密码更新成功!");
        }
        return ServerResponse.createByErrorMessage("密码更新失败!");
    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        // 用户名不能更新
        // 在controller层中，将当前用户的用户名添加。
        // 查询想更改的邮箱地址是否已经被其他用户用了
        int emailCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (emailCount > 0) {
            return ServerResponse.createByErrorMessage("邮箱已被注册，请换一个新的邮箱地址!");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        // 只更新已下字段
        updateUser.setAnswer(user.getAnswer());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("更新成功", updateUser);
        }
        return ServerResponse.createByErrorMessage("更新失败！");
    }

    @Override
    public ServerResponse<User> getInformation(Integer uid) {
        User user = userMapper.selectByPrimaryKey(uid);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        // 返回到前台的用户数据中的密码设置为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }


    // backend
    @Override
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.Role_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }


}
