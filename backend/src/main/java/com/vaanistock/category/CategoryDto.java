package com.vaanistock.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryDto {
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    private String name;

    private Long productCount;

    public CategoryDto() {}

    public CategoryDto(Long id, String name, Long productCount) {
        this.id = id;
        this.name = name;
        this.productCount = productCount;
    }

    public static CategoryDto from(Category category, Long productCount) {
        return new CategoryDto(category.getId(), category.getName(), productCount != null ? productCount : 0L);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getProductCount() {
        return productCount;
    }

    public void setProductCount(Long productCount) {
        this.productCount = productCount;
    }
}
