package com.elemental.tech.workitems.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.elemental.tech.workitems.api.dto.CreateWorkItemRequest;
import com.elemental.tech.workitems.api.dto.UpdateStatusRequest;
import com.elemental.tech.workitems.api.dto.WorkItemResponse;
import com.elemental.tech.workitems.domain.WorkItem.Status;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class WorkItemControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    private WorkItemResponse sendCreateRequest(String title, String description, CreateWorkItemRequest.Priority priority) throws Exception {
        CreateWorkItemRequest request = new CreateWorkItemRequest(
            "Hello",
            "World",
            CreateWorkItemRequest.Priority.MEDIUM
        );

        MvcResult result = mvc.perform(
                post("/api/work-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isCreated())
            .andReturn();

        String responseString = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseString, WorkItemResponse.class);
    }

    private WorkItemResponse sendCreateRequest() throws Exception {
        return sendCreateRequest("Hello", "World", CreateWorkItemRequest.Priority.MEDIUM);
    }

    @Test
    void createNewWorkItem() throws Exception {
        WorkItemResponse response = sendCreateRequest();

        assertEquals("Hello", response.title());
        assertEquals("World", response.description());
        assertEquals(CreateWorkItemRequest.Priority.MEDIUM.toString(), response.priority());
        assertEquals(Status.OPEN.toString(), response.status());
    }

    @Test
    void createAndFindWorkItem() throws Exception {
        WorkItemResponse createResponse = sendCreateRequest();

        MvcResult getResult = mvc.perform(get("/api/work-items/" + createResponse.id()))
            .andExpect(status().isOk())
            .andReturn();
        String getResponseString = getResult.getResponse().getContentAsString();
        WorkItemResponse getResponse = objectMapper.readValue(getResponseString, WorkItemResponse.class);

        // I had odd mis-matches between formatting of the two Instants in the different responses
        // not sure why, as the formatting is done by the same mapper
        assertEquals(createResponse.id(), getResponse.id());
        assertEquals(createResponse.title(), getResponse.title());
        assertEquals(createResponse.description(), getResponse.description());
        assertEquals(createResponse.priority().toString(), getResponse.priority());
        assertEquals(createResponse.status(), getResponse.status());
    }

    @Test
    void createAndListWorkItems() throws Exception {
        sendCreateRequest();
        sendCreateRequest("foo", "bar", CreateWorkItemRequest.Priority.LOW);

        MvcResult getResult = mvc.perform(
            get("/api/work-items"))
            .andExpect(status().isOk())
            .andReturn();
        String getResponseString = getResult.getResponse().getContentAsString();
        List<WorkItemResponse> getResponse = objectMapper.readValue(getResponseString, new TypeReference<List<WorkItemResponse>>(){});

        // Is this sufficient?
        assertEquals(2, getResponse.size());
    }

    @Test
    void createAndUpdateWorkItem() throws Exception {
        WorkItemResponse createResponse = sendCreateRequest();

        var invalidUpdateReq = new UpdateStatusRequest("DONE");
        mvc.perform(
                patch("/api/work-items/" + createResponse.id() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdateReq))
            )
            .andExpect(status().is5xxServerError());
            
        var validUpdateReq = new UpdateStatusRequest("IN_PROGRESS");
        MvcResult updateResult = mvc.perform(
                patch("/api/work-items/" + createResponse.id() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateReq))
            )
            .andExpect(status().isOk())
            .andReturn();
        String updateResponseString = updateResult.getResponse().getContentAsString();
        WorkItemResponse updateResponse = objectMapper.readValue(updateResponseString, WorkItemResponse.class);

        assertEquals(createResponse.id(), updateResponse.id());
        assertEquals(createResponse.title(), updateResponse.title());
        assertEquals(createResponse.description(), updateResponse.description());
        assertEquals(createResponse.priority().toString(), updateResponse.priority());
        assertEquals(Status.IN_PROGRESS.toString(), updateResponse.status());
        assertNotEquals(createResponse.updatedAt(), updateResponse.updatedAt());
    }

    @Test
    void reqShouldReturnOK() throws Exception {
        mvc.perform(get("/api/work-items"))
                .andExpect(status().isOk());
    }
}
