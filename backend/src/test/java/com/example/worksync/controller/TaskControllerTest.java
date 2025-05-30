package com.example.worksync.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import com.example.worksync.dto.requests.TaskDTO;
import com.example.worksync.model.User;
import com.example.worksync.repository.TaskRepository;
import com.example.worksync.service.TaskService;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @Mock
    private TaskRepository taskRepository;

    @Test
    void testListTasksByProject() {
        Long projectId = 1L;
        List<TaskDTO> tasks = List.of(new TaskDTO());
        when(taskService.listTasksByProject(projectId)).thenReturn(tasks);

        ResponseEntity<List<TaskDTO>> response = taskController.listTasksByProject(projectId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(tasks, response.getBody());
    }

    @Test
    void testGetTaskById_Found() {
        Long taskId = 1L;
        TaskDTO taskDTO = new TaskDTO();
        when(taskService.findById(taskId)).thenReturn(Optional.of(taskDTO));

        ResponseEntity<TaskDTO> response = taskController.getTaskById(taskId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(taskDTO, response.getBody());
    }

    @Test
    void testGetTaskById_NotFound() {
        Long taskId = 1L;
        when(taskService.findById(taskId)).thenReturn(Optional.empty());

        ResponseEntity<TaskDTO> response = taskController.getTaskById(taskId);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testCreateTask_Success() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Test Task"); // Garante que title está preenchido
        taskDTO.setDescription("Description");

        User user = new User(); // Criar ou mockar User

        // Mockando um BindingResult sem erros
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        when(taskService.createTask(taskDTO, user)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskController.createTask(taskDTO, user, bindingResult);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(taskDTO, response.getBody());
    }

    @Test
    void testUpdateTask() {
        Long taskId = 1L;
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Updated Task");
        taskDTO.setDescription("Updated Description");

        when(taskService.updateTask(anyLong(), any(TaskDTO.class))).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskController.updateTask(taskId, taskDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taskDTO, response.getBody());

        verify(taskService).updateTask(taskId, taskDTO);
    }

    @Test
    void testDeleteTask() {
        // Mocka a existência da tarefa
        when(taskRepository.existsById(1L)).thenReturn(true);

        // Não precisa mockar taskService.deleteTask porque o Controller não o usa
        // diretamente

        ResponseEntity<Void> response = taskController.deleteTask(1L);

        assertEquals(204, response.getStatusCode().value());

        // Verifica se realmente foi chamado o deleteById
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void testCreateTask_WithValidationErrors() {
        TaskDTO taskDTO = new TaskDTO();
        User user = new User();

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        ResponseEntity<TaskDTO> response = taskController.createTask(taskDTO, user, bindingResult);

        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testDeleteTask_NotFound() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            taskController.deleteTask(1L);
        });

        assertEquals("Task not found!", exception.getMessage());
    }

    @Test
    void testSearchTasks_NoResults() {
        when(taskService.searchTasks(any(), any(), any(), any(), any())).thenReturn(List.of());

        ResponseEntity<List<TaskDTO>> response = taskController.searchTasks(null, null, null, null, null);

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testSearchTasks_WithAllParameters() {
        List<TaskDTO> tasks = List.of(new TaskDTO());

        String title = "Project Task";
        LocalDate startDateMin = LocalDate.now();
        LocalDate startDateMax = LocalDate.now().plusDays(7);
        Long creatorId = 1L;
        Long assignedPersonId = 2L;

        when(taskService.searchTasks("project task", startDateMin, startDateMax, creatorId, assignedPersonId))
                .thenReturn(tasks);

        ResponseEntity<List<TaskDTO>> response = taskController.searchTasks(
                title, startDateMin, creatorId, assignedPersonId, startDateMax);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(tasks, response.getBody());

        verify(taskService).searchTasks(
                "project task", startDateMin, startDateMax, creatorId, assignedPersonId);
    }

    @Test
    void testSearchTasks_WithException() {
        when(taskService.searchTasks(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<List<TaskDTO>> response = taskController.searchTasks(null, null, null, null, null);

        assertEquals(500, response.getStatusCode().value());
    }

}
