package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.naming.Context;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    //获取当前账户的id
    /*
    * 展示购物车数据
    * */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        //获取当前账户的userId
        long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart>lqw=new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);
        lqw.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(lqw);
        return R.success(list);
    }
    /*
    * 添加进购物车
    * */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //获取当前账户的userId
        long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //查询当前菜品或者套餐是否在购物车内,在的话number数量＋1,否则新增数据,数量设为1
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);
        lqw.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId())
                .or()
                .eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        //select * from shopping_cart where user_id=#{userId} and dish_id=#{dishId} or setmeal_id=#{setmealId}
        // 从数据库中查询购物车数据
        ShoppingCart sc = shoppingCartService.getOne(lqw);
        if (sc==null){
            // 如果购物车中不存在该菜品或者套餐，则新增购物车数据
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            return R.success(shoppingCart);
        }else {
            // 如果购物车中已经存在该菜品或者套餐，则更新购物车数据
            int number= sc.getNumber();
            sc.setNumber(number+1);
            shoppingCartService.update(sc,lqw);
            return R.success(sc);
        }
    }
    /*
    * 购物车减少商品
    * */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        //获取当前账户的userId
        long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart>lqw=new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);
        lqw.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId())
                .or()
                .eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        shoppingCart = shoppingCartService.getOne(lqw);
        int number=shoppingCart.getNumber();
        if (number>1){
            shoppingCart.setNumber(number-1);
            shoppingCartService.update(shoppingCart,lqw);
        }else {
            shoppingCartService.remove(lqw);
        }
        log.info("===================");
        System.out.println(shoppingCart);;
        return R.success(shoppingCart);
    }
        /*
    * 清空购物车数据
    * */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //获取当前账户的userId
        long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(lqw);
        return R.success("已清空");
    }
}
