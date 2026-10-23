package pl.com.possystem.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.entity.User;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.mapper.StoreMapper;
import pl.com.possystem.payload.dto.StoreDto;
import pl.com.possystem.payload.response.ApiResponse;
import pl.com.possystem.service.StoreService;
import pl.com.possystem.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store")
public class StoreController {

    private final StoreService storeService;

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<StoreDto> createStore(@RequestBody StoreDto storeDto, @RequestHeader ("Authorization") String jwt)
            throws UserException {
        User user = userService.getUserFromJwt(jwt);
        return ResponseEntity.ok(storeService.createStore(storeDto, user));
    }


    @GetMapping("/{id}")
    public ResponseEntity<StoreDto> getStoreById(@PathVariable Long id, @RequestHeader ("Authorization") String jwt)
            throws Exception {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    @GetMapping
    public ResponseEntity<List<StoreDto>> getAllStores(@RequestHeader ("Authorization") String jwt) throws Exception {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    @GetMapping("/admin")
    public ResponseEntity<StoreDto> getStoreByAdmin(@RequestHeader ("Authorization") String jwt) throws Exception {
        return ResponseEntity.ok(StoreMapper.toDTO(storeService.getStoreByAdmin()));
    }

    @GetMapping("/employee")
    public ResponseEntity<StoreDto> getStoreByEmployee(@RequestHeader ("Authorization") String jwt) throws Exception {
            return ResponseEntity.ok(storeService.getStoreByEmployee());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<StoreDto> updateStore(@PathVariable Long id,
                                                @RequestBody StoreDto storeDto,
                                                @RequestHeader ("Authorization") String jwt) throws UserException {
        return ResponseEntity.ok(storeService.updateStore(id, storeDto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteStore(@PathVariable Long id) throws UserException {
        storeService.deleteStore(id);
        ApiResponse response = new ApiResponse();
        response.setMessage("Store deleted successfully");
        return ResponseEntity.ok(response);
    }


    @PutMapping("/moderate/{id}")
    public ResponseEntity<StoreDto> moderateStore(@PathVariable Long id,
                                                    @RequestParam StoreStatus status,
                                                    @RequestHeader ("Authorization") String jwt) throws UserException {
        StoreDto storeDto = storeService.moderateStore(id, status);
        return ResponseEntity.ok(storeDto);
    }


}
