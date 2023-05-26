package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    /*
    * 新增套餐
    * */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("保存成功");
    }
    /*
    * 分页查询
    * */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //对Setmeal类的分页展示,不包括套餐分类
        Page<Setmeal> setmealPage=new Page<>(page,pageSize);
        LambdaQueryWrapper<Setmeal> lqw=new LambdaQueryWrapper<>();
//        lqw.eq(Setmeal::getStatus,1);
        lqw.orderByDesc(Setmeal::getUpdateTime);
        lqw.like(name!=null,Setmeal::getName,name);
        setmealService.page(setmealPage, lqw);
        //对套餐分类与Setmeal类的整合展示
        Page<SetmealDto> setmealDtoPage=new Page<>();
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> list=new ArrayList<>();
        for (Setmeal record : records) {
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(record,setmealDto);
            Category category = categoryService.getById(record.getCategoryId());
            if(category.getName()!=null)setmealDto.setCategoryName(category.getName());
            list.add(setmealDto);
        }
        setmealDtoPage.setRecords(list);
        return R.success(setmealDtoPage);
    }
    /*
    * 套餐起售
    * */
    @PostMapping("/status/1")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> updateStatusOn(long [] ids){
        for (long id : ids) {
            setmealService.updateStatusOn(id);
        }
        return R.success("套餐已起售");
    }
    /*
    * 套餐停售
    * */
    @PostMapping("/status/0")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> updateStatusOff(long [] ids){
        for (long id : ids) {
            setmealService.updateStatusOff(id);
        }
        return R.success("套餐已停售");
    }
    /*
    * 套餐删除
    * */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    //allEntries = true 删除setmealCache下 所有缓存数据
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.deleteWithDish(ids);
        return R.success("删除成功");
    }
    /*
    * 套餐修改页面回显功能
    * */
    @GetMapping("/{id}")
    public R<SetmealDto> show(@PathVariable long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }
    /*
    * 套餐修改页面保存功能
    * */
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }
    /*
    * 前台展示套餐信息
    * */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        log.info(setmeal.toString());
        LambdaQueryWrapper<Setmeal> lqw=new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lqw.eq(setmeal.getStatus()!=null,Setmeal::getStatus,1);
        lqw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(lqw);
        return R.success(setmealList);
    }
}
