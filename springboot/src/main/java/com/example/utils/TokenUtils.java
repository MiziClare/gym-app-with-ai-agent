package com.example.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.common.Constants;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.service.AdminService;
import com.example.service.CoachService;
import com.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Token utility
 * Used to handle JWT, mainly for user authentication and authorization
 */
@Component
public class TokenUtils {

    private static final Logger log = LoggerFactory.getLogger(TokenUtils.class);

    private static AdminService staticAdminService;
    private static CoachService staticCoachService;
    private static UserService staticUserService;

    @Resource
    AdminService adminService;

    @Resource
    CoachService coachService;

    @Resource
    UserService userService;

    @PostConstruct
    public void setUserService() {
        // Inject @PostConstruct annotation, assign values to static variables, so that static methods can call them
        staticAdminService = adminService;
        staticCoachService = coachService;
        staticUserService = userService;
    }

    /**
     * Generate token
     */
    public static String createToken(String data, String sign) {
        return JWT.create().withAudience(data) // Save userId-role in the token as payload
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2)) // 2 hours later, the token will expire
                .sign(Algorithm.HMAC256(sign)); // Use password as the key for the token
    }

    /**
     * Get the current logged-in user information
     */
    public static Account getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader(Constants.TOKEN);
            if (ObjectUtil.isNotEmpty(token)) {
                String userRole = JWT.decode(token).getAudience().get(0);
                String userId = userRole.split("-")[0];  // Get the user id
                String role = userRole.split("-")[1];    // Get the role
                if (RoleEnum.ADMIN.name().equals(role)) {
                    return staticAdminService.selectById(Integer.valueOf(userId));
                }
                if (RoleEnum.COACH.name().equals(role)) {
                    return staticCoachService.selectById(Integer.valueOf(userId));
                }
                if (RoleEnum.USER.name().equals(role)) {
                    return staticUserService.selectById(Integer.valueOf(userId));
                }
            }
        } catch (Exception e) {
            log.error("Error getting current user information", e);
        }
        return new Account();  // Return an empty account object
    }
}

