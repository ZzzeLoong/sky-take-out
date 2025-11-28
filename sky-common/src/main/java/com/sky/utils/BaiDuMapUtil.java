package com.sky.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.exception.OrderBusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BaiDuMapUtil {

    private static String shopAddress;
    private static String ak;
    private static String sk;

    @Value("${sky.shop.address}")
    public void setShopAddress(String shopAddress) {
        BaiDuMapUtil.shopAddress = shopAddress;
    }

    @Value("${sky.baidu.ak}")
    public void setAk(String ak) {
        BaiDuMapUtil.ak = ak;
    }

    @Value("${sky.baidu.sk}")
    public void setSk(String sk) {
        BaiDuMapUtil.sk = sk;
    }


    /**
     * 校验配送距离是否超出范围
     * @param userAddress 用户收货地址
     */
    public static void checkOutOfRange(String userAddress) throws UnsupportedEncodingException {
        // --- 1. 获取店铺经纬度 ---
        String timestamp = String.valueOf(System.currentTimeMillis());
        Map<String, String> shopMap = new LinkedHashMap<>();
        shopMap.put("address", shopAddress);
        shopMap.put("output", "json");
        shopMap.put("ak", ak);
        shopMap.put("timestamp", timestamp);
        // 计算店铺查询接口的 SN
        String shopSn = generateSn("/geocoding/v3", shopMap);
        shopMap.put("sn", shopSn);

        // 调用百度接口
        String shopRes = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", shopMap);
        String shopLngLat = parseLngLat(shopRes, "店铺地址");

        // --- 2. 获取用户经纬度 ---
        Map<String, String> userMap = new LinkedHashMap<>();
        userMap.put("address", userAddress);
        userMap.put("output", "json");
        userMap.put("ak", ak);
        userMap.put("timestamp", timestamp);
        // 计算用户查询接口的 SN
        String userSn = generateSn("/geocoding/v3", userMap);
        userMap.put("sn", userSn);

        String userRes = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", userMap);
        String userLngLat = parseLngLat(userRes, "收货地址");

        // --- 3. 路线规划 (计算距离) ---
        Map<String, String> driveMap = new LinkedHashMap<>();
        driveMap.put("origin", shopLngLat);
        driveMap.put("destination", userLngLat);
        driveMap.put("steps_info", "0");
        driveMap.put("ak", ak);
        driveMap.put("timestamp", timestamp);
        // 计算驾车规划接口的 SN
        String driveSn = generateSn("/directionlite/v1/driving", driveMap);
        driveMap.put("sn", driveSn);

        String driveRes = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", driveMap);

        // 解析距离
        JSONObject jsonObject = JSON.parseObject(driveRes);
        if (!"0".equals(jsonObject.getString("status"))) {
            throw new OrderBusinessException("配送路线规划失败: " + jsonObject.getString("message"));
        }

        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = result.getJSONArray("routes");
        if (jsonArray == null || jsonArray.isEmpty()) {
            throw new OrderBusinessException("未找到配送路线");
        }

        Integer distance = jsonArray.getJSONObject(0).getInteger("distance");

        if (distance > 50000) {
            throw new OrderBusinessException("超出配送范围");
        }
    }

    /**
     * 辅助方法：解析经纬度响应
     */
    private static String parseLngLat(String jsonResponse, String locationName) {
        JSONObject jsonObject = JSON.parseObject(jsonResponse);
        if (!"0".equals(jsonObject.getString("status"))) {
            throw new OrderBusinessException(locationName + "解析失败");
        }
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        return location.getString("lat") + "," + location.getString("lng");
    }

    /**
     * 核心工具：生成SN签名
     * 算法说明：https://lbsyun.baidu.com/index.php?title=jspopular3.0/guide/sn
     */
    public static String generateSn(String uri, Map<String, String> paramsMap) throws UnsupportedEncodingException {
        // 1. 拼接参数字符串 (key1=value1&key2=value2...)
        String paramsStr = toQueryString(paramsMap);

        // 2. 拼接完整的待加密串: uri + ? + params + sk
        // 这一步非常关键，SK直接拼接在最后
        String wholeStr = uri + "?" + paramsStr + sk;

        // 3. 对整体做一次 URLEncoder 编码
        String tempStr = URLEncoder.encode(wholeStr, "UTF-8");

        // 4. MD5 加密
        return MD5(tempStr);
    }

    /**
     * 将Map转换为请求字符串，并对Value进行UTF-8编码
     */
    private static String toQueryString(Map<String, String> data) throws UnsupportedEncodingException {
        StringBuilder queryString = new StringBuilder();
        for (Entry<String, String> pair : data.entrySet()) {
            queryString.append(pair.getKey()).append("=");
            // 百度要求：参数的值在拼接前必须进行 URLEncoder
            queryString.append(URLEncoder.encode(pair.getValue(), "UTF-8")).append("&");
        }
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }

    /**
     * MD5计算
     */
    private static String MD5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}