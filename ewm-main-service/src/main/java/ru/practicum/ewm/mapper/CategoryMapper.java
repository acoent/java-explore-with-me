package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.model.Category;

@UtilityClass
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public Category toEntity(NewCategoryDto dto) {
        if (dto == null) {
            return null;
        }
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public void update(Category category, CategoryDto dto) {
        if (dto == null) {
            return;
        }
        if (dto.getName() != null) {
            category.setName(dto.getName());
        }
    }
}
