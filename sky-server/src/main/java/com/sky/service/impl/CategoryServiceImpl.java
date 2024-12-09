package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {

        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void save(CategoryDTO categoryDTO) {
        log.info("名称: {} ", categoryDTO.getName());
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .sort(categoryDTO.getSort())
                .type(categoryDTO.getType())
                .build();

        category.setStatus(StatusConstant.DISABLE);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insert(category);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        categoryMapper.update(Category.builder().
                id(id).
                status(status).
                updateUser(BaseContext.getCurrentId()).
                updateTime(LocalDateTime.now()).
                build());
    }

    @Override
    public void update(CategoryDTO categoryDTO) {

        categoryMapper.update(Category.builder()
                .id(categoryDTO.getId())
                .name(categoryDTO.getName())
                .sort(categoryDTO.getSort())
                .type(categoryDTO.getType())
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build());
    }

    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }

    @Override
    public void delete(Long id) {
        // 查询分类是否关联了菜品
        if (dishMapper.countByCategoryId(id) > 0 ) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        // 查询分类是否关联了套餐
        if (setmealMapper.countByCategoryId(id) > 0 ) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        categoryMapper.deleteById(id);
    }
}
