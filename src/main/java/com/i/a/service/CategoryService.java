package com.i.a.service;

import com.i.a.dto.CategoryRequest;
import com.i.a.dto.CategoryResponse;
import com.i.a.entity.Category;
import com.i.a.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        String name = request.name().trim();

        if (categoryRepository.existsByName(name)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Category already exists"
            );
        }

        Category category = new Category();
        category.setName(name);

        return toResponse(categoryRepository.save(category));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName()
        );
    }
}
