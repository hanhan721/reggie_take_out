package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
 * 菜品管理
 * */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    //注入RedisTemplate对象
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        this.deleteDishFromRedis();
        return R.success("新增菜品成功");
    }

    /*
     * 菜品页分页展示
     * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Dish> dishPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(name != null, Dish::getName, name);
        lqw.orderByDesc(Dish::getUpdateTime);
        Page<Dish> dishpage = dishService.page(dishPage, lqw);
        //由于菜品管理页面Dish实体类没有菜品分类字段,所以需要引入DishDto类
        Page<DishDto> dishDtoPage = new Page<>();
        //对象拷贝,排除掉records属性
        BeanUtils.copyProperties(dishpage, dishDtoPage, "records");
        //获取records属性的值
        List<Dish> records = dishpage.getRecords();
        //stream流写法
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) dishDto.setCategoryName(category.getName());
            return dishDto;
        }).collect(Collectors.toList());
        //传统for循环,易理解
        /*List<DishDto> list = new ArrayList<>();
        for (Dish item : records) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);    //通过categoryId查找对应的category
            if(category!=null) dishDto.setCategoryName(category.getName());    //如果category不为空,把name的值重新存入dishDto对象的categoryName中
            list.add(dishDto);
        }*/
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /*
     * 修改菜品时回显数据
     * */
    @GetMapping("/{id}")
    public R<DishDto> show(@PathVariable long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /*
     * 修改菜品保存数据
     * */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        this.deleteDishFromRedis();
        return R.success("修改成功");
    }

    /*
     * 批量起售
     * */
    @PostMapping("/status/1")
    public R<String> updateStatusOn(long[] ids) {
        for (long id : ids) {
            dishService.updateStatusOn(id);
        }
        this.deleteDishFromRedis();
        return R.success("修改成功");
    }

    /*
     * 批量停售
     * */
    @PostMapping("/status/0")
    public R<String> updateStatusOff(long[] ids) {
        for (long id : ids) {
            dishService.updateStatusOff(id);
        }
        this.deleteDishFromRedis();
        return R.success("修改成功");
    }

    /*
     * 删除  单个和批量
     * */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        dishService.deleteByIdWithFlavor(ids);
        this.deleteDishFromRedis();
        return R.success("删除成功");
    }

    /*
     * 新增套餐时展示菜品信息
     * */
/*    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){

        LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper<>();
        lqw.like(dish.getName()!=null,Dish::getName,dish.getName());
        lqw.eq(Dish::getStatus,1);  //查询状态为1的,起售
        lqw.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(lqw);
        return R.success(list);
    }*/
    //前台移动端,需要做一些修改,返回类型应该是DishDto带口味数据的集合
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        //使用redis缓存菜品数据
        List<DishDto> dishDtoList = new ArrayList<>();
        String key = "CategoryId:" + dish.getCategoryId() + "_" + dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList == null) {
            log.info("redis中没找到数据,通过数据库查询");
            LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
            lqw.like(dish.getName() != null, Dish::getName, dish.getName());
            lqw.eq(Dish::getStatus, 1);  //查询状态为1的,起售
            lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
            lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
            List<Dish> list = dishService.list(lqw);
            dishDtoList = list.stream().map((item) -> {
                DishDto dishDto = new DishDto();
                BeanUtils.copyProperties(item, dishDto);
                LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(DishFlavor::getDishId, item.getId());
                List<DishFlavor> dishFlavorList = dishFlavorService.list(wrapper);
                dishDto.setFlavors(dishFlavorList);
                return dishDto;
            }).collect(Collectors.toList());
            //保存至redis中60分钟
            redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        } else {
            log.info("redis中有缓存,直接查询redis");
        }
        return R.success(dishDtoList);
    }

    //从redis中删除dish菜品缓存
    void deleteDishFromRedis() {
        Set keys = redisTemplate.keys("CategoryId:*_*");
        System.out.println("=============================");
        System.out.println(keys);
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }
}
