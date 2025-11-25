package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userShopController")
@Api(tags ="店铺相关接口")
@RequestMapping("/user/shop")
public class ShopController {

    public static final String Key = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getShopStatus() {
        Integer status= Integer.parseInt((String) redisTemplate.opsForValue().get(Key));
        return Result.success(status);
    }

}
