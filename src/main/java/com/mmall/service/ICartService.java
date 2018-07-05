package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

/**
 * Created by apple on 2018/7/3.
 */
public interface ICartService {
    ServerResponse<CartVo> add(Integer userid , Integer productId, Integer count);
    ServerResponse<CartVo> update(Integer userid ,Integer productId,Integer count);
    ServerResponse<CartVo> deleteProduct(Integer userId,String productIds);
    ServerResponse<CartVo> list(Integer userId );
    ServerResponse<CartVo> selectAllOrUnSelect(Integer userId,Integer productId,Integer checked );
    ServerResponse<Integer> selectCartProductCount(Integer userId);
}
