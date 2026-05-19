package com.property.service.impl;

import com.property.dto.request.*;
import com.property.dto.response.ContractVO;
import com.property.entity.Contract;
import com.property.exception.BusinessException;
import com.property.mapper.ContractMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Contract Service Unit Tests")
class ContractServiceImplTest {

    @Mock private ContractMapper contractMapper;
    @InjectMocks private ContractServiceImpl contractService;
    private Contract draft;

    @BeforeEach
    void setUp() {
        draft = new Contract();
        draft.setId("c001");
        draft.setContractNo("HT-2026-001");
        draft.setTitle("Test");
        draft.setType("LEASE");
        draft.setTenantId("t001");
        draft.setTenantName("Zhang");
        draft.setStartDate(LocalDate.of(2026, 1, 1));
        draft.setEndDate(LocalDate.of(2027, 1, 1));
        draft.setAmount(new BigDecimal("5000.00"));
        draft.setStatus("DRAFT");
    }

    @Nested @DisplayName("Create")
    class CreateTests {
        @Test @DisplayName("unique contractNo -> DRAFT")
        void shouldCreateDraft() {
            when(contractMapper.exists(any())).thenReturn(false);
            when(contractMapper.insert(any(Contract.class))).thenReturn(1);
            ContractDTO dto = new ContractDTO();
            dto.setContractNo("HT-2026-001");
            dto.setTitle("New");
            dto.setType("LEASE");
            ContractVO vo = contractService.create(dto);
            assertEquals("DRAFT", vo.getStatus());
            verify(contractMapper).insert(any(Contract.class));
        }

        @Test @DisplayName("duplicate contractNo -> 40925")
        void duplicateNoShouldThrow() {
            when(contractMapper.exists(any())).thenReturn(true);
            ContractDTO dto = new ContractDTO();
            dto.setContractNo("HT-DUP");
            BusinessException ex = assertThrows(BusinessException.class, () -> contractService.create(dto));
            assertEquals(40925, ex.getCode());
        }
    }

    @Nested @DisplayName("Status Flow")
    class StatusFlowTests {
        @Test @DisplayName("DRAFT -> PENDING_APPROVAL")
        void submit() {
            when(contractMapper.selectById("c001")).thenReturn(draft);
            when(contractMapper.updateById(any(Contract.class))).thenReturn(1);
            assertEquals("PENDING_APPROVAL", contractService.submit("c001").getStatus());
        }

        @Test @DisplayName("submit non-DRAFT -> throws")
        void submitNonDraft() {
            draft.setStatus("ACTIVE");
            when(contractMapper.selectById("c001")).thenReturn(draft);
            assertThrows(BusinessException.class, () -> contractService.submit("c001"));
        }

        @Test @DisplayName("PENDING_APPROVAL -> ACTIVE")
        void approve() {
            draft.setStatus("PENDING_APPROVAL");
            when(contractMapper.selectById("c001")).thenReturn(draft);
            when(contractMapper.updateById(any(Contract.class))).thenReturn(1);
            assertEquals("ACTIVE", contractService.approve("c001").getStatus());
        }

        @Test @DisplayName("approve non-PENDING -> throws")
        void approveWrong() {
            when(contractMapper.selectById("c001")).thenReturn(draft);
            assertThrows(BusinessException.class, () -> contractService.approve("c001"));
        }

        @Test @DisplayName("reject with reason -> DRAFT")
        void reject() {
            draft.setStatus("PENDING_APPROVAL");
            when(contractMapper.selectById("c001")).thenReturn(draft);
            when(contractMapper.updateById(any(Contract.class))).thenReturn(1);
            ContractRejectDTO dto = new ContractRejectDTO();
            dto.setRejectReason("Incomplete");
            ContractVO vo = contractService.reject("c001", dto);
            assertEquals("DRAFT", vo.getStatus());
            assertEquals("Incomplete", vo.getRejectReason());
        }

        @Test @DisplayName("reject without reason -> throws")
        void rejectNoReason() {
            draft.setStatus("PENDING_APPROVAL");
            when(contractMapper.selectById("c001")).thenReturn(draft);
            assertThrows(BusinessException.class, () -> contractService.reject("c001", new ContractRejectDTO()));
        }

        @Test @DisplayName("ACTIVE -> TERMINATED")
        void terminate() {
            draft.setStatus("ACTIVE");
            when(contractMapper.selectById("c001")).thenReturn(draft);
            when(contractMapper.updateById(any(Contract.class))).thenReturn(1);
            ContractTerminateDTO dto = new ContractTerminateDTO();
            dto.setTerminateReason("Breach");
            assertEquals("TERMINATED", contractService.terminate("c001", dto).getStatus());
        }

        @Test @DisplayName("terminate without reason -> throws")
        void terminateNoReason() {
            draft.setStatus("ACTIVE");
            when(contractMapper.selectById("c001")).thenReturn(draft);
            assertThrows(BusinessException.class, () -> contractService.terminate("c001", new ContractTerminateDTO()));
        }

        @Test @DisplayName("full lifecycle: DRAFT -> PENDING -> ACTIVE -> TERMINATED")
        void fullLifecycle() {
            when(contractMapper.selectById("c001")).thenReturn(draft);
            when(contractMapper.updateById(any(Contract.class))).thenReturn(1);
            contractService.submit("c001");
            assertEquals("PENDING_APPROVAL", draft.getStatus());
            contractService.approve("c001");
            assertEquals("ACTIVE", draft.getStatus());
            ContractTerminateDTO t = new ContractTerminateDTO();
            t.setTerminateReason("Expired");
            contractService.terminate("c001", t);
            assertEquals("TERMINATED", draft.getStatus());
        }
    }

    @Nested @DisplayName("Renew")
    class RenewTests {
        @Test @DisplayName("later endDate -> success")
        void renewOk() {
            draft.setStatus("ACTIVE");
            when(contractMapper.selectById("c001")).thenReturn(draft);
            when(contractMapper.updateById(any(Contract.class))).thenReturn(1);
            ContractRenewDTO dto = new ContractRenewDTO();
            dto.setEndDate(LocalDate.of(2028, 1, 1));
            assertEquals(LocalDate.of(2028, 1, 1), contractService.renew("c001", dto).getEndDate());
        }

        @Test @DisplayName("earlier endDate -> 40926")
        void renewEarlier() {
            draft.setStatus("ACTIVE");
            when(contractMapper.selectById("c001")).thenReturn(draft);
            ContractRenewDTO dto = new ContractRenewDTO();
            dto.setEndDate(LocalDate.of(2026, 6, 1));
            assertEquals(40926, assertThrows(BusinessException.class, () -> contractService.renew("c001", dto)).getCode());
        }

        @Test @DisplayName("renew non-ACTIVE -> throws")
        void renewNonActive() {
            when(contractMapper.selectById("c001")).thenReturn(draft);
            ContractRenewDTO dto = new ContractRenewDTO();
            dto.setEndDate(LocalDate.of(2028, 1, 1));
            assertThrows(BusinessException.class, () -> contractService.renew("c001", dto));
        }
    }

    @Nested @DisplayName("Delete")
    class DeleteTests {
        @Test @DisplayName("delete DRAFT -> ok")
        void deleteDraft() {
            when(contractMapper.selectById("c001")).thenReturn(draft);
            when(contractMapper.deleteById("c001")).thenReturn(1);
            assertDoesNotThrow(() -> contractService.delete("c001"));
        }

        @Test @DisplayName("delete non-DRAFT -> throws")
        void deleteActive() {
            draft.setStatus("ACTIVE");
            when(contractMapper.selectById("c001")).thenReturn(draft);
            assertThrows(BusinessException.class, () -> contractService.delete("c001"));
        }
    }

    @Test @DisplayName("getById non-existent -> 40422")
    void notFound() {
        when(contractMapper.selectById("xxx")).thenReturn(null);
        assertEquals(40422, assertThrows(BusinessException.class, () -> contractService.getById("xxx")).getCode());
    }
}
