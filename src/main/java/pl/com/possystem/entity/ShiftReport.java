package pl.com.possystem.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "shift_reports")
public class ShiftReport {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


}
