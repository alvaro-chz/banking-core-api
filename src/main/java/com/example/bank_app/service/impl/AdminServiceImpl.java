package com.example.bank_app.service.impl;

import com.example.bank_app.dto.admin.AdminDashboardResponse;
import com.example.bank_app.model.LoginAttempt;
import com.example.bank_app.repository.BankTransactionRepository;
import com.example.bank_app.repository.LoginAttemptRepository;
import com.example.bank_app.repository.UserRepository;
import com.example.bank_app.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final BankTransactionRepository bankTransactionRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final UserRepository userRepository;

    @Override
    public AdminDashboardResponse getDashboard() {
        Long retainedUsers = bankTransactionRepository.getRetainedUsers();
        Long totalBlocked = loginAttemptRepository.countByIsBlockedTrue();
        Long totalUsers = userRepository.countByRole_Name("CLIENT");

        List<LoginAttempt> blockedAttempts = loginAttemptRepository.findLastBlockedUsers(PageRequest.of(0, 3));
        List<AdminDashboardResponse.BlockedUserSummary> blockedUsers = blockedAttempts.stream()
                .map(loginAttempt -> new AdminDashboardResponse.BlockedUserSummary(
                        loginAttempt.getUser().getName() + " " + loginAttempt.getUser().getLastName1(),
                        loginAttempt.getUser().getDocumentId(),
                        loginAttempt.getLastAttempt()
                ))
                .toList();

        List<Object[]> rawData = bankTransactionRepository.getTransactionCurveDataGroupedByCurrency();

        Map<String, List<AdminDashboardResponse.ChartDataPoint>> curveMap = rawData.stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[1],
                        Collectors.mapping(
                                row -> new AdminDashboardResponse.ChartDataPoint(
                                        (LocalDate) row[0],
                                        (BigDecimal) row[2]
                                ),
                                Collectors.toList()
                        )
                ));

        return new AdminDashboardResponse(
                retainedUsers != null ? retainedUsers : 0L,
                totalUsers,
                totalBlocked,
                blockedUsers,
                curveMap
        );
    }
}
