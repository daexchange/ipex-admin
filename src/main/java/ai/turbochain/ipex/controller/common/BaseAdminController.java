package ai.turbochain.ipex.controller.common;

import ai.turbochain.ipex.constant.SysConstant;
import ai.turbochain.ipex.controller.BaseController;
import ai.turbochain.ipex.entity.Admin;
import ai.turbochain.ipex.service.LocaleMessageSourceService;
import ai.turbochain.ipex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

/**
 * @author GS
 * @date 2017年12月19日
 */
@Component
public class BaseAdminController extends BaseController {

    @Autowired
    private RedisTemplate redisTemplate ;
    @Autowired
    private LocaleMessageSourceService msService;

    protected Admin getAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (Admin) session.getAttribute(SysConstant.SESSION_ADMIN);
    }

    /**
     * 判断手机验证码正确否
     * @param code 验证码
     * @param key redis中的key 前缀+手机号
     * @return
     */
    protected MessageResult checkCode(String code, String key){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object value = valueOperations.get(key);
        if(value==null) {
            return error(msService.getMessage("CODE_NOT_EXIST_RESEND"));
        }
        if(!value.toString().equals(code)) {
            return  error(msService.getMessage("CODE_ERROR"));
        }
        valueOperations.getOperations().delete(key);
        /**
         * 十分钟之内无需再次验证
         */
        valueOperations.set(key+"_PASS",true,10, TimeUnit.MINUTES);
        return success(msService.getMessage("CODE_CORRECT"));
    }
}
