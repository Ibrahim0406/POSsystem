package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.StoreContact;
import pl.com.possystem.entity.User;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.mapper.StoreMapper;
import pl.com.possystem.payload.dto.StoreDto;
import pl.com.possystem.repository.StoreRepository;
import pl.com.possystem.service.StoreService;
import pl.com.possystem.service.UserService;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    private final UserService userService;



    @Override
    public StoreDto createStore(StoreDto storeDto, User user) {

        Store store = StoreMapper.toEntity(storeDto, user);
        return StoreMapper.toDTO(storeRepository.save(store));
    }

    @Override
    public StoreDto getStoreById(Long id) throws Exception {
        Store store = storeRepository.findById(id).orElseThrow(() -> new Exception ("Store not found with id: " + id));

        return StoreMapper.toDTO(store);
    }

    @Override
    public List<StoreDto> getAllStores() {
        List<Store> dtos =  storeRepository.findAll();
        return dtos.stream().map(StoreMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Store getStoreByAdmin() throws UserException {
        User admin = userService.getCurrentUser();
        return storeRepository.findByStoreAdminId(admin.getId());
    }

    @Override
    public StoreDto updateStore(Long id, StoreDto storeDto) throws UserException {

        User currentUser = userService.getCurrentUser();

        Store existing = storeRepository.findByStoreAdminId(currentUser.getId());

        if (existing == null) {
            throw new UserException("Store not found for the current user");
        }

        existing.setBrand(storeDto.getBrand());
        existing.setDescription(storeDto.getDescription());

        if (storeDto.getStoreType() != null) {
            existing.setStoreType(storeDto.getStoreType());
        }

        if (storeDto.getContact() != null){
            StoreContact contact = StoreContact.builder()
                    .address(storeDto.getContact().getAddress())
                    .phone(storeDto.getContact().getPhone())
                    .email(storeDto.getContact().getEmail())
                    .build();
            existing.setContact(contact);
        }
        Store updatedStore = storeRepository.save(existing);
        return StoreMapper.toDTO(updatedStore);
    }

    @Override
    public void deleteStore(Long id) throws UserException {
        Store store = getStoreByAdmin();
        storeRepository.delete(store);

    }

    @Override
    public StoreDto getStoreByEmployee() throws UserException {

        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            throw new UserException("Don't have permission to access this resource");
        }
        return StoreMapper.toDTO(currentUser.getStore());
    }

    @Override
    public StoreDto moderateStore(Long id, StoreStatus status) throws UserException {
        Store store = storeRepository.findById(id).orElseThrow(() -> new UserException("Store not found with id: " + id));
        store.setStatus(status);

        Store updatedStore = storeRepository.save(store);
        return StoreMapper.toDTO(updatedStore);
    }
}
