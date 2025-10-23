package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.util.PaginationUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CategoryDto createCategory(NewCategoryDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException("Category with name '%s' already exists".formatted(dto.getName()));
        }
        Category category = categoryRepository.save(CategoryMapper.toEntity(dto));
        return CategoryMapper.toDto(category);
    }

    @Transactional
    public void deleteCategory(long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=%d was not found".formatted(categoryId)));
        long eventsCount = eventRepository.countByCategoryId(categoryId);
        if (eventsCount > 0) {
            throw new ConflictException("Category with id=%d is bound to events".formatted(categoryId));
        }
        categoryRepository.delete(category);
    }

    @Transactional
    public CategoryDto updateCategory(long categoryId, CategoryDto dto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=%d was not found".formatted(categoryId)));
        if (dto.getName() != null
                && !dto.getName().equalsIgnoreCase(category.getName())
                && categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new ConflictException("Category with name '%s' already exists".formatted(dto.getName()));
        }
        CategoryMapper.update(category, dto);
        Category saved = categoryRepository.save(category);
        return CategoryMapper.toDto(saved);
    }

    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PaginationUtil.offsetPageable(from, size);
        return categoryRepository.findAll(pageable)
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    public CategoryDto getCategory(long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=%d was not found".formatted(categoryId)));
        return CategoryMapper.toDto(category);
    }
}
