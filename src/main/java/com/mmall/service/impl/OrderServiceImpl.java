package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by apple on 2018/7/7.
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ShippingMapper shippingMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;


    public ServerResponse create(Integer userId, Integer shippingId) {
        if (userId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 从购物车中获取已经勾选的商品
        List<Cart> cartList = cartMapper.selectCartByStatusChecked(userId);
        // 构造前端页面显示的cartItem的集合
        ServerResponse orderItems = getCartOrderItem(userId, cartList);
        if (!orderItems.isSuccess()) {
            return orderItems;
        }
        // 计算这个订单的总价
        BigDecimal payment = calTotalPrice((List<OrderItem>) orderItems.getData());
        // 构造订单
        Order order = assembleOrder(userId, shippingId, payment);
        if (order == null) {
            return ServerResponse.createByErrorMessage("生成订单错误");
        }

        if (CollectionUtils.isEmpty((List<OrderItem>) orderItems.getData())) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }

        for (OrderItem orderItem : (List<OrderItem>) orderItems.getData()) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        // mybatis插入
        orderItemMapper.insertBatch((List<OrderItem>) orderItems.getData());
        // 减少库存
        reduceProductStock((List<OrderItem>) orderItems.getData());
        // 清空购物车
        cleanCart(cartList);

        OrderVo orderVo = assembleOrderVo(order, (List<OrderItem>) orderItems.getData());
        return ServerResponse.createBySuccess(orderVo);
    }

    public ServerResponse cancel(Integer userId, Long orderId){
        if (userId == null || orderId==null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectOrderByUserIdAndOrderId(userId,orderId);
        if(order==null){
            return ServerResponse.createByErrorMessage("该用户下的该订单号不存在！");
        }
        if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createByErrorMessage("已付款，无法取消该订单号!");
        }
        order.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int resultCount = orderMapper.updateByPrimaryKeySelective(order);
        if(resultCount>0){
            return ServerResponse.createBySuccess("取消成功");
        }else {
            return  ServerResponse.createByErrorMessage("取消失败");
        }
    }

    public ServerResponse getOrderCartProduct(Integer userId){
        if (userId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Cart> cartList = cartMapper.selectCartByStatusChecked(userId);
        ServerResponse serverResponse = getCartOrderItem(userId,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItems = (List<OrderItem>)serverResponse.getData();
        List<OrderItemVo> orderItemVos = Lists.newArrayList();
        for(OrderItem orderItem:orderItems){
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVos.add(orderItemVo);
        }
        OrderProductVo orderProductVo = new OrderProductVo();
        orderProductVo.setOrderItemVoList(orderItemVos);
        orderProductVo.setProductTotalPrice(calTotalPrice(orderItems));
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVo);
    }

    public ServerResponse detail(Integer userId,Long orderNo){
        if (userId == null || orderNo==null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectOrderByUserIdAndOrderId(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("找不到该用户下的该订单!");
        }else{
            List<OrderItem> orderItems = orderItemMapper.selectAllByUserIdAndOrderNo(userId,order.getOrderNo());
            OrderVo orderVo = assembleOrderVo(order,orderItems);
            return ServerResponse.createBySuccess(orderVo);
        }
    }

    public ServerResponse list(Integer userId,Integer pageNum ,Integer pageSize) {
        if (userId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectOrderList(userId);
        List<OrderVo> orderVoList = assbleOrderVoList(orderList,userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        long orderNo = this.generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payment);

        order.setUserId(userId);
        order.setShippingId(shippingId);
        //todo 发货时间等等 在后端发货的时候更新
        //todo 付款时间等等 在后端发货的时候更新
        int rowCount = orderMapper.insert(order);
        if (rowCount > 0) {
            return order;
        }
        return null;
    }

    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToString(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToString(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToString(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToString(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToString(order.getCloseTime()));


        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    private List<OrderVo> assbleOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList){
            List<OrderItem> orderItemList = Lists.newArrayList();
            // todo 针对管理员情况，管理员不需要传userId
            if(userId == null){
                orderItemList = orderItemMapper.selectAllByOrderNo(order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.selectAllByUserIdAndOrderNo(userId,order.getOrderNo());
            }
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    private ServerResponse getCartOrderItem(Integer userId, List<Cart> cartList) {
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        List<OrderItem> orderItems = Lists.newArrayList();
        for (Cart cart : cartList) {
            OrderItem orderItemVo = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (product != null) {
                // 校验是否在售
                if (product.getStatus() == Const.productStatusEnum.ON_SALE.getCode()) {
                    // 校验库存
                    if (product.getStock() >= cart.getQuantity()) {
                        orderItemVo.setUserId(userId);
                        orderItemVo.setProductId(cart.getProductId());
                        orderItemVo.setProductName(product.getName());
                        orderItemVo.setProductImage(product.getMainImage());
                        orderItemVo.setQuantity(cart.getQuantity());
                        orderItemVo.setCurrentUnitPrice(product.getPrice());
                        orderItemVo.setTotalPrice(BigDecimalUtil.multi(orderItemVo.getQuantity(), product.getPrice().doubleValue()));
                    } else {
                        return ServerResponse.createByErrorMessage("商品:" + product.getName() + "库存不足!");
                    }
                } else {
                    return ServerResponse.createByErrorMessage("商品:" + product.getName() + "已下架!");
                }
            } else {
                return ServerResponse.createByErrorMessage("找不到购物车中该商品!");
            }
            orderItems.add(orderItemVo);
        }
        return ServerResponse.createBySuccess(orderItems);
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToString(orderItem.getCreateTime()));
        return orderItemVo;
    }

    private long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    private BigDecimal calTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal payment = BigDecimal.ZERO;
        for (OrderItem orderItem : (List<OrderItem>) orderItemList) {
            payment = BigDecimalUtil.add(orderItem.getTotalPrice().doubleValue(), payment.doubleValue());
        }
        return payment;
    }

    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    private void cleanCart( List<Cart> cartList){
        for(Cart cart:cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }



    // backend
    public ServerResponse manageList(Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAll();
        List<OrderVo> orderVos = assbleOrderVoList(orderList,null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVos);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse manageDetail(Long orderNo){
        if (orderNo == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("找不到该用户下的该订单!");
        }else{
            List<OrderItem> orderItems = orderItemMapper.selectAllByOrderNo(order.getOrderNo());
            OrderVo orderVo = assembleOrderVo(order,orderItems);
            return ServerResponse.createBySuccess(orderVo);
        }
    }

    public ServerResponse manageSearch(Long orderNo,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        if (orderNo == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("找不到该用户下的该订单!");
        }else{
            List<OrderItem> orderItems = orderItemMapper.selectAllByOrderNo(order.getOrderNo());
            OrderVo orderVo = assembleOrderVo(order,orderItems);
            PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
            pageInfo.setList(Lists.newArrayList(orderVo));
            return ServerResponse.createBySuccess(pageInfo);
        }
    }

    public ServerResponse sendGoods(Long orderNo){
        if (orderNo == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("找不到该订单!");
        }
        if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
            order.setSendTime(new Date());
            order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            int resultCount = orderMapper.updateByPrimaryKeySelective(order);
            if(resultCount>0){
                return  ServerResponse.createBySuccessMessage("发货成功");
            }else{
                return ServerResponse.createByErrorMessage("发送失败");
            }
        }else {
            return ServerResponse.createByErrorMessage("该订单还未付款，不能发货.");
        }

    }


}
