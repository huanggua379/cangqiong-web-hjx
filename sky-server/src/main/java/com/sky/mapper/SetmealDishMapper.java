package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    void save(SetmealDish setmealDish);

    void deleteBySetmealIds(List<Long> ids);

    void deleteBySetmealId(Long id);

    void insertBatch(List<SetmealDish> setmealDishes);
}
