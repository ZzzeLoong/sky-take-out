package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteBySetmealId(Long setmealId);

    @Select("SELECT * FROM setmeal_dish WHERE setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);


    void deleteBatch(List<Long> ids);
}
