package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @PostMapping
    public Result save(@RequestBody SetmealDTO setmealDTO){
      log.info("新增套餐:{}",setmealDTO);
        setmealService.save(setmealDTO);
        return Result.success();
    }
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询：{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除套餐:{}",ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }
    @PutMapping
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐:{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    public Result use(@PathVariable("status") int status, @RequestParam("id") long id) {
        log.info("决定是否启用:{}",status);
        setmealService.use(status, id);
        return Result.success();
    }
}
