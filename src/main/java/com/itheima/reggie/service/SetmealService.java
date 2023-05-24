package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;
import java.util.Set;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);
    void updateStatusOn(long id);
    void updateStatusOff(long id);
    void deleteWithDish(List<Long> ids);
    SetmealDto getByIdWithDish(long id);
    void updateWithDish(SetmealDto setmealDto);
}
