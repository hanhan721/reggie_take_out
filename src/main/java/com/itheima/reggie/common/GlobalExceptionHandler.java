package com.itheima.reggie.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLIntegrityConstraintViolationException;

/*
* 全局异常处理器
* */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
public class GlobalExceptionHandler {
    /*
    * 新增员工时,对账号已存在异常进行处理
    * */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler (SQLIntegrityConstraintViolationException e){   //捕获SQLIntegrityConstraintViolationException异常
        if (e.getMessage().contains("Duplicate entry")){ //异常信息中若有此字段则可判断为新增账号重复,数据库对账号要求唯一
            String[] s = e.getMessage().split(" ");
            return R.error(s[2]+"已存在");
        }
        return R.error("未知错误");
    }
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler (CustomException e){
        return R.error(e.getMessage());
    }
}
