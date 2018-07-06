package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * Created by apple on 2018/7/6.
 */

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse select(Integer userId, Integer shippingId) {
        if (userId == null || shippingId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Shipping shipping = shippingMapper.selectByUserIdAndShippingId(userId, shippingId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.createBySuccess(shipping);
    }

    public ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectAllShippingByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse add(Integer userId, Shipping shipping) {
        if (userId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        shipping.setUserId(userId);
        int resultCount = shippingMapper.insert(shipping);
        if (resultCount > 0) {
            Map<String, Integer> resultMap = Maps.newHashMap();
            return ServerResponse.createBySuccess(resultMap.put("shippingId", shipping.getId()));
        } else {
            return  ServerResponse.createByErrorMessage("新增地址失败");
        }
    }


    public ServerResponse del(Integer userId, Integer shippingId) {
        if (userId == null || shippingId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        int resultCount = shippingMapper.delByShippingIdAndUSerId(userId, shippingId);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess("删除地址成功");
        } else {
            return ServerResponse.createByErrorMessage("删除地址失败");
        }
    }

    public ServerResponse update(Integer userId,Shipping shipping){
        if (userId == null || shipping == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        shipping.setUserId(userId);
        int resultCount = shippingMapper.updateShipping(shipping);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }else{
            return ServerResponse.createByErrorMessage("更新地址失败");
        }
    }


}
