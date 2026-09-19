package com.vaanistock.category;

import com.vaanistock.common.BusinessException;
import com.vaanistock.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    public static final List<String> DEFAULT_CATEGORIES = List.of(
            "Grocery",
            "Beverages",
            "Food Items",
            "Stationery",
            "Household",
            "Personal Care",
            "FMCG",
            "Other"
    );

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Long businessId) {
        return categoryRepository.findByBusinessIdOrderByNameAsc(businessId)
                .stream()
                .map(cat -> CategoryDto.from(cat, 0L))
                .toList();
    }

    @Transactional(readOnly = true)
    public Category getCategoryEntity(Long businessId, Long categoryId) {
        return categoryRepository.findById(categoryId)
                .filter(cat -> cat.getBusinessId().equals(businessId))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategory(Long businessId, Long categoryId) {
        Category category = getCategoryEntity(businessId, categoryId);
        return CategoryDto.from(category, 0L);
    }

    @Transactional
    public CategoryDto createCategory(Long businessId, CategoryDto dto) {
        if (categoryRepository.existsByBusinessIdAndNameIgnoreCase(businessId, dto.getName().trim())) {
            throw new BusinessException("Category '" + dto.getName() + "' already exists");
        }
        Category category = new Category(businessId, dto.getName().trim());
        Category saved = categoryRepository.save(category);
        return CategoryDto.from(saved, 0L);
    }

    @Transactional
    public CategoryDto updateCategory(Long businessId, Long categoryId, CategoryDto dto) {
        Category category = getCategoryEntity(businessId, categoryId);
        String newName = dto.getName().trim();
        if (!category.getName().equalsIgnoreCase(newName) &&
                categoryRepository.existsByBusinessIdAndNameIgnoreCase(businessId, newName)) {
            throw new BusinessException("Category '" + newName + "' already exists");
        }
        category.setName(newName);
        Category saved = categoryRepository.save(category);
        return CategoryDto.from(saved, 0L);
    }

    @Transactional
    public void deleteCategory(Long businessId, Long categoryId) {
        Category category = getCategoryEntity(businessId, categoryId);
        categoryRepository.delete(category);
    }

    @Transactional
    public void seedDefaultCategories(Long businessId) {
        for (String name : DEFAULT_CATEGORIES) {
            if (!categoryRepository.existsByBusinessIdAndNameIgnoreCase(businessId, name)) {
                categoryRepository.save(new Category(businessId, name));
            }
        }
    }
}
