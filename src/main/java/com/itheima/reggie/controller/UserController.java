package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.print.DocFlavor;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    //注入RedisTemplate对象
    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping("/sendMsg")
    public R<String> sendMsg (@RequestBody User user, HttpSession session){
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)){
            //生成4位随机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            //调用阿里云短信服务api发送短信
//            SMSUtils.sendMessage();
            //由于没申请短信服务,使用以下方法在控制台看验证码
            log.info("code="+code);
//            session.setAttribute(phone,code);
            //验证码保存至redis中,设置有效期为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.success("手机验证码发送成功");
        }

        return R.error("手机验证码发送失败");
    }
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        //也可以用map接收json数据参数,此处前端传的是phone和code,不能用User接收,不用map可以创建Userdto类接收
        log.info(map.toString());
        String phone = (String) map.get("phone");
        String code = (String) map.get("code");
        /*if(!session.getAttribute(phone).equals(code)){
            return R.error("验证码有误");
        }*/
        //从redis中获取验证码
        String codeFromRedis = (String)redisTemplate.opsForValue().get(phone);
        if(!code.equals(codeFromRedis)){
            return R.error("验证码有误");
        }
        LambdaQueryWrapper<User> lqw=new LambdaQueryWrapper<>();
        lqw.eq(User::getPhone,phone);
        User user = userService.getOne(lqw);   //通过手机号码查找对应的User类
        if (user==null){
            //若为空,则为新用户,进行注册处理,保存至数据库
            user=new User();
            user.setPhone(phone);
            user.setStatus(1);
            userService.save(user);
        }
        session.setAttribute("user",user.getId());  //登录成功需要设定session,否则会被过滤器给过滤掉,又返回登录页面
        //登录成功删除redis中缓存的验证码
        redisTemplate.delete(phone);
        return R.success(user);
    }

}
