package pl.com.possystem.mapper;

import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Store;
import pl.com.possystem.payload.dto.BranchDTO;

public class BranchMapper {

    public static BranchDTO toDTO(Branch branch){
        return BranchDTO.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .phoneNumber(branch.getPhoneNumber())
                .email(branch.getEmail())
                .workingDays(branch.getWorkingDays())
                .openTime(branch.getOpenTime())
                .closeTime(branch.getCloseTime())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .storeId(branch.getStore() != null ? branch.getStore().getId() : null)
                //.manager(branch.getManager() != null ? UserMapper.toDTO(branch.getManager()) : null)
                .build();
    }



    public static Branch toEntity(BranchDTO branchDTO, Store store) {
        return Branch.builder()
                .name(branchDTO.getName())
                .address(branchDTO.getAddress())
                .phoneNumber(branchDTO.getPhoneNumber())
                .email(branchDTO.getEmail())
                .workingDays(branchDTO.getWorkingDays())
                .openTime(branchDTO.getOpenTime())
                .closeTime(branchDTO.getCloseTime())
                .store(store)
                .createdAt(branchDTO.getCreatedAt())
                .updatedAt(branchDTO.getUpdatedAt())
                //.store(branchDTO.getStore() != null ? StoreMapper.toEntity(branchDTO.getStore()) : null)
                //.manager(branchDTO.getManager() != null ? UserMapper.toEntity(branchDTO.getManager()) : null)
                .build();
    }
}
