package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper {
    @Insert("INSERT into orders (number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount, " +
            "remark, phone, address, user_name, consignee, estimated_delivery_time, " +
            "delivery_status, pack_amount, tableware_number, tableware_status) VALUES " +
            "(#{number}, #{status}, #{userId}, #{addressBookId}, #{orderTime}, #{checkoutTime}, #{payMethod}," +
            "                #{payStatus}, #{amount}, #{remark}, #{phone}, #{address}, #{userName},#{consignee}," +
            "                #{estimatedDeliveryTime}, #{deliveryStatus}, #{packAmount}, #{tablewareNumber}, #{tablewareStatus})")
    @Options(useGeneratedKeys = true,keyProperty = "id")
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber} and user_id=#{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} " +
            "where number = #{orderNumber}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, String orderNumber);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id=#{orderId}")
    Orders getByOrderId(Long orderId);

    @Select("SELECT count(*) from orders where status=#{status}")
    Integer getCountByStatus(Integer status);
}
