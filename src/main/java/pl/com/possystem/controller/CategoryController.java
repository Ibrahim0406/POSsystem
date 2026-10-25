package pl.com.possystem.controller;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.payload.dto.CategoryDTO;
import pl.com.possystem.payload.response.ApiResponse;
import pl.com.possystem.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {


    private final CategoryService categoryService;

    private final UserController userController;

    @PostMapping("/create")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) throws Exception {

        return ResponseEntity.ok(categoryService.createCategory(categoryDTO));
    }

    @GetMapping("store/{storeId}")
    public ResponseEntity<List<CategoryDTO>> getCategoryByStoreId(@PathVariable Long storeId)  throws  Exception {
        return ResponseEntity.ok(categoryService.getCategoriesByStore(storeId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) throws Exception {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) throws Exception {
        categoryService.deleteCategory(id);

        ApiResponse response = new ApiResponse();
        response.setMessage("Category deleted successfully");

        return ResponseEntity.ok(response);
    }
}
