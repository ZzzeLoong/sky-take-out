package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品:{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        String key = "dish_"+dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam("ids") List<Long> dishIds) {
        log.info("批量删除菜品：{}",dishIds);
        dishService.deleteBatch(dishIds);

        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查找菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("查找菜品id为：{}",id);
        DishVO dishVO= dishService.getById(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateWithFlavor(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);

        cleanCache("dish_*");
        return Result.success();
    }

    @PostMapping("status/{status}")
    @ApiOperation("起售停售菜品")
    public Result startOrStopDish(@PathVariable Integer status, @RequestParam("id") Long dishId) {
        log.info("起售或停售菜品id为：{}",dishId);
        dishService.startOrStopDish(status,dishId);

        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(@RequestParam Long categoryId) {
        List<DishVO> dishVOS=dishService.getByCategoryId(categoryId);
        return Result.success(dishVOS);
    }

    /**
     * 清理缓存数据
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
