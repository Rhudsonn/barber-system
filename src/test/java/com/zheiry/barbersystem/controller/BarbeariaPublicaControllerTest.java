package com.zheiry.barbersystem.controller;

import com.zheiry.barbersystem.entities.BarbeariaEntity;
import com.zheiry.barbersystem.repositories.BarbeariaRepository;
import com.zheiry.barbersystem.services.BarbeariaService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BarbeariaPublicaControllerTest {

    @Test
    void respostaPublicaIncluiSomenteCamposNecessariosParaIdentificacaoEAReserva() throws Exception {
        MockMvc mockMvc = controllerComBarbeariaDeTeste();

        mockMvc.perform(get("/barbearias/principal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nomeFantasia").value("Zheiry"))
                .andExpect(jsonPath("$.logoUrl").value("https://cdn.example.test/logo.png"))
                .andExpect(jsonPath("$.descricao").value("Descrição pública"))
                .andExpect(jsonPath("$.endereco").value("Rua Central, 10"))
                .andExpect(jsonPath("$.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.whatsapp").value("11999999999"))
                .andExpect(jsonPath("$.razaoSocial").doesNotExist())
                .andExpect(jsonPath("$.cnpj").doesNotExist())
                .andExpect(jsonPath("$.cep").doesNotExist())
                .andExpect(jsonPath("$.telefone").doesNotExist())
                .andExpect(jsonPath("$.instagram").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.estado").doesNotExist())
                .andExpect(jsonPath("$.horarios").doesNotExist());
    }

    @Test
    void rotaAdministrativaAindaRetornaDadosCompletosEExigePerfilAdministrador() throws Exception {
        MockMvc mockMvc = controllerComBarbeariaDeTeste();

        mockMvc.perform(get("/barbearias/principal/administracao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Zheiry Serviços Ltda."))
                .andExpect(jsonPath("$.cnpj").value("12345678000199"))
                .andExpect(jsonPath("$.email").value("contato@example.test"))
                .andExpect(jsonPath("$.horarios").isArray());

        assertPreAuthorize("buscarPrincipalAdministrativa");
        assertPreAuthorize("buscarPorId", Long.class);
        assertPreAuthorize("listarTodas");
    }

    private MockMvc controllerComBarbeariaDeTeste() {
        BarbeariaEntity entidade = new BarbeariaEntity();
        entidade.setId(7L);
        entidade.setNomeFantasia("Zheiry");
        entidade.setRazaoSocial("Zheiry Serviços Ltda.");
        entidade.setCnpj("12345678000199");
        entidade.setTelefone("11999999999");
        // Formato legado armazenado antes da normalização com DDI.
        entidade.setWhatsapp("11999999999");
        entidade.setInstagram("https://instagram.example.test/zheiry");
        entidade.setEmail("contato@example.test");
        entidade.setEndereco("Rua Central, 10");
        entidade.setCidade("São Paulo");
        entidade.setEstado("SP");
        entidade.setCep("01001000");
        entidade.setDescricao("Descrição pública");
        entidade.setLogoUrl("https://cdn.example.test/logo.png");

        BarbeariaRepository repository = mock(BarbeariaRepository.class);
        when(repository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(entidade));
        return MockMvcBuilders.standaloneSetup(new BarbeariaController(new BarbeariaService(repository))).build();
    }

    private void assertPreAuthorize(String nomeMetodo, Class<?>... tiposParametros) throws Exception {
        PreAuthorize autorizacao = BarbeariaController.class.getMethod(nomeMetodo, tiposParametros)
                .getAnnotation(PreAuthorize.class);
        assertNotNull(autorizacao);
        assertEquals("hasRole('ADMINISTRADOR')", autorizacao.value());
    }
}
