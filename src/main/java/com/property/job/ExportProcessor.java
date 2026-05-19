package com.property.job;

import com.property.entity.ExportTask;
import com.property.mapper.ExportTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExportProcessor {

    private final ExportTaskMapper exportTaskMapper;

    @Async
    public void process(String taskId) {
        ExportTask task = exportTaskMapper.selectById(taskId);
        if (task == null) return;

        task.setStatus("RUNNING");
        task.setStartedAt(LocalDateTime.now());
        exportTaskMapper.updateById(task);

        try {
            Thread.sleep(2000);
            task.setStatus("DONE");
            task.setFileUrl("https://oss.example.com/exports/" + taskId + ".xlsx");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.setStatus("FAILED");
            task.setErrorMsg(e.getMessage());
        } catch (Exception e) {
            log.error("Export task {} failed", taskId, e);
            task.setStatus("FAILED");
            task.setErrorMsg(e.getMessage());
        }

        task.setFinishedAt(LocalDateTime.now());
        exportTaskMapper.updateById(task);
    }
}
