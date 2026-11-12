package pl.com.possystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.possystem.domain.SupportReportStatus;
import pl.com.possystem.entity.SupportReport;

import java.util.List;

public interface SupportReportRepository extends JpaRepository<SupportReport, Long> {
    List<SupportReport> findAllByOrderByCreatedAtDesc();
    long countByStatus(SupportReportStatus status);
}
