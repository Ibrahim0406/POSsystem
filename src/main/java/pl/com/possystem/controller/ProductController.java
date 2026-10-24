package pl.com.possystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.entity.Product;
import pl.com.possystem.entity.User;
import pl.com.possystem.payload.dto.ProductDTO;
import pl.com.possystem.payload.response.ApiResponse;
import pl.com.possystem.repository.ProductRepository;
import pl.com.possystem.service.ProductService;
import pl.com.possystem.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    private final UserService userService;

    private final ProductRepository productRepository;

    @PostMapping("/create")
    public ResponseEntity<ProductDTO> create(@RequestBody ProductDTO productDTO, @RequestHeader ("Authorization") String jwt) throws Exception {

        User user = userService.getUserFromJwt(jwt);
        return ResponseEntity.ok(productService.createProduct(productDTO, user));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @RequestBody ProductDTO
    productDTO, @RequestHeader ("Authorization") String jwt) throws Exception {

        User user = userService.getUserFromJwt(jwt);
            return ResponseEntity.ok(productService.updateProduct(id, productDTO, user));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity <List<ProductDTO>> getByStoreId(@PathVariable Long storeId, @RequestHeader ("Authorization") String jwt) throws Exception {
        return ResponseEntity.ok(productService.getAllProductsByStoreId(storeId));
    }


    @GetMapping("/{storeId}/search")
    public ResponseEntity <List<ProductDTO>> searchByKeyword(@PathVariable Long storeId,@RequestParam String keyword, @RequestHeader ("Authorization") String jwt) throws Exception {
        return ResponseEntity.ok(productService.searchByKeyword(storeId, keyword));
    }




    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, @RequestHeader ("Authorization") String jwt) throws Exception {
        User user = userService.getUserFromJwt(jwt);
        productService.deleteProduct(id, user);
        ApiResponse response = new ApiResponse();
        response.setMessage("Delete product successfully");

        return ResponseEntity.ok(response);
    }

}
