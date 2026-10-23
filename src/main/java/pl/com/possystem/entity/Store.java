package pl.com.possystem.entity;


import jakarta.persistence.*;
import lombok.*;
import pl.com.possystem.domain.StoreStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "stores")
public class Store {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String brand;

    @OneToOne
    private User storeAdmin;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String description;

    private String storeType;

    private StoreStatus status;

    @Embedded
    private StoreContact contact = new StoreContact();


    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        status = StoreStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

}
