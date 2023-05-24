package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishMapper dishMapper;
    /*
    * 新增菜品时,同时保存对应的口味数据
    * 因为口味数据和菜品数据不在同一个数据库表,所以需要自定义service
    * */
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);
        //保存口味信息到口味表dishFlavor
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        dishFlavorService.saveBatch(flavors);
    }
    //根据id查询菜品信息及对应的口味信息
    @Override
    public DishDto getByIdWithFlavor(long id) {
        DishDto dishDto=new DishDto();
        Dish dish = this.getById(id);
        LambdaQueryWrapper<DishFlavor> lqw=new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(lqw);
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(list);
        return dishDto;
    }
    //修改菜品,包括口味数据
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新Dish类的数据,不包括口味
        this.updateById(dishDto);
        //更新口味数据,分两步,先删除,后保存新数据
        LambdaQueryWrapper<DishFlavor> lqw=new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lqw);
        List<DishFlavor> flavors = dishDto.getFlavors();
        //由于获取的flavors变量没有dishId的值,存入数据库无意义,下方使用stream流给flavors添加对应的dishId值,也可以使用for循环
        flavors=flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public void updateStatusOn(long id) {
        dishMapper.updateStatusOn(id);
    }

    @Override
    public void updateStatusOff(long id) {
        dishMapper.updateStatusOff(id);
    }

    @Override
    @Transactional
    public void deleteByIdWithFlavor(List<Long> ids) {
        LambdaQueryWrapper<Dish> lqw=new LambdaQueryWrapper<>();
        lqw.eq(Dish::getStatus,1);
        lqw.in(Dish::getId,ids);
        int count = this.count(lqw);
        if (count>0){
            throw new CustomException("此菜品在售中,无法删除");
        }
        this.removeByIds(ids);  //如果菜品已停售则删除
        LambdaQueryWrapper<DishFlavor> lqw2=new LambdaQueryWrapper<>();
        lqw2.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(lqw2);  //删除菜品对应的口味数据
    }
}
