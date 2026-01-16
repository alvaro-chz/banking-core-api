package com.example.bank_app.service.impl;

import com.example.bank_app.dto.admin.AdminDashboardResponse;
import com.example.bank_app.model.LoginAttempt;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.BankTransactionRepository;
import com.example.bank_app.repository.LoginAttemptRepository;
import com.example.bank_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private BankTransactionRepository bankTransactionRepository;
    @Mock private LoginAttemptRepository loginAttemptRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AdminServiceImpl adminService;

    @Test
    void getDashboard_ShouldReturnCompleteData_WhenRepositoriesReturnValues() {
        // --- GIVEN ---

        when(bankTransactionRepository.getRetainedUsers()).thenReturn(150L);
        when(loginAttemptRepository.countByIsBlockedTrue()).thenReturn(5L);
        when(userRepository.countByRole_Name("CLIENT")).thenReturn(2L);

        User user1 = User.builder().documentId("123").name("Juan").lastName1("Perez").build();
        User user2 = User.builder().documentId("456").name("Maria").lastName1("Gomez").build();

        LoginAttempt attempt1 = LoginAttempt.builder()
                .user(user1)
                .lastAttempt(LocalDateTime.now().minusMinutes(10))
                .build();
        LoginAttempt attempt2 = LoginAttempt.builder()
                .user(user2)
                .lastAttempt(LocalDateTime.now().minusMinutes(20))
                .build();

        // Nota: El servicio usa PageRequest.of(0, 3) hardcoded
        when(loginAttemptRepository.findLastBlockedUsers(PageRequest.of(0, 3)))
                .thenReturn(List.of(attempt1, attempt2));

        LocalDate today = LocalDate.now();
        LocalDate yesterday = LocalDate.now().minusDays(1);

        Object[] row1 = { yesterday, "USD", new BigDecimal("100.00") };
        Object[] row2 = { today,     "USD", new BigDecimal("200.00") };
        Object[] row3 = { today,     "PEN", new BigDecimal("50.00") };

        List<Object[]> rawChartData = List.of(row1, row2, row3);

        when(bankTransactionRepository.getTransactionCurveDataGroupedByCurrency())
                .thenReturn(rawChartData);

        // --- WHEN ---
        AdminDashboardResponse response = adminService.getDashboard();

        // --- THEN ---

        assertThat(response.retainedUsersCount()).isEqualTo(150L);
        assertThat(response.totalBlockedUsersCount()).isEqualTo(5L);
        assertThat(response.totalUsers()).isEqualTo(2L);

        // Validar lista de bloqueados (Mapping correcto de nombre y apellido)
        assertThat(response.lastUsersBlocked()).hasSize(2);
        assertThat(response.lastUsersBlocked().get(0).name()).isEqualTo("Juan Perez");
        assertThat(response.lastUsersBlocked().get(0).documentId()).isEqualTo("123");
        assertThat(response.lastUsersBlocked().get(1).name()).isEqualTo("Maria Gomez");

        // Validar Gráfica (Grouping by Currency)
        assertThat(response.transactionCurve()).hasSize(2);
        assertThat(response.transactionCurve()).containsKeys("USD", "PEN");

        // Validar datos dentro de la moneda USD
        List<AdminDashboardResponse.ChartDataPoint> usdPoints = response.transactionCurve().get("USD");
        assertThat(usdPoints).hasSize(2);
        assertThat(usdPoints.get(0).amount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(usdPoints.get(1).amount()).isEqualTo(new BigDecimal("200.00"));

        // Validar datos dentro de la moneda PEN
        List<AdminDashboardResponse.ChartDataPoint> penPoints = response.transactionCurve().get("PEN");
        assertThat(penPoints).hasSize(1);
        assertThat(penPoints.get(0).amount()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void getDashboard_ShouldHandleNullsAndEmptyLists() {
        // --- GIVEN ---
        // Escenario donde la DB devuelve nulls o listas vacías
        when(bankTransactionRepository.getRetainedUsers()).thenReturn(null);
        when(loginAttemptRepository.countByIsBlockedTrue()).thenReturn(0L);

        when(loginAttemptRepository.findLastBlockedUsers(any())).thenReturn(Collections.emptyList());
        when(bankTransactionRepository.getTransactionCurveDataGroupedByCurrency()).thenReturn(Collections.emptyList());
        when(userRepository.countByRole_Name("CLIENT")).thenReturn(0L);

        // --- WHEN ---
        AdminDashboardResponse response = adminService.getDashboard();

        // --- THEN ---
        assertThat(response.retainedUsersCount()).isZero();
        assertThat(response.totalBlockedUsersCount()).isZero();
        assertThat(response.lastUsersBlocked()).isEmpty();
        assertThat(response.transactionCurve()).isEmpty();
        assertThat(response.totalUsers()).isZero();
    }
}