package pl.com.possystem.service;

import pl.com.possystem.exception.UserException;
import pl.com.possystem.payload.dto.CategoryDTO;

import java.util.List;

public interface CategoryService{

    CategoryDTO createCategory(CategoryDTO categoryDTO) throws Exception;

    List<CategoryDTO> getCategoriesByStore(Long storeId);

    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) throws Exception;

    void deleteCategory(Long id) throws Exception;
}
