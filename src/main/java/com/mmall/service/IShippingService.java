package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

/**
 * Created by apple on 2018/7/6.
 */
public interface IShippingService {

    ServerResponse select(Integer userId, Integer shippingId);

    ServerResponse list(Integer userId, Integer pageNum,Integer pageSize);

    ServerResponse del(Integer userId, Integer shippingId);

    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse update(Integer userId,Shipping shipping);

}
