package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);
@AutoFill(OperationType.INSERT)
    void save(Setmeal setmeal);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);
@Select("select * from setmeal where id =#{id}")
    Setmeal getById(Long id);

    void deleteByIds(List<Long> ids);

    void update(Setmeal setmeal);
    @Update("update setmeal set status = #{status}, update_time = current_timestamp where id = #{id}")
    int use(@Param("status") int status, @Param("id") long id);

    @Select("select count(*) from setmeal_dish sd left join dish d on sd.dish_id = d.id "
            + "where sd.setmeal_id = #{id} and (d.status is null or d.status != 1)")
    int countUnavailableDishes(@Param("id") long id);
}
