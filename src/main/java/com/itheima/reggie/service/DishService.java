package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);
    DishDto getByIdWithFlavor(long id);
    void updateWithFlavor(DishDto dishDto);
    void updateStatusOn(long id);
    void updateStatusOff(long id);
    void deleteByIdWithFlavor(List<Long> ids);

}
