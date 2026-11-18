package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.com.possystem.entity.Category;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.User;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.mapper.CategoryMapper;
import pl.com.possystem.payload.dto.CategoryDTO;
import pl.com.possystem.repository.CategoryRepository;
import pl.com.possystem.repository.StoreRepository;
import pl.com.possystem.service.CategoryService;
import pl.com.possystem.service.TenantAccess;
import pl.com.possystem.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final UserService userService;

    private final StoreRepository storeRepository;
    private final TenantAccess tenantAccess;

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) throws Exception {
        User user = userService.getCurrentUser();

        Store store = storeRepository.findById(categoryDTO.getStoreId()).orElseThrow(() -> new Exception("Store not found"));

        tenantAccess.requireStoreManagement(user, store);
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .store(store)
                .build();


        return CategoryMapper.toDTO(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDTO> getCategoriesByStore(Long storeId) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Store not found"));
        tenantAccess.requireStoreAccess(userService.getCurrentUser(), store);
        List<Category> categories = categoryRepository.findByStoreId(storeId);
        return categories.stream().map(CategoryMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) throws Exception {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new Exception("Category not found"));

        User user = userService.getCurrentUser();

        tenantAccess.requireStoreManagement(user, category.getStore());

        category.setName(categoryDTO.getName());

        return CategoryMapper.toDTO(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) throws Exception {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new Exception("Category not found"));

        User user = userService.getCurrentUser();

        tenantAccess.requireStoreManagement(user, category.getStore());

        categoryRepository.deleteById(id);
    }
}
