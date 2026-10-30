package br.com.pedroneves.controle_gastos.controller;

import br.com.pedroneves.controle_gastos.config.SecurityConfiguration;
import br.com.pedroneves.controle_gastos.dto.ResumoTransacoesResponse;
import br.com.pedroneves.controle_gastos.service.TransacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
@Import(SecurityConfiguration.class)
@org.springframework.test.context.TestPropertySource(properties = {
        "app.security.username=test-user",
        "app.security.password=test-password"
})
class TransacaoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransacaoService transacaoService;

    @Test
    void exigeAutenticacaoParaAcessarApi() throws Exception {
        mockMvc.perform(get("/transacoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void retornaResumoParaUsuarioAutenticado() throws Exception {
        when(transacaoService.resumo(2026, 10))
                .thenReturn(new ResumoTransacoesResponse(
                        new BigDecimal("150.00"),
                        new BigDecimal("40.00"),
                        new BigDecimal("110.00")));

        mockMvc.perform(get("/transacoes/resumo")
                        .param("ano", "2026")
                        .param("mes", "10")
                        .with(httpBasic("test-user", "test-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receitas").value(150.0))
                .andExpect(jsonPath("$.despesas").value(40.0))
                .andExpect(jsonPath("$.saldo").value(110.0));

        verify(transacaoService).resumo(2026, 10);
    }

    @Test
    void devolveErroDeValidacaoEmPortugues() throws Exception {
        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("test-user", "test-password"))
                        .contentType("application/json")
                        .content("""
                                {
                                  "descricao": "",
                                  "valor": -10,
                                  "data": "2026-10-29",
                                  "tipo": "DESPESA",
                                  "categoriaId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());
    }
}
