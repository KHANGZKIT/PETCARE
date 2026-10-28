package com.petcare.controller;

import com.petcare.dto.request.PetRequest;
import com.petcare.dto.response.PetResponse;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.exception.GlobalExceptionHandler;
import com.petcare.service.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    @Mock
    private PetService petService;

    @InjectMocks
    private PetController petController;

    private MockMvc mockMvc;
    private UsernamePasswordAuthenticationToken customerAuth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(petController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserEntity customer = new UserEntity("Khách Hàng", "customer@petcare.com", "0900000001", "hash", UserRole.CUSTOMER);
        customerAuth = new UsernamePasswordAuthenticationToken(
                customer,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    @Test
    void getMyPets_success_returns200AndList() throws Exception {
        List<PetResponse> pets = List.of(
                new PetResponse(1L, "Milu", "DOG"),
                new PetResponse(2L, "Miu", "CAT")
        );
        when(petService.getMyPets(eq("customer@petcare.com"))).thenReturn(pets);

        mockMvc.perform(get("/api/v1/pets").principal(customerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Milu"))
                .andExpect(jsonPath("$[0].species").value("DOG"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Miu"))
                .andExpect(jsonPath("$[1].species").value("CAT"));
    }

    @Test
    void getMyPets_noPets_returns200AndEmptyList() throws Exception {
        when(petService.getMyPets(eq("customer@petcare.com"))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/pets").principal(customerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMyPets_staffRole_returns403Forbidden() throws Exception {
        when(petService.getMyPets(any()))
                .thenThrow(new AccessDeniedException("Chỉ khách hàng mới có thể thao tác với thú cưng"));

        mockMvc.perform(get("/api/v1/pets").principal(customerAuth))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Bạn không có quyền thực hiện thao tác này"));
    }

    @Test
    void addPet_validRequest_returns201Created() throws Exception {
        PetResponse created = new PetResponse(1L, "Milu", "DOG");
        when(petService.addPet(any(PetRequest.class), eq("customer@petcare.com"))).thenReturn(created);

        String json = """
                {
                    "name": "Milu",
                    "species": "DOG"
                }
                """;

        mockMvc.perform(post("/api/v1/pets")
                        .principal(customerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Milu"))
                .andExpect(jsonPath("$.species").value("DOG"));
    }

    @Test
    void addPet_blankName_returns400BadRequest() throws Exception {
        String json = """
                {
                    "name": "   ",
                    "species": "DOG"
                }
                """;

        mockMvc.perform(post("/api/v1/pets")
                        .principal(customerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void addPet_missingSpecies_returns400BadRequest() throws Exception {
        String json = """
                {
                    "name": "Milu"
                }
                """;

        mockMvc.perform(post("/api/v1/pets")
                        .principal(customerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.species").exists());
    }

    @Test
    void addPet_staffRole_returns403Forbidden() throws Exception {
        when(petService.addPet(any(PetRequest.class), any()))
                .thenThrow(new AccessDeniedException("Chỉ khách hàng mới có thể thêm thú cưng"));

        String json = """
                {
                    "name": "Milu",
                    "species": "DOG"
                }
                """;

        mockMvc.perform(post("/api/v1/pets")
                        .principal(customerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
