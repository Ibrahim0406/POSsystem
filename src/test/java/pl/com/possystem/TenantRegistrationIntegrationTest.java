package pl.com.possystem;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.com.possystem.config.JWTProvider;
import pl.com.possystem.domain.AccountStatus;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.User;
import pl.com.possystem.repository.StoreRepository;
import pl.com.possystem.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TenantRegistrationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JWTProvider jwtProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private StoreRepository storeRepository;

    @Test
    void signupCreatesPendingTenantAndStoreAdminUntilPlatformApproval() throws Exception {
        String response = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerFullName":"Tenant Owner",
                                  "ownerEmail":"owner@example.test",
                                  "ownerPassword":"a-secure-test-password",
                                  "ownerPhone":"0610000000",
                                  "storeBrand":"New Test Store",
                                  "storeDescription":"Test tenant registration",
                                  "storeType":"Retail"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storeStatus").value("PENDING"))
                .andExpect(jsonPath("$.user.role").value("ROLE_STORE_ADMIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String jwt = JsonPath.read(response, "$.jwt");
        User owner = userRepository.findByEmail("owner@example.test");
        assertEquals(UserRole.ROLE_STORE_ADMIN, owner.getRole());
        assertEquals("New Test Store", owner.getStore().getBrand());
        assertEquals(owner.getId(), owner.getStore().getStoreAdmin().getId());
        assertEquals(1, storeRepository.count());

        mockMvc.perform(get("/api/products/{storeId}", owner.getStore().getId())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/store/admin")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        User platformAdmin = new User();
        platformAdmin.setFullName("Platform Administrator");
        platformAdmin.setEmail("platform-admin@example.test");
        platformAdmin.setPassword("not-used");
        platformAdmin.setRole(UserRole.ROLE_ADMIN);
        platformAdmin.setStatus(AccountStatus.ACTIVE);
        platformAdmin = userRepository.saveAndFlush(platformAdmin);
        String adminJwt = jwtProvider.generateToken(new UsernamePasswordAuthenticationToken(
                platformAdmin.getEmail(), null,
                List.of(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN.name()))));

        mockMvc.perform(put("/api/store/moderate/{id}", owner.getStore().getId())
                        .param("status", "ACTIVE")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/products/{storeId}", owner.getStore().getId())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/store/moderate/{id}", owner.getStore().getId())
                        .param("status", "BLOCKED")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/products/{storeId}", owner.getStore().getId())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void signupRejectsDuplicateOwnerEmailWithoutCreatingAnotherTenant() throws Exception {
        String body = """
                {
                  "ownerFullName":"Unique Owner",
                  "ownerEmail":"unique-owner@example.test",
                  "ownerPassword":"a-secure-test-password",
                  "storeBrand":"Unique Store"
                }
                """;
        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());

        assertEquals(1, userRepository.count());
        assertEquals(1, storeRepository.count());
    }
}
