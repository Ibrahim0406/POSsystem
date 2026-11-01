package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.ShiftReport;
import pl.com.possystem.entity.User;
import pl.com.possystem.mapper.ShiftReportMapper;
import pl.com.possystem.payload.dto.ShiftReportDTO;
import pl.com.possystem.repository.BranchRepository;
import pl.com.possystem.repository.ShiftReportRepository;
import pl.com.possystem.service.ShiftReportService;
import pl.com.possystem.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShiftReportServiceImpl implements ShiftReportService {

    private final ShiftReportRepository shiftReportRepository;

    private final UserService userService;
    private final BranchRepository branchRepository;

    @Override
    public ShiftReportDTO startShift(Long cashierId, Long branchId, LocalDateTime shiftStart) throws Exception {

        User currentUser = userService.getCurrentUser();
        shiftStart = LocalDateTime.now();
        LocalDateTime startOfDay =shiftStart.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = shiftStart.withHour(23).withMinute(59).withSecond(59);

        Optional<ShiftReport> exsisting = shiftReportRepository.findByCashierAndShiftStartBetween(currentUser, startOfDay, endOfDay);

        if (exsisting.isPresent()) {
            throw new Exception("Shift already started for today");
        }

        Branch branch = currentUser.getBranch();

        ShiftReport shiftReport = ShiftReport.builder()
                .cashier(currentUser)
                .branch(branch)
                .shiftStart(shiftStart)
                .build();

        ShiftReport savedShiftReport = shiftReportRepository.save(shiftReport);

        return ShiftReportMapper.toDTO( savedShiftReport);
    }

    @Override
    public ShiftReportDTO endShift(Long shiftReportId, LocalDateTime shiftEnd) throws Exception {
        return null;
    }

    @Override
    public ShiftReportDTO getShiftReportById(Long id) throws Exception {
        return null;
    }

    @Override
    public List<ShiftReportDTO> getAllShiftReports() {
        return List.of();
    }

    @Override
    public List<ShiftReportDTO> getShiftReportsByBranchId(Long branchId) {
        return List.of();
    }

    @Override
    public List<ShiftReportDTO> getShiftReportsByCashierId(Long cashierId) {
        return List.of();
    }

    @Override
    public ShiftReportDTO getCurrentShiftProgress(Long cashierId) throws Exception {
        return null;
    }

    @Override
    public ShiftReportDTO getShiftByCashierAndDate(Long cashierId, LocalDateTime date) throws Exception {
        return null;
    }
}
