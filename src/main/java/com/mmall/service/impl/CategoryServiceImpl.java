package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by apple on 2018/6/24.
 */

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("添加种类的参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int resultCount = categoryMapper.insert(category);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess("添加种类成功!");
        }
        return ServerResponse.createByErrorMessage("添加种类失败!");
    }

    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("更新种类名字的参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess("更新种类名字成功!");
        }
        return ServerResponse.createByErrorMessage("更新种类名字失败!");
    }

    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("未找到当前分类的子分类!");
        }
        return ServerResponse.createBySuccess(categoryList);
    }


    /**
     * 递归查询本节点的ID和所有孩子节点的Id
     * @param categoryId
     * @return
     */
    @Override
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet, categoryId);
        // 传回前台的数据只需要id就可以了
        List<Integer> categoryList = Lists.newArrayList();
        for (Category iter : categorySet) {
            categoryList.add(iter.getId());
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    // 递归获取子节点
    // 这种set集合中的category，需要重写hashcode和equlas方法
    public Set<Category> findChildCategory(Set<Category> categorySet, Integer categotyId) {
        Category category = categoryMapper.selectByPrimaryKey(categotyId);
        if (category != null) {
            categorySet.add(category);
        }
        List<Category> list = categoryMapper.selectCategoryChildrenByParentId(categotyId);
        for (Category iter : list) {
            findChildCategory(categorySet, iter.getId());
        }
        return categorySet;

    }


}
