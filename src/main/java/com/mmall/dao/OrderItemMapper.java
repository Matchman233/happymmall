package com.mmall.dao;

import com.mmall.pojo.Order;
import com.mmall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    void insertBatch(@Param("orderItemList") List<OrderItem> orderItemList);

    List<OrderItem> selectAllByUserIdAndOrderNo(@Param("usedId") Integer usedId,@Param("orderNo") long orderNo);

    List<OrderItem> selectAllByOrderNo(long orderNo);
}