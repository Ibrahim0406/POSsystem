package pl.com.possystem;

import org.junit.jupiter.api.BeforeEach;
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
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.*;
import pl.com.possystem.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TenantIsolationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JWTProvider jwtProvider;
    @Autowired private StoreRepository storeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BranchRepository branchRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private RefundRepository refundRepository;
    @Autowired private ShiftReportRepository shiftReportRepository;
    private Store storeA;
    private Store storeB;
    private Branch branchA;
    private Branch branchB;
    private Category categoryB;
    private Product productB;
    private Inventory inventoryB;
    private Customer customerA;
    private Customer customerB;
    private User cashierB;
    private Order orderB;
    private Refund refundB;
    private ShiftReport shiftReportB;
    private String tokenA;
    private String tokenB;
    private String tokenStoreManagerA;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();
        User adminA = saveUser("admin-a-" + suffix, UserRole.ROLE_STORE_ADMIN);
        User adminB = saveUser("admin-b-" + suffix, UserRole.ROLE_STORE_ADMIN);
        storeA = saveStore("tenant-a-" + suffix, adminA);
        storeB = saveStore("tenant-b-" + suffix, adminB);
        branchA = saveBranch("branch-a-" + suffix, storeA);
        branchB = saveBranch("branch-b-" + suffix, storeB);
        Category categoryA = categoryRepository.saveAndFlush(
                Category.builder().name("category-a-" + suffix).store(storeA).build());
        categoryB = categoryRepository.saveAndFlush(
                Category.builder().name("category-b-" + suffix).store(storeB).build());
        productRepository.saveAndFlush(product("sku-a-" + suffix, storeA, categoryA));
        productB = productRepository.saveAndFlush(product("sku-b-" + suffix, storeB, categoryB));
        inventoryB = inventoryRepository.saveAndFlush(
                Inventory.builder().branch(branchB).product(productB).quantity(10).build());
        cashierB = saveUser("cashier-b-" + suffix, UserRole.ROLE_BRANCH_CASHIER);
        cashierB.setStore(storeB);
        cashierB.setBranch(branchB);
        cashierB = userRepository.saveAndFlush(cashierB);
        customerB = customerRepository.saveAndFlush(Customer.builder()
                .fullName("Customer B " + suffix)
                .email("customer-b-" + suffix + "@example.test")
                .store(storeB)
                .build());
        customerA = customerRepository.saveAndFlush(Customer.builder()
                .fullName("Customer A " + suffix)
                .email("customer-a-" + suffix + "@example.test")
                .store(storeA)
                .build());
        orderB = orderRepository.saveAndFlush(Order.builder()
                .branch(branchB)
                .cashier(cashierB)
                .customer(customerB)
                .items(List.of())
                .build());
        refundB = refundRepository.saveAndFlush(Refund.builder()
                .branch(branchB)
                .cashier(cashierB)
                .order(orderB)
                .amount(new BigDecimal("1.00"))
                .reason("test")
                .build());
        shiftReportB = shiftReportRepository.saveAndFlush(ShiftReport.builder()
                .branch(branchB)
                .cashier(cashierB)
                .shiftStart(LocalDateTime.now())
                .build());
        tokenA = tokenFor(adminA);
        tokenB = tokenFor(adminB);
        User storeManagerA = saveUser("manager-a-" + suffix, UserRole.ROLE_STORE_MANAGER);
        storeManagerA.setStore(storeA);
        storeManagerA = userRepository.saveAndFlush(storeManagerA);
        tokenStoreManagerA = tokenFor(storeManagerA);
    }

    @Test
    void storeAdminsCanReadOwnTenantButCannotReadOtherTenant() throws Exception {
        mockMvc.perform(get("/api/store/{id}", storeA.getId()).header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/store/{id}", storeB.getId()).header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.trace").doesNotExist());
        mockMvc.perform(get("/api/store/{id}", storeA.getId()).header("Authorization", bearer(tokenB)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/products/{storeId}", storeA.getId()).header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/products/{storeId}", storeB.getId()).header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/branches/{id}", branchB.getId()).header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/categories/store/{storeId}", storeB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/inventory/branch/{branchId}", branchB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/customers/{id}", customerB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/orders/get/{id}", orderB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/orders/branch/{branchId}", branchB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/refunds/{id}", refundB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/refunds/branch/{branchId}", branchB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/shift-reports/{id}", shiftReportB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/shift-reports/branch/{branchId}", branchB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/employees/store/{storeId}", storeB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/employees/branch/{branchId}", branchB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/user/{id}", cashierB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerListsAndSearchStayInsideTheCurrentTenant() throws Exception {
        mockMvc.perform(get("/api/customers").header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.contains(customerA.getId().intValue())));
        mockMvc.perform(get("/api/customers/search")
                        .param("keyword", currentSuffix())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.contains(customerA.getId().intValue())));
        mockMvc.perform(get("/api/customers/{id}", customerB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/customers").header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.contains(customerB.getId().intValue())));
    }

    @Test
    void isolationWorksInBothDirectionsAndSystemAdminRemainsGlobal() throws Exception {
        mockMvc.perform(get("/api/store/{id}", storeB.getId()).header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/products/{storeId}", storeB.getId()).header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/store/{id}", storeA.getId()).header("Authorization", bearer(tokenB)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/products/{storeId}", storeA.getId()).header("Authorization", bearer(tokenB)))
                .andExpect(status().isForbidden());

        User systemAdmin = saveUser("platform-admin-" + UUID.randomUUID(), UserRole.ROLE_ADMIN);
        mockMvc.perform(get("/api/store/{id}", storeB.getId())
                        .header("Authorization", bearer(tokenFor(systemAdmin))))
                .andExpect(status().isOk());
    }

    @Test
    void storeManagerCanOperateAllBranchesAndCannotChangeStoreOwner() throws Exception {
        mockMvc.perform(post("/api/branches/create")
                        .header("Authorization", bearer(tokenStoreManagerA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"manager-created","address":"Address",
                                 "phoneNumber":"0610000000","email":"manager-created@example.test",
                                 "workingDays":["Monday"],"openTime":"09:00:00","closeTime":"17:00:00"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/branches/store/{storeId}", storeA.getId())
                        .header("Authorization", bearer(tokenStoreManagerA)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", bearer(tokenStoreManagerA)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/employees/store/{storeId}", storeA.getId())
                        .header("Authorization", bearer(tokenStoreManagerA)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/store/update/{id}", storeA.getId())
                        .header("Authorization", bearer(tokenStoreManagerA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"brand":"changed","description":"changed"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/store/delete/{id}", storeA.getId())
                        .header("Authorization", bearer(tokenStoreManagerA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/store/{id}", storeB.getId())
                        .header("Authorization", bearer(tokenStoreManagerA)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/employees/branch/{branchId}", branchA.getId())
                        .header("Authorization", bearer(tokenStoreManagerA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Manager Cashier","email":"manager-cashier@example.test",
                                 "password":"secure-password","role":"ROLE_BRANCH_CASHIER",
                                 "branchId":%d}
                                """.formatted(branchA.getId())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/employees/branch/{branchId}", branchB.getId())
                        .header("Authorization", bearer(tokenStoreManagerA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeAdminCannotModifyOrDeleteAnotherTenantsResources() throws Exception {
        mockMvc.perform(put("/api/store/update/{id}", storeB.getId())
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"brand":"changed","description":"changed"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/store/delete/{id}", storeB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/branches/update/{id}", branchB.getId())
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"changed","address":"changed","phoneNumber":"000",
                                 "email":"changed@example.com","workingDays":["Monday"],
                                 "openTime":"09:00:00","closeTime":"17:00:00"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/branches/delete/{id}", branchB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/products/update/{id}", productB.getId())
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"changed","sku":"%s","mrp":10,"sellingPrice":9}
                                """.formatted(productB.getSku())))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/products/delete/{id}", productB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/categories/update/{id}", categoryB.getId())
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"changed"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/categories/delete/{id}", categoryB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/inventory/update")
                        .param("id", inventoryB.getId().toString())
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":99}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/inventory/delete")
                        .param("id", inventoryB.getId().toString())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/customers/update/{id}", customerB.getId())
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"changed","email":"changed@example.test"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/customers/delete/{id}", customerB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/refunds/{id}", refundB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/employees/delete/{id}", cashierB.getId())
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isForbidden());

        org.junit.jupiter.api.Assertions.assertEquals("tenant-b-" + currentSuffix(), storeB.getBrand());
        org.junit.jupiter.api.Assertions.assertEquals("branch-b-" + currentSuffix(), branchB.getName());
        org.junit.jupiter.api.Assertions.assertEquals("sku-b-" + currentSuffix(), productB.getSku());
        org.junit.jupiter.api.Assertions.assertEquals("category-b-" + currentSuffix(), categoryB.getName());
        org.junit.jupiter.api.Assertions.assertEquals(10, inventoryB.getQuantity());
        org.junit.jupiter.api.Assertions.assertEquals("Customer B " + currentSuffix(), customerB.getFullName());
        org.junit.jupiter.api.Assertions.assertNotNull(orderRepository.findById(orderB.getId()).orElse(null));
        org.junit.jupiter.api.Assertions.assertNotNull(refundRepository.findById(refundB.getId()).orElse(null));
        org.junit.jupiter.api.Assertions.assertNotNull(
                shiftReportRepository.findById(shiftReportB.getId()).orElse(null));
        org.junit.jupiter.api.Assertions.assertNotNull(userRepository.findById(cashierB.getId()).orElse(null));
    }

    @Test
    void branchCashierCannotManageInventoryOrAccessAnotherBranch() throws Exception {
        User cashier = saveUser("cashier-" + UUID.randomUUID(), UserRole.ROLE_BRANCH_CASHIER);
        cashier.setStore(storeA);
        cashier.setBranch(branchA);
        userRepository.saveAndFlush(cashier);
        String cashierToken = tokenFor(cashier);

        Branch otherBranchInSameStore = saveBranch("branch-a2-" + UUID.randomUUID(), storeA);
        mockMvc.perform(get("/api/branches/{id}", branchA.getId())
                        .header("Authorization", bearer(cashierToken)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/branches/{id}", otherBranchInSameStore.getId())
                        .header("Authorization", bearer(cashierToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/branches/{id}", branchB.getId())
                        .header("Authorization", bearer(cashierToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/employees/branch/{branchId}", branchA.getId())
                        .header("Authorization", bearer(cashierToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/employees/update/{id}", cashier.getId())
                        .header("Authorization", bearer(cashierToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Escalated","email":"escalated@example.test",
                                 "role":"ROLE_BRANCH_MANAGER","branchId":%d}
                                """.formatted(branchA.getId())))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/inventory/update")
                        .param("id", inventoryB.getId().toString())
                        .header("Authorization", bearer(cashierToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":99}
                                """))
                .andExpect(status().isForbidden());
        org.junit.jupiter.api.Assertions.assertEquals(10, inventoryB.getQuantity());
    }

    private User saveUser(String email, UserRole role) {
        User user = new User();
        user.setFullName(email);
        user.setEmail(email + "@example.test");
        user.setPassword("not-used");
        user.setRole(role);
        user.setStatus(AccountStatus.ACTIVE);
        return userRepository.saveAndFlush(user);
    }

    private Store saveStore(String brand, User admin) {
        Store store = new Store();
        store.setBrand(brand);
        store.setStoreAdmin(admin);
        store = storeRepository.saveAndFlush(store);
        store.setStatus(StoreStatus.ACTIVE);
        store = storeRepository.saveAndFlush(store);
        admin.setStore(store);
        userRepository.saveAndFlush(admin);
        return store;
    }

    private Branch saveBranch(String name, Store store) {
        return branchRepository.saveAndFlush(Branch.builder()
                .name(name)
                .store(store)
                .workingDays(List.of("Monday"))
                .build());
    }

    private Product product(String sku, Store store, Category category) {
        return Product.builder()
                .sku(sku)
                .name(sku)
                .mrp(new BigDecimal("10.00"))
                .sellingPrice(new BigDecimal("9.00"))
                .store(store)
                .category(category)
                .build();
    }

    private String tokenFor(User user) {
        return jwtProvider.generateToken(new UsernamePasswordAuthenticationToken(
                user.getEmail(), null,
                List.of(new SimpleGrantedAuthority(user.getRole().name()))));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String currentSuffix() {
        return storeA.getBrand().substring("tenant-a-".length());
    }
}
