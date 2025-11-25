package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DishService {
    void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> dishIds);

    DishVO getById(Long id);

    void updateWithFlavor(DishDTO dishDTO);

    void startOrStopDish(Integer status, Long dishId);

    List<DishVO> getByCategoryId(Long categoryId);
}
