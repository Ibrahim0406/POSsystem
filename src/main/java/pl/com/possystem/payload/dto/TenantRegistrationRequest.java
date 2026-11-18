package pl.com.possystem.payload.dto;

import lombok.Data;
import pl.com.possystem.entity.StoreContact;

@Data
public class TenantRegistrationRequest {

    private String ownerFullName;
    private String ownerEmail;
    private String ownerPassword;
    private String ownerPhone;
    private String storeBrand;
    private String storeDescription;
    private String storeType;
    private StoreContact contact;
}
