package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Dish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    @Update("update dish set status= 1 where id=#{id}")
    void updateStatusOn(long id);
    @Update("update dish set status= 0 where id=#{id}")
    void updateStatusOff(long id);
    @Delete("delete from dish where id=#{id}")
    void deleteById(long id);
}
