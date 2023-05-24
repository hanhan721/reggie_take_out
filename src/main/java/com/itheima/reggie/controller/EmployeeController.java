package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.DigestException;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    /*
    * 登录功能
    * */
    @PostMapping("login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(lqw);
        //3、如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }
        //4、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("密码错误");
        }
        //5、查看员工状态，如果为已用状态，则返回员工已禁用结
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }
        //6、登灵成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
    /*
    * 退出登录
    * */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出登录成功");
    }
    /*
    * 添加员工
    * */
    @PostMapping
    public R<String> add(HttpServletRequest request,@RequestBody Employee employee){
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //获取当前登录用户的id
        //long id = (long)request.getSession().getAttribute("employee");
        //employee.setCreateUser(id);
        //employee.setUpdateUser(id);


        //设置初始密码,使用MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employeeService.save(employee);
        return R.success("添加员工成功");
    }
    /*
    * 分页查询展示
    * */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page pageInfo=new Page(page,pageSize);
        LambdaQueryWrapper<Employee> wrapper=new LambdaQueryWrapper<>();
        //StringUtils.isNotEmpty(name),判断name不为空时才实行此语句
        wrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        wrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo, wrapper);
        return R.success(pageInfo);
    }
    @PutMapping
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request){
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser((long)request.getSession().getAttribute("employee"));

        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }
    /*
    * 根据id查询员工后回显至前端(编辑按钮)
    * */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable long id){
        Employee emp = employeeService.getById(id);
        if (emp!=null) {
            return R.success(emp);
        }
        return R.error("没有查到对应员工的信息");
    }

}
