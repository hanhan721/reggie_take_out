package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealMapper setmealMapper;
    /*
    * 新增套餐,同时需要保存套餐和菜品的关联关系
    * */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
            setmealDishService.save(setmealDish);
        }
    }

    @Override
    public void updateStatusOn(long id) {
        setmealMapper.updateStatusOn(id);
    }

    @Override
    public void updateStatusOff(long id) {
        setmealMapper.updateStatusOff(id);
    }

    @Override
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        //删除Setmeal表数据
        LambdaQueryWrapper<Setmeal> lqw=new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,1);// select count(*) from setmeal where id in(#{ids}) and status = 1;
        int count = this.count(lqw);
        if (count>0){
            throw new CustomException("套餐正在售卖中,不能删除");
        }
        //删除相关联中setmealDish表数据
        this.removeByIds(ids);
        LambdaQueryWrapper<SetmealDish> lqw2=new LambdaQueryWrapper<>();
        lqw2.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lqw2);
    }

    @Override
    public SetmealDto getByIdWithDish(long id) {
        SetmealDto setmealDto=new SetmealDto();
        Setmeal setmeal = this.getById(id);
        LambdaQueryWrapper<SetmealDish> lqw=new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(lqw);
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);
        LambdaQueryWrapper<SetmealDish> lqw=new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(lqw);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
            setmealDishService.save(setmealDish);
        }
    }

}
