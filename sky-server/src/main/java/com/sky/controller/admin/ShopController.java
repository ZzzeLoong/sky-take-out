package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminShopController")
@Api(tags ="店铺相关接口")
@RequestMapping("/admin/shop")
public class ShopController {

    public static final String Key = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result updateShopStatus(@PathVariable String status) {
        redisTemplate.opsForValue().set(Key,status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getShopStatus() {
        Integer status= Integer.parseInt((String) redisTemplate.opsForValue().get(Key));
        return Result.success(status);
    }

}
