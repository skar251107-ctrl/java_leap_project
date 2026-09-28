package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.entity.AdminUser;
import com.example.demo.entity.BloodGroup;
import com.example.demo.entity.Donor;
import com.example.demo.repository.AdminUserRepository;
import com.example.demo.repository.BloodGroupRepository;
import com.example.demo.repository.DonationRecordRepository;
import com.example.demo.repository.DonorRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BloodLinkIntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired private BloodGroupRepository bloodGroupRepository;
    @Autowired private DonorRepository donorRepository;
    @Autowired private DonationRecordRepository donationRepository;
    @Autowired private AdminUserRepository adminUserRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void bloodGroupsSupportCrudStatsAndStructuredMissingIds() throws Exception {
        mockMvc.perform(get("/api/blood-groups")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(8));
        BloodGroup group = bloodGroupRepository.findByBloodGroup("O-").orElseThrow();
        mockMvc.perform(get("/api/blood-groups/{id}", group.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bloodGroup").value("O-"));
        mockMvc.perform(put("/api/blood-groups/{id}", group.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"bloodGroup\":\"O-\",\"description\":\"Updated\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Updated"));
        mockMvc.perform(delete("/api/blood-groups/{id}", group.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Blood group ID " + group.getId() + " deleted successfully."));
        mockMvc.perform(get("/api/blood-groups/{id}", group.getId()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("BLOOD_GROUP_NOT_FOUND"));
        mockMvc.perform(post("/api/blood-groups").contentType(MediaType.APPLICATION_JSON)
                .content("{\"bloodGroup\":\"O-\",\"description\":\"Restored\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.bloodGroup").value("O-"));
        mockMvc.perform(get("/api/blood-groups/count")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bloodGroup").exists()).andExpect(jsonPath("$[0].count").value(0));
    }

    @Test
    void donorCrudSearchAndForeignKeyConflictWorkWithoutPublicPhoneExposure() throws Exception {
        mockMvc.perform(post("/api/donors").contentType(MediaType.APPLICATION_JSON)
                .content(donorJson("Ravi", "9876543210", "O+", "Chennai", null)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.available").value(true));
        Donor donor = donorRepository.findByPhone("9876543210").orElseThrow();
        mockMvc.perform(get("/api/donors")).andExpect(status().isOk()).andExpect(jsonPath("$[0].phone").value("9876543210"));
        mockMvc.perform(get("/api/donors/{id}", donor.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.bloodGroup").value("O+"));
        mockMvc.perform(get("/api/donors/search").param("bloodGroup", "O+").param("city", "chen"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Ravi"))
                .andExpect(jsonPath("$[0].phone").doesNotExist());
        mockMvc.perform(put("/api/donors/{id}", donor.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(donorJson("Ravi Kumar", "9876543210", "O+", "Chennai", null)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Ravi Kumar"));
        BloodGroup group = donor.getBloodGroup();
        mockMvc.perform(delete("/api/blood-groups/{id}", group.getId())).andExpect(status().isConflict());
        mockMvc.perform(delete("/api/donors/{id}", donor.getId())).andExpect(status().isOk());
        mockMvc.perform(get("/api/donors/{id}", donor.getId())).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("DONOR_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Donor ID " + donor.getId() + " does not exist."));
    }

    @Test
    void donationCrudEnforcesNinetyDayRuleAndUpdatesDonorAvailability() throws Exception {
        createDonor("Ava", "9123456780", "A+", LocalDate.now().minusDays(89));
        Donor coolingDonor = donorRepository.findByPhone("9123456780").orElseThrow();
        mockMvc.perform(post("/api/donations").contentType(MediaType.APPLICATION_JSON)
                .content(donationJson(coolingDonor.getId(), LocalDate.now())))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("COOLDOWN_ERROR"));

        createDonor("Mina", "9123456781", "A+", LocalDate.now().minusDays(90));
        Donor eligibleDonor = donorRepository.findByPhone("9123456781").orElseThrow();
        String response = mockMvc.perform(post("/api/donations").contentType(MediaType.APPLICATION_JSON)
                .content(donationJson(eligibleDonor.getId(), LocalDate.now())))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.donorName").value("Mina"))
                .andReturn().getResponse().getContentAsString();
        long donationId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/donations")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/donations/{id}", donationId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.donorId").value(eligibleDonor.getId()));
        mockMvc.perform(get("/api/donors/{id}/donations", eligibleDonor.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/donors/{id}", eligibleDonor.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mockMvc.perform(put("/api/donations/{id}", donationId).contentType(MediaType.APPLICATION_JSON)
                .content(donationJson(eligibleDonor.getId(), LocalDate.now().minusDays(1))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.donationDate").value(LocalDate.now().minusDays(1).toString()));
        mockMvc.perform(delete("/api/donations/{id}", donationId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/donations/{id}", donationId)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("DONATION_NOT_FOUND"));
        assertThat(donationRepository.existsById(donationId)).isFalse();
    }

    @Test
    void adminUsersSupportCrudWithBcryptAndNoHashInResponses() throws Exception {
        mockMvc.perform(post("/api/admin-users").contentType(MediaType.APPLICATION_JSON)
                .content(adminJson("operator", "operator@example.test", "long-test-password", "ADMIN")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        AdminUser user = adminUserRepository.findByUsernameIgnoreCase("operator").orElseThrow();
        assertThat(passwordEncoder.matches("long-test-password", user.getPasswordHash())).isTrue();
        mockMvc.perform(get("/api/admin-users")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
        mockMvc.perform(get("/api/admin-users/{id}", user.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("operator"));
        mockMvc.perform(put("/api/admin-users/{id}", user.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"operator2\",\"email\":\"operator2@example.test\",\"role\":\"STAFF\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("STAFF"));
        mockMvc.perform(delete("/api/admin-users/{id}", user.getId())).andExpect(status().isOk());
        mockMvc.perform(get("/api/admin-users/{id}", user.getId())).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ADMIN_USER_NOT_FOUND"));
    }

    @Test
    void validationAndSwaggerResponsesAreUseful() throws Exception {
        mockMvc.perform(post("/api/donors").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \",\"phone\":\"bad\",\"bloodGroup\":\"O+\",\"city\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").exists()).andExpect(jsonPath("$.fieldErrors.phone").exists());
        mockMvc.perform(post("/api/admin-users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"operator\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.password").exists());
        mockMvc.perform(get("/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/donations']").exists())
                .andExpect(jsonPath("$.paths['/api/blood-groups/count']").exists())
                .andExpect(jsonPath("$.paths['/api/donors'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/donations'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/blood-groups'].post.responses['409']").exists());
    }

    @Test
    void duplicateBloodGroupsDonorsAndAdminUsersReturnConflict() throws Exception {
        mockMvc.perform(post("/api/blood-groups").contentType(MediaType.APPLICATION_JSON)
                .content("{\"bloodGroup\":\"O+\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("DUPLICATE_RECORD"));
        createDonor("Lia", "9000000000", "B+", null);
        mockMvc.perform(post("/api/donors").contentType(MediaType.APPLICATION_JSON)
                .content(donorJson("Lia two", "9000000000", "B+", "Chennai", null)))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/admin-users").contentType(MediaType.APPLICATION_JSON)
                .content(adminJson("duplicate", "duplicate@example.test", "long-test-password", "ADMIN")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/admin-users").contentType(MediaType.APPLICATION_JSON)
                .content(adminJson("duplicate", "other@example.test", "long-test-password", "ADMIN")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("DUPLICATE_RECORD"));
    }

    private void createDonor(String name, String phone, String bloodGroup, LocalDate lastDonationDate) throws Exception {
        mockMvc.perform(post("/api/donors").contentType(MediaType.APPLICATION_JSON)
                .content(donorJson(name, phone, bloodGroup, "Chennai", lastDonationDate)))
                .andExpect(status().isCreated());
    }

    private String donorJson(String name, String phone, String bloodGroup, String city, LocalDate lastDonationDate) {
        return "{\"name\":\"%s\",\"phone\":\"%s\",\"bloodGroup\":\"%s\",\"city\":\"%s\",\"lastDonationDate\":%s}"
                .formatted(name, phone, bloodGroup, city,
                        lastDonationDate == null ? "null" : "\"" + lastDonationDate + "\"");
    }

    private String donationJson(Long donorId, LocalDate date) {
        return "{\"donorId\":%d,\"donationDate\":\"%s\",\"hospitalName\":\"City Hospital\"}"
                .formatted(donorId, date);
    }

    private String adminJson(String username, String email, String password, String role) {
        return "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}"
                .formatted(username, email, password, role);
    }
}
