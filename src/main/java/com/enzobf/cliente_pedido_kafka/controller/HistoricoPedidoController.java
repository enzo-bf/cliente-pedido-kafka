package com.enzobf.cliente_pedido_kafka.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enzobf.cliente_pedido_kafka.dto.response.HistoricoPedidoResponse;
import com.enzobf.cliente_pedido_kafka.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

/**
 * Consulta o histórico de processamento de pedidos de forma transversal
 * (não restrita a um único pedido), com filtro opcional por tipo de evento
 * e suporte a paginação/ordenação.
 */
@Tag(name = "Histórico", description = "Consulta transversal do histórico de processamento de pedidos, filtrável por tipo de evento")
@RestController
@RequestMapping("/historico-pedidos")
@RequiredArgsConstructor
public class HistoricoPedidoController {

    private final PedidoService pedidoService;

    @Operation(
            summary = "Listar histórico por tipo de evento",
            description = "Lista, de forma paginada, as entradas de histórico de todos os pedidos. "
                    + "O filtro `tipoEvento` é opcional: quando omitido, retorna o histórico completo; "
                    + "quando informado (por exemplo `PEDIDO_PROCESSADO` ou `PEDIDO_ATUALIZADO`), restringe "
                    + "o resultado às entradas daquele tipo. Suporta os parâmetros padrão de paginação e "
                    + "ordenação do Spring Data (`page`, `size`, `sort`)."
    )
    @ApiResponse(responseCode = "200", description = "Página de histórico retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HistoricoPedidoResponse.class)))
    @GetMapping
    public ResponseEntity<Page<HistoricoPedidoResponse>> listarPorTipoEvento(
            @Parameter(description = "Filtra pelo tipo de evento (ex.: PEDIDO_PROCESSADO, PEDIDO_ATUALIZADO)", example = "PEDIDO_PROCESSADO")
            @RequestParam(required = false) String tipoEvento,
            @Parameter(description = "Paginação e ordenação: `page` (padrão 0), `size` (padrão 10) "
                    + "e `sort` no formato `campo,asc|desc` (padrão `dataProcessamento,desc`)")
            @PageableDefault(size = 10, sort = "dataProcessamento", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(pedidoService.listarHistoricoPorTipoEvento(tipoEvento, pageable));
    }
}
