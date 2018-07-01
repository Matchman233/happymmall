package com.mmall.controller.backend;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import com.sun.javafx.collections.MappingChange;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by apple on 2018/6/25.
 */
@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;


    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product) {
        // 只能管理员有权限操作
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录!");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 操作category
            return iProductService.saveOrUpdateProduct(product);
        }
        return ServerResponse.createByErrorMessage("无权限操作！");
    }


    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        // 只能管理员有权限操作
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录!");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 操作category
            return iProductService.setSaleStatus(productId, status);
        }
        return ServerResponse.createByErrorMessage("无权限操作！");
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detailProduct(HttpSession session, Integer productId) {
        // 只能管理员有权限操作
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录!");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 操作category
            return iProductService.manageProductDetail(productId);
        }
        return ServerResponse.createByErrorMessage("无权限操作！");
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getProductList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        // 只能管理员有权限操作
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录!");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.getProductList(pageNum, pageSize);
        }
        return ServerResponse.createByErrorMessage("无权限操作！");
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        // 只能管理员有权限操作
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录!");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        }
        return ServerResponse.createByErrorMessage("无权限操作！");
    }

    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile, HttpServletRequest request) {

        // 只能管理员有权限操作
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录!");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String uploadName = iFileService.upload(multipartFile, path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + uploadName;
            Map resultMap = Maps.newHashMap();
            resultMap.put("uri", uploadName);
            resultMap.put("url", url);
            return ServerResponse.createBySuccess(resultMap);
        }
        return ServerResponse.createByErrorMessage("无权限操作！");
    }

    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile, HttpServletRequest request, HttpServletResponse response) {
        Map result = Maps.newHashMap();
        // TODO 富文本上传的时候有自己的要求，springMVC，simditor。
        // todo Simditor编辑器 SpringMVC上传图片

        // 只能管理员有权限操作
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            result.put("success", false);
            result.put("msg", "未登录");
            return result;
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String uploadName = iFileService.upload(multipartFile, path);
            if (StringUtils.isBlank(uploadName)) {
                result.put("success", false);
                result.put("msg", "上传出错!");
                return result;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + uploadName;
            result.put("success", true);
            result.put("msg", "上传成功");
            result.put("file_path", url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return result;
        }
        result.put("success", false);
        result.put("msg", "无权限操作");
        return result;
    }









}
