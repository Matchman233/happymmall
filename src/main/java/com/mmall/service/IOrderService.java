package com.mmall.service;

import com.mmall.common.ServerResponse;

/**
 * Created by apple on 2018/7/7.
 */
public interface IOrderService {
    ServerResponse create(Integer userId, Integer shippingId);
    ServerResponse cancel(Integer userId, Long orderId);
    ServerResponse getOrderCartProduct(Integer userId);
    ServerResponse detail(Integer userId,Long orderNo);
    ServerResponse list(Integer userId,Integer pageNum ,Integer pageSize);
    ServerResponse manageList(Integer pageNum,Integer pageSize);
    ServerResponse manageDetail(Long orderNo);
    ServerResponse manageSearch(Long orderNo,Integer pageNum,Integer pageSize);
    ServerResponse sendGoods(Long orderNo);
}
