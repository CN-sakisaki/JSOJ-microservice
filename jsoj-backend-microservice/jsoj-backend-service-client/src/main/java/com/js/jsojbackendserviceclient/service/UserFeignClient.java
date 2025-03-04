package com.js.jsojbackendserviceclient.service;

import com.js.jsojbackendmodel.entity.User;
import com.js.jsojbackendmodel.enums.UserRoleEnum;
import com.js.jsojbackendmodel.vo.user.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

// import static com.js.jsojbackendmodel.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户内部服务接口
 * @author sakisaki
 * @date 2025/2/22 15:05
 */
@FeignClient(name = "jsoj-backend-user-service", path = "/api/user/inner")
public interface UserFeignClient {


    /**
     * 根据 id 获取用户信息
     *
     * @param userId
     * @return User
     */
    @GetMapping("/get/id")
    User getById(@RequestParam("userId") long userId);

    /**
     * 根据 id 获取用户列表
     *
     * @param idList
     * @return List<User>
     */
    @GetMapping("/get/ids")
    List<User> listByIds(@RequestParam("idList") Collection<Long> idList);

    // /**
    //  * 获取当前登录用户
    //  *
    //  * @param request
    //  * @return User
    //  */
    // default User getLoginUser(HttpServletRequest request) {
    //     // 先判断是否已登录
    //     Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
    //     User currentUser = (User) userObj;
    //     if (currentUser == null || currentUser.getId() == null) {
    //         throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    //     }
    //     return currentUser;
    // }

    /**
     * 是否为管理员
     *
     * @param user
     * @return boolean
     */
    default boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return UserVO
     */
    default UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

}
