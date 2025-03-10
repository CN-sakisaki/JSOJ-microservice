package com.js.jsojbackenduserservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.common.UserContext;
import com.js.jsojbackendcommon.constant.JwtConstant;
import com.js.jsojbackendcommon.constant.RedisConstant;
import com.js.jsojbackendcommon.exception.BusinessException;
import com.js.jsojbackendcommon.exception.ThrowUtils;
import com.js.jsojbackendcommon.utils.JwtUtils;
import com.js.jsojbackendcommon.utils.SqlUtils;
import com.js.jsojbackendmodel.constant.CommonConstant;
import com.js.jsojbackendmodel.dto.user.UserQueryRequest;
import com.js.jsojbackendmodel.entity.User;
import com.js.jsojbackendmodel.enums.UserRoleEnum;
import com.js.jsojbackendmodel.vo.user.UserVO;
import com.js.jsojbackenduserservice.mapper.UserMapper;
import com.js.jsojbackenduserservice.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户接口实现类
 *
 * @author sakisaki
 * @date 2025/2/22 15:36
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "js";


    private final StringRedisTemplate stringRedisTemplate;

    public UserServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 用户注册
     *
     * @param userPhone   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return long
     */
    @Override
    public long userRegister(String userPhone, String userPassword, String checkPassword, String userEmail) {
        // 1. 校验
        ThrowUtils.throwIf(StringUtils.isAnyBlank(userPhone, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userPhone.length() < 11, ErrorCode.PARAMS_ERROR, "手机号过短");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        if (userEmail != null) {
            ThrowUtils.throwIf(emailPattern(userEmail), ErrorCode.PARAMS_ERROR);
        }
        synchronized (userPhone.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userPhone);
            long count = this.baseMapper.selectCount(queryWrapper);
            ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "手机号已注册");
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserEmail(userEmail);
            user.setUserPhone(userPhone);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "注册失败,系统错误");
            return user.getId();
        }
    }

    /**
     * 用户登录
     *
     * @param userPhone  用户账户
     * @param userPassword 用户密码
     * @return LoginUserVO
     */
    @Override
    public UserVO userLogin(String userPhone, String userPassword) {
        // 1. 校验
        ThrowUtils.throwIf(StringUtils.isAnyBlank(userPhone, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userPhone.length() < 4, ErrorCode.PARAMS_ERROR, "账号错误");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码错误");
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userPhone", userPhone);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        // 生成 Token
        String accessToken = JwtUtils.generateAccessToken(user.getId());
        String refreshToken = JwtUtils.generateRefreshToken(user.getId());
        stringRedisTemplate.opsForValue().set(RedisConstant.TOKEN + RedisConstant.ACCESS_TOKEN + user.getId(), accessToken, JwtConstant.ACCESS_TOKEN_EXPIRE, TimeUnit.MILLISECONDS);
        stringRedisTemplate.opsForValue().set(RedisConstant.TOKEN + RedisConstant.REFRESH_TOKEN + user.getId(), refreshToken, JwtConstant.REFRESH_TOKEN_EXPIRE, TimeUnit.MILLISECONDS);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setToken(accessToken);
        return userVO;
    }

    @Override
    public String baseRefreshTokenGetToken(long id) {
        String key = RedisConstant.TOKEN + RedisConstant.REFRESH_TOKEN + id;
        String refreshToken = stringRedisTemplate.opsForValue().get(key);
        ThrowUtils.throwIf(refreshToken == null, ErrorCode.NOT_FOUND_ERROR, "refreshToken不存在");
        Claims claims = JwtUtils.parseToken(refreshToken);
        long userId = (long) claims.get(String.valueOf(id));
        User user = this.getById(userId);
        String newToken = JwtUtils.generateAccessToken(user.getId());
        stringRedisTemplate.opsForValue().set(RedisConstant.TOKEN + RedisConstant.ACCESS_TOKEN + user.getId(), newToken, JwtConstant.ACCESS_TOKEN_EXPIRE, TimeUnit.MILLISECONDS);
        return newToken;
    }

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        Long userId = UserContext.getUser().getId();
        String token = RedisConstant.TOKEN + RedisConstant.ACCESS_TOKEN + userId;
        String refreshToken = RedisConstant.TOKEN + RedisConstant.REFRESH_TOKEN + userId;
        stringRedisTemplate.delete(token);
        stringRedisTemplate.delete(refreshToken);
        return true;
    }


    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏的用户信息集
     *
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 构建查询条件
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userEmail = userQueryRequest.getUserEmail();
        String userPhone = userQueryRequest.getUserPhone();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.eq(StringUtils.isNotBlank(userEmail), "userEmail", userEmail);
        queryWrapper.eq(StringUtils.isNotBlank(userPhone), "userPhone", userPhone);
        queryWrapper.eq(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 验证用户邮箱
     * @param userEmail
     * @return boolean
     */
    private boolean emailPattern(String userEmail) {
        String pattern = "^[a-zA-Z0-9_+&*-]+(?:\\\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\\\.)+[a-zA-Z]{2,7}$";
        return pattern.equals(userEmail);
    }
}
