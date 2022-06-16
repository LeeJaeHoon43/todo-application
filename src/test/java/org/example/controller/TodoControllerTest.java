package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.TodoEntity;
import org.example.model.TodoRequest;
import org.example.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    TodoService todoService;

    private TodoEntity expected;

    @BeforeEach
    void setUp(){
        this.expected = new TodoEntity();
        this.expected.setId(123L);
        this.expected.setTitle("TEST TITLE");
        this.expected.setOrder(0L);
        this.expected.setCompleted(false);
    }

    @Test
    void create() throws Exception {
        Mockito.when(this.todoService.add(ArgumentMatchers.any(TodoRequest.class)))
                .then((i) -> {
                    TodoRequest request = i.getArgument(0, TodoRequest.class);
                    return new TodoEntity(this.expected.getId(), request.getTitle(), request.getOrder(), request.getCompleted());
                });

        TodoRequest request = new TodoRequest();
        request.setTitle(this.expected.getTitle());

        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);

        mvc.perform(MockMvcRequestBuilders.post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expected.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(expected.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.order").value(expected.getOrder()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.completed").value(expected.getCompleted()));
    }

    @Test
    void readOne() throws Exception {
        BDDMockito.given(todoService.searchById(123L)).willReturn(expected);

        mvc.perform(MockMvcRequestBuilders.get("/123"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expected.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(expected.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.order").value(expected.getOrder()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.completed").value(expected.getCompleted()));
    }

    @Test
    void readOneException() throws Exception {
        BDDMockito.given(todoService.searchById(123L)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mvc.perform(MockMvcRequestBuilders.get("/123"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void readAll() throws Exception {
        List<TodoEntity> mockList = new ArrayList<>();
        int expectedLength = 10;
        for (int i = 0; i < expectedLength; i++) {
            mockList.add(Mockito.mock(TodoEntity.class));
        }

        BDDMockito.given(todoService.searchAll()).willReturn(mockList);

        mvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(expectedLength));
    }

    @Test
    void deleteAll() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}