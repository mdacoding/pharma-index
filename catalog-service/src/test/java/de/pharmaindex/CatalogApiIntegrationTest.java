package de.pharmaindex;

import de.pharmaindex.pzn.PznChecksum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogApiIntegrationTest {

    private static final String API_KEY = "demo-partner-key";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsWriteWithoutApiKey() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicCatalogSearchNeedsNoApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("q", "Ibuprofen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThan(0)));
    }

    @Test
    void publicLandingIsOpen() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void searchesSeededCatalog() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("q", "Ibuprofen").header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThan(0)))
                .andExpect(jsonPath("$.content[*].activeIngredient", hasItem("Ibuprofen")));
    }

    @Test
    void loadsProductByValidPzn() throws Exception {
        String pzn = PznChecksum.withCheckDigit("9900001");
        mockMvc.perform(get("/api/v1/products/" + pzn).header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aspirin 500 mg Tabletten"));
    }

    @Test
    void matchesTypoAgainstParacetamol() throws Exception {
        mockMvc.perform(post("/api/v1/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"Paracetmol HEXAL"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches[0].name").value(org.hamcrest.Matchers.containsString("Paracetamol")))
                .andExpect(jsonPath("$.matches[0].explanations").isArray())
                .andExpect(jsonPath("$.candidatePoolSize").value(greaterThan(0)));
    }

    @Test
    void exposesAtcGroupAndRevisions() throws Exception {
        String pzn = PznChecksum.withCheckDigit("9900001");
        mockMvc.perform(get("/api/v1/products/" + pzn).header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atcGroup").value(org.hamcrest.Matchers.containsString("Nervensystem")));
        mockMvc.perform(get("/api/v1/products/" + pzn + "/revisions").header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].changeType").value("CREATED"));
    }

    @Test
    void dashboardShowsCatalogKpis() throws Exception {
        mockMvc.perform(get("/api/v1/ops/dashboard").header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCount").value(greaterThan(10)))
                .andExpect(jsonPath("$.matchingIndexSize").value(greaterThan(0)))
                .andExpect(jsonPath("$.openFindings").value(greaterThan(0)));
    }

    @Test
    void matchingIndexAppearsInHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.matchingIndex.status").value("UP"));
    }

    @Test
    void reportsQualityFindings() throws Exception {
        mockMvc.perform(get("/api/v1/qa/findings").header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", hasItem("INVALID_PZN")));
    }

    @Test
    void importsPartnerCsv() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "partner-import.csv",
                "text/csv",
                """
                pzn;name;manufacturer;active_ingredient;atc_code;strength;form;package_size;pharmacy_price;prescription_required
                %s;Ibuprofen AL 400 mg Filmtabletten;ALIUD PHARMA;Ibuprofen;M01AE01;400 mg;TABLETTE;20 Stück;4.95;false
                """.formatted(PznChecksum.withCheckDigit("9900015")).getBytes()
        );
        mockMvc.perform(multipart("/api/v1/b2b/imports").file(file).header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordsOk").value(1));
        mockMvc.perform(multipart("/api/v1/b2b/imports").file(file).header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordsOk").value(1))
                .andExpect(jsonPath("$.recordsError").value(0));
    }
}
