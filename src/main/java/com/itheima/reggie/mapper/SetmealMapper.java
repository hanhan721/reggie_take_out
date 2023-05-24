package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {
    @Update("update setmeal set status= 1 where id=#{id}")
    void updateStatusOn(long id);
    @Update("update setmeal set status= 0 where id=#{id}")
    void updateStatusOff(long id);
}
