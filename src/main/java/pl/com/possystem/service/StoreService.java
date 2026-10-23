package pl.com.possystem.service;


import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.User;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.payload.dto.StoreDto;

import java.util.List;

public interface StoreService {

    StoreDto createStore(StoreDto storeDto, User user);

    StoreDto getStoreById(Long id) throws Exception;

    List<StoreDto> getAllStores();

    Store getStoreByAdmin() throws UserException;

    StoreDto updateStore(Long id, StoreDto storeDto) throws UserException;

    void deleteStore(Long id) throws UserException;

    StoreDto getStoreByEmployee() throws UserException;

    StoreDto moderateStore(Long id, StoreStatus status) throws UserException;

}
