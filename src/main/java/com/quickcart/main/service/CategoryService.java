package com.quickcart.main.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.quickcart.main.model.Category;

public interface CategoryService {

    public Category saveCategory(Category category);

    public Boolean exitsCategory(String name);

    public List<Category> getAllCategories();

    public Boolean deleteCategory(int id);

    public Category getCategoryById(int id);

    public List<Category> getAllActiveCategory();

    public Page<Category> getAllCategoryPagination(Integer pageNo, Integer pageSize);
}
