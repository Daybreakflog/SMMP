package com.property.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.property.dto.request.WorkOrderAssignDTO;
import com.property.dto.request.WorkOrderDTO;
import com.property.dto.response.WorkOrderVO;
import com.property.entity.WorkOrder;
import com.property.exception.BusinessException;
import com.property.mapper.WoAttachmentMapper;
import com.property.mapper.WoMessageMapper;
import com.property.mapper.WoTimelineMapper;
import com.property.mapper.WorkOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import com.property.entity.WoTimeline;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkOrder Service Unit Tests")
class WorkOrderServiceImplTest {

    @Mock private WorkOrderMapper workOrderMapper;
    @Mock private WoTimelineMapper woTimelineMapper;
    @Mock private WoMessageMapper woMessageMapper;
    @Mock private WoAttachmentMapper woAttachmentMapper;
    @Mock private ObjectMapper objectMapper;
    @InjectMocks private WorkOrderServiceImpl workOrderService;

    private WorkOrder pending;

    @BeforeEach
    void setUp() {
        pending = new WorkOrder();
        pending.setId("wo001");
        pending.setNo("WO1234567890");
        pending.setTitle("Leak");
        pending.setStatus("PENDING");
        pending.setCategory("REPAIR");
        pending.setProjectId("p001");
    }

    @Nested @DisplayName("Create")
    class CreateTests {
        @Test @DisplayName("create -> PENDING")
        void shouldCreatePending() {
            when(workOrderMapper.insert(any(WorkOrder.class))).thenReturn(1);
            WorkOrderDTO dto = new WorkOrderDTO();
            dto.setTitle("New issue");
            dto.setCategory("REPAIR");
            WorkOrderVO vo = workOrderService.create(dto);
            assertEquals("PENDING", vo.getStatus());
            assertNotNull(vo.getNo());
            verify(workOrderMapper).insert(any(WorkOrder.class));
        }
    }

    @Nested @DisplayName("GetById")
    class GetByIdTests {
        @Test @DisplayName("existing -> returns VO")
        void found() {
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            WorkOrderVO vo = workOrderService.getById("wo001");
            assertEquals("wo001", vo.getId());
        }

        @Test @DisplayName("non-existent -> throws")
        void notFound() {
            when(workOrderMapper.selectById("xxx")).thenReturn(null);
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> workOrderService.getById("xxx"));
            assertEquals(40405, ex.getCode());
        }
    }

    @Nested @DisplayName("Assign")
    class AssignTests {
        @Test @DisplayName("PENDING -> ASSIGNED")
        void assignFromPending() {
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
            when(woTimelineMapper.insert(any(WoTimeline.class))).thenReturn(1);
            WorkOrderAssignDTO dto = new WorkOrderAssignDTO();
            dto.setAssigneeId("m001");
            WorkOrderVO vo = workOrderService.assign("wo001", dto);
            assertEquals("ASSIGNED", vo.getStatus());
            assertEquals("m001", vo.getMaintainerId());
        }
    }

    @Nested @DisplayName("Start")
    class StartTests {
        @Test @DisplayName("PENDING -> IN_PROGRESS")
        void startFromPending() {
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
            when(woTimelineMapper.insert(any(WoTimeline.class))).thenReturn(1);
            WorkOrderVO vo = workOrderService.start("wo001");
            assertEquals("IN_PROGRESS", vo.getStatus());
        }

        @Test @DisplayName("ASSIGNED -> IN_PROGRESS")
        void startFromAssigned() {
            pending.setStatus("ASSIGNED");
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
            when(woTimelineMapper.insert(any(WoTimeline.class))).thenReturn(1);
            WorkOrderVO vo = workOrderService.start("wo001");
            assertEquals("IN_PROGRESS", vo.getStatus());
        }

        @Test @DisplayName("IN_PROGRESS -> start throws STATUS_ILLEGAL")
        void startFromInProgress() {
            pending.setStatus("IN_PROGRESS");
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> workOrderService.start("wo001"));
            assertEquals(40005, ex.getCode());
        }

        @Test @DisplayName("DONE -> start throws STATUS_ILLEGAL")
        void startFromDone() {
            pending.setStatus("DONE");
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            assertThrows(BusinessException.class, () -> workOrderService.start("wo001"));
        }

        @Test @DisplayName("CLOSED -> start throws STATUS_ILLEGAL")
        void startFromClosed() {
            pending.setStatus("CLOSED");
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            assertThrows(BusinessException.class, () -> workOrderService.start("wo001"));
        }
    }

    @Nested @DisplayName("Complete")
    class CompleteTests {
        @Test @DisplayName("IN_PROGRESS -> DONE")
        void completeOk() {
            pending.setStatus("IN_PROGRESS");
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
            when(woTimelineMapper.insert(any(WoTimeline.class))).thenReturn(1);
            WorkOrderVO vo = workOrderService.complete("wo001");
            assertEquals("DONE", vo.getStatus());
            assertNotNull(vo.getCompletedAt());
        }

        @Test @DisplayName("PENDING -> complete throws")
        void completeFromPending() {
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            assertThrows(BusinessException.class, () -> workOrderService.complete("wo001"));
        }

        @Test @DisplayName("ASSIGNED -> complete throws")
        void completeFromAssigned() {
            pending.setStatus("ASSIGNED");
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            assertThrows(BusinessException.class, () -> workOrderService.complete("wo001"));
        }
    }

    @Nested @DisplayName("Close")
    class CloseTests {
        @Test @DisplayName("DONE -> CLOSED")
        void closeFromDone() {
            pending.setStatus("DONE");
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
            when(woTimelineMapper.insert(any(WoTimeline.class))).thenReturn(1);
            WorkOrderVO vo = workOrderService.close("wo001");
            assertEquals("CLOSED", vo.getStatus());
        }

        @Test @DisplayName("PENDING -> CLOSED (close has no guard)")
        void closeFromPending() {
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
            when(woTimelineMapper.insert(any(WoTimeline.class))).thenReturn(1);
            WorkOrderVO vo = workOrderService.close("wo001");
            assertEquals("CLOSED", vo.getStatus());
        }
    }

    @Nested @DisplayName("Reopen")
    class ReopenTests {
        @Test @DisplayName("CLOSED -> PENDING")
        void reopenOk() {
            pending.setStatus("CLOSED");
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
            when(woTimelineMapper.insert(any(WoTimeline.class))).thenReturn(1);
            WorkOrderVO vo = workOrderService.reopen("wo001");
            assertEquals("PENDING", vo.getStatus());
        }

        @Test @DisplayName("PENDING -> reopen throws")
        void reopenFromPending() {
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            assertThrows(BusinessException.class, () -> workOrderService.reopen("wo001"));
        }

        @Test @DisplayName("IN_PROGRESS -> reopen throws")
        void reopenFromInProgress() {
            pending.setStatus("IN_PROGRESS");
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            assertThrows(BusinessException.class, () -> workOrderService.reopen("wo001"));
        }
    }

    @Nested @DisplayName("Full Lifecycle")
    class LifecycleTests {
        @Test @DisplayName("PENDING -> ASSIGNED -> IN_PROGRESS -> DONE -> CLOSED -> PENDING (reopen)")
        void fullCycle() {
            when(workOrderMapper.selectById("wo001")).thenReturn(pending);
            when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
            when(woTimelineMapper.insert(any(WoTimeline.class))).thenReturn(1);

            WorkOrderAssignDTO assignDto = new WorkOrderAssignDTO();
            assignDto.setAssigneeId("m001");
            workOrderService.assign("wo001", assignDto);
            assertEquals("ASSIGNED", pending.getStatus());

            workOrderService.start("wo001");
            assertEquals("IN_PROGRESS", pending.getStatus());

            workOrderService.complete("wo001");
            assertEquals("DONE", pending.getStatus());

            workOrderService.close("wo001");
            assertEquals("CLOSED", pending.getStatus());

            workOrderService.reopen("wo001");
            assertEquals("PENDING", pending.getStatus());
        }
    }
}
