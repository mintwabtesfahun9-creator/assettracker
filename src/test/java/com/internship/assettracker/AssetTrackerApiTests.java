package com.internship.assettracker;

import com.i.a.entity.Role;
import com.i.a.entity.User;
import com.i.a.repository.AssetRepository;
import com.i.a.repository.CategoryRepository;
import com.i.a.repository.UserRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssetTrackerApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        assetRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registrationCreatesStaffAccountWithEncodedPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"new-staff","password":"secure-pass-123"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.role").value("ROLE_STAFF"));

        User registered = userRepository.findByUsername("new-staff").orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotEquals(
            "secure-pass-123", registered.getPassword());
        org.junit.jupiter.api.Assertions.assertTrue(
            passwordEncoder.matches("secure-pass-123", registered.getPassword()));
    }

    @Test
    void swaggerDocumentationIsPublicWhileApiEndpointsRemainProtected() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.info.title").value("IT Asset Tracker API"));

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanAssignOnlyAvailableAssetsAndStaffCannotCreateInventory() throws Exception {
        User admin = saveUser("admin", Role.ROLE_ADMIN);
        User staff = saveUser("staff", Role.ROLE_STAFF);

        mockMvc.perform(post("/api/categories")
                .with(httpBasic("staff", "staff-password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Laptop\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.path").value("/api/categories"));

        String categoryJson = mockMvc.perform(post("/api/categories")
                .with(httpBasic("admin", "admin-password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Laptop\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode category = objectMapper.readTree(categoryJson);

        String assetJson = mockMvc.perform(post("/api/assets")
                .with(httpBasic("admin", "admin-password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Workstation","serialNumber":"SN-001","categoryId":%d}
                    """.formatted(category.get("id").asLong())))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode asset = objectMapper.readTree(assetJson);
        long assetId = asset.get("id").asLong();

        mockMvc.perform(patch("/api/assets/{id}/assign/{userId}", assetId, staff.getId())
                .with(httpBasic("admin", "admin-password")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ASSIGNED"))
            .andExpect(jsonPath("$.assignedToUsername").value("staff"));

        mockMvc.perform(patch("/api/assets/{id}/assign/{userId}", assetId, admin.getId())
                .with(httpBasic("admin", "admin-password")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value(
                "/api/assets/" + assetId + "/assign/" + admin.getId()));

        mockMvc.perform(get("/api/assets/my-assets")
                .with(httpBasic("staff", "staff-password")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(patch("/api/assets/{id}/return", assetId)
                .with(httpBasic("admin", "admin-password")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.assignedToId").value((Object) null));
    }

    private User saveUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(username + "-password"));
        user.setRole(role);
        return userRepository.save(user);
    }
}
