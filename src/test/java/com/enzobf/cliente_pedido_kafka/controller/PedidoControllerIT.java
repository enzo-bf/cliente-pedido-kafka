package com.enzobf.cliente_pedido_kafka.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PedidoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCriarPedidoAlterarStatusEConsultarHistorico() throws Exception {
        MvcResult clienteResult = mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Marina Lima",
                                  "cpf": "55566677788",
                                  "email": "marina@email.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        Integer clienteId = JsonPath.read(clienteResult.getResponse().getContentAsString(), "$.id");

        MvcResult pedidoResult = mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Monitor 27",
                                  "valor": 1500.00,
                                  "desconto": 10,
                                  "clienteId": %d
                                }
                                """.formatted(clienteId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.valorFinal").value(1350.00))
                .andReturn();

        Integer pedidoId = JsonPath.read(pedidoResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(patch("/pedidos/{id}/status", pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PROCESSANDO" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSANDO"));

        mockMvc.perform(get("/pedidos/{id}/historico", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statusNovo").value("CRIADO"))
                .andExpect(jsonPath("$[1].statusNovo").value("PROCESSANDO"));
    }
}
