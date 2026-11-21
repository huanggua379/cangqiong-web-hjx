package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.BaseException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.save(setmeal);
        Long id = setmeal.getId();
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
if (setmealDishes!=null&&setmealDishes.size()>0){
    for (SetmealDish setmealDish : setmealDishes) {
        setmealDish.setSetmealId(id);
        setmealDishMapper.save(setmealDish);
    }
}
    }
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page= setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
            setmealMapper.deleteByIds(ids);
            setmealDishMapper.deleteBySetmealIds(ids);
    }

    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
//            //向口味表插入n条数据
//            setmealDishMapper.insertBatch(setmeal);//后绪步骤实现
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }
    @Override
    public void use(int status, long id) {
        if (status != StatusConstant.ENABLE && status != StatusConstant.DISABLE) {
            throw new BaseException("套餐状态只能为 0 或 1");
        }
        if (status == StatusConstant.ENABLE && setmealMapper.countUnavailableDishes(id) > 0) {
            throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
        }
        if (setmealMapper.use(status, id) == 0 && setmealMapper.getById(id) == null) {
            throw new BaseException("套餐不存在");
        }
    }
}
