package com.yourcompany.workforcemgmt.service;

import com.yourcompany.workforcemgmt.dto.*;
import com.yourcompany.workforcemgmt.model.*;
import com.yourcompany.workforcemgmt.util.TaskMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final Map<Long, Task> tasks = new HashMap<>();
    private long taskIdCounter = 1;

    public TaskDto createTask(CreateTaskRequest request) {
        Task task = new Task(taskIdCounter++, request.getTitle(), request.getAssigneeId(), TaskStatus.OPEN, LocalDate.now(), null, request.getPriority());
        task.addHistory("Task created.");
        tasks.put(task.getId(), task);
        return TaskMapper.toDto(task);
    }

    public TaskDto reassignTask(Long taskId, Long newAssigneeId) {
        Task oldTask = tasks.get(taskId);
        oldTask.setStatus(TaskStatus.CANCELLED);
        oldTask.addHistory("Task reassigned to ID " + newAssigneeId);

        Task newTask = new Task(taskIdCounter++, oldTask.getTitle(), newAssigneeId, TaskStatus.OPEN, LocalDate.now(), null, oldTask.getPriority());
        newTask.addHistory("Created by reassignment from task " + taskId);
        tasks.put(newTask.getId(), newTask);
        return TaskMapper.toDto(newTask);
    }

    public List<TaskDto> getTasksBetween(LocalDate start, LocalDate end) {
        return tasks.values().stream()
                .filter(t -> t.getStatus() != TaskStatus.CANCELLED &&
                        (t.getStartDate().isAfter(start.minusDays(1)) && t.getStartDate().isBefore(end.plusDays(1)) ||
                        (t.getStartDate().isBefore(start) && t.getStatus() == TaskStatus.OPEN)))
                .map(TaskMapper::toDto)
                .collect(Collectors.toList());
    }

    public void updateTaskPriority(Long taskId, TaskPriority priority) {
        Task task = tasks.get(taskId);
        task.setPriority(priority);
        task.addHistory("Priority updated to " + priority);
    }

    public List<TaskDto> getTasksByPriority(TaskPriority priority) {
        return tasks.values().stream()
                .filter(t -> t.getPriority() == priority && t.getStatus() != TaskStatus.CANCELLED)
                .map(TaskMapper::toDto)
                .collect(Collectors.toList());
    }

    public void addComment(Long taskId, String comment) {
        Task task = tasks.get(taskId);
        task.getComments().add(comment);
        task.addHistory("Comment added: " + comment);
    }

    public TaskDto getTaskDetails(Long taskId) {
        return TaskMapper.toDto(tasks.get(taskId));
    }
}
