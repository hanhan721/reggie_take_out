package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;
    /*
    * 用户下单
    * */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获取用户id
        long userId = BaseContext.getCurrentId();
        //通过id查询购物车数据
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCart = shoppingCartService.list(lqw);
        if (shoppingCart==null||shoppingCart.size()==0){
            throw new CustomException("购物车为空,无法下单");
        }
        //查询用户数据
        User user = userService.getById(userId);
        //查询地址信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook==null){
            throw new CustomException("用户地址信息有误,无法下单");
        }
        //向订单表插入数据---1条
        long id = IdWorker.getId();  //生成订单id
        orders.setId(id);
        orders.setNumber(id+"");    //插入订单号
        orders.setUserId(userId)
                .setOrderTime(LocalDateTime.now())
                .setCheckoutTime(LocalDateTime.now() )
                .setStatus(2)
                .setUserName(user.getName())
                .setConsignee(addressBook.getConsignee())
                .setPhone(addressBook.getPhone())
                .setAddress((addressBook.getProvinceName()==null?"":addressBook.getProvinceName())
                            +(addressBook.getCityName()==null?"":addressBook.getCityName())
                            +(addressBook.getDistrictName()==null?"":addressBook.getDistrictName())
                            +(addressBook.getDetail()==null?"":addressBook.getDetail())
                );
        BigDecimal amount=new BigDecimal(0);
        List<OrderDetail> orderDetailList=new ArrayList<>();  //创建订单详细对象集合
        for (ShoppingCart cart : shoppingCart) {
            OrderDetail orderDetail=new OrderDetail();
            orderDetail.setOrderId(id)              //给订单详细插入数据
                    .setNumber(cart.getNumber())
                    .setDishFlavor(cart.getDishFlavor())
                    .setNumber(cart.getNumber())
                    .setDishId(cart.getDishId())
                    .setSetmealId(cart.getSetmealId())
                    .setName(cart.getName())
                    .setImage(cart.getImage())
                    .setAmount(cart.getAmount());
            orderDetailList.add(orderDetail);
            amount=amount.add(cart.getAmount().multiply(BigDecimal.valueOf(cart.getNumber())));  //amount*number获得
        }
        orders.setAmount(amount);  //插入计算后的价格
        this.save(orders);
        //向订单详细表插入数据   ---可能多条
        orderDetailService.saveBatch(orderDetailList);
        //清空购物车
        shoppingCartService.remove(lqw);
    }
}
