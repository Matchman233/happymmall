package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by apple on 2018/7/3.
 */
@Controller
@RequestMapping("cart")
public class CartController {
    @Autowired
    private ICartService iCartService;

    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<CartVo> add(HttpSession session, Integer productId, Integer count){

        // 判断用户是否存在
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.add(user.getId(),productId,count);
    }

    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse<CartVo> update(HttpSession session, Integer productId, Integer count){

        // 判断用户是否存在
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.update(user.getId(),productId,count);
    }

    @RequestMapping("delete_product.do")
    @ResponseBody
    public ServerResponse<CartVo> deleteProduct(HttpSession session, String  productIds){

        // 判断用户是否存在
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.deleteProduct(user.getId(),productIds);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session){

        // 判断用户是否存在
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.list(user.getId());
    }

    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpSession session){

        // 判断用户是否存在
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectAllOrUnSelect(user.getId(),null,Const.Cart.CHECKED);
    }

    @RequestMapping("un_select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpSession session){

        // 判断用户是否存在
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectAllOrUnSelect(user.getId(),null,Const.Cart.UN_CHECKED);
    }

    @RequestMapping("select_one.do")
    @ResponseBody
    public ServerResponse<CartVo> selectOne(HttpSession session,Integer productId){

        // 判断用户是否存在
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectAllOrUnSelect(user.getId(),productId,Const.Cart.CHECKED);
    }

    @RequestMapping("un_select_one.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelectOne(HttpSession session,Integer productId){

        // 判断用户是否存在
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectAllOrUnSelect(user.getId(),productId,Const.Cart.UN_CHECKED);
    }


    @RequestMapping("select_cart_product_count.do")
    @ResponseBody
    public ServerResponse<Integer> selectCartProductCount(HttpSession session){

        // 判断用户是否存在
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return  ServerResponse.createBySuccess(0);
        }
        return iCartService.selectCartProductCount(user.getId());
    }

}
