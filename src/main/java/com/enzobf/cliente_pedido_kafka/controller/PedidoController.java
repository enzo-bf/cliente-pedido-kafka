package com.enzobf.cliente_pedido_kafka.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enzobf.cliente_pedido_kafka.dto.request.PedidoRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.ErroResponse;
import com.enzobf.cliente_pedido_kafka.dto.response.HistoricoPedidoResponse;
import com.enzobf.cliente_pedido_kafka.dto.response.PedidoResponse;
import com.enzobf.cliente_pedido_kafka.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de cadastro, consulta, atualização e exclusão de pedidos.
 * A criação e a atualização publicam eventos no Kafka; o desconto e o
 * histórico de processamento são gerados de forma assíncrona pelo consumidor.
 */
@Tag(name = "Pedidos", description = "Cadastro, consulta, atualização, exclusão, listagem paginada e histórico de pedidos")
@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @Operation(
            summary = "Criar pedido",
            description = "Cria um novo pedido para um cliente existente e publica o evento `PEDIDO_CRIADO` "
                    + "no tópico `pedidos.criados`. A resposta imediata traz `desconto` e `valorFinal` "
                    + "nulos: esses campos só são preenchidos após o processamento assíncrono do evento "
                    + "pelo consumidor Kafka, que também grava uma entrada no histórico do pedido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso (desconto ainda não calculado)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (campos obrigatórios ausentes ou valor <= 0)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente encontrado com o clienteId informado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping
    public ResponseEntity<PedidoResponse> cadastrar(
            @Valid @RequestBody PedidoRequest request
    ) {
        PedidoResponse response = pedidoService.cadastrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Buscar pedido por ID",
            description = "Retorna os dados de um pedido a partir do seu identificador, incluindo desconto "
                    + "e valor final já calculados, caso o processamento assíncrono já tenha ocorrido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum pedido encontrado com o ID informado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(
            @Parameter(description = "Identificador do pedido", required = true, example = "10")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @Operation(
            summary = "Listar pedidos",
            description = "Lista pedidos de forma paginada, com filtros opcionais e combináveis por cliente "
                    + "e/ou faixa de valor (`valorMin`/`valorMax`). Suporta os parâmetros padrão de "
                    + "paginação e ordenação do Spring Data (`page`, `size`, `sort`)."
    )
    @ApiResponse(responseCode = "200", description = "Página de pedidos retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PedidoResponse.class)))
    @GetMapping
    public ResponseEntity<Page<PedidoResponse>> listar(
            @Parameter(description = "Filtra pedidos de um cliente específico", example = "1")
            @RequestParam(required = false) Long clienteId,
            @Parameter(description = "Valor mínimo do pedido (inclusive)", example = "100.00")
            @RequestParam(required = false) BigDecimal valorMin,
            @Parameter(description = "Valor máximo do pedido (inclusive)", example = "1000.00")
            @RequestParam(required = false) BigDecimal valorMax,
            @Parameter(description = "Paginação e ordenação: `page` (padrão 0), `size` (padrão 10) "
                    + "e `sort` no formato `campo,asc|desc` (padrão `dataCriacao,desc`)")
            @PageableDefault(size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(pedidoService.listar(clienteId, valorMin, valorMax, pageable));
    }

    @Operation(
            summary = "Atualizar pedido",
            description = "Atualiza a descrição, o valor e/ou o cliente de um pedido existente. O desconto "
                    + "e o valor final são zerados e recalculados de forma assíncrona: a atualização publica "
                    + "o evento `PEDIDO_ATUALIZADO` no tópico `pedidos.atualizados`, e o consumidor Kafka "
                    + "recalcula o desconto e grava uma nova entrada no histórico do pedido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido atualizado com sucesso (desconto ainda não recalculado)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum pedido (ou cliente) encontrado com o ID informado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponse> atualizar(
            @Parameter(description = "Identificador do pedido", required = true, example = "10")
            @PathVariable Long id,
            @Valid @RequestBody PedidoRequest request
    ) {
        return ResponseEntity.ok(pedidoService.atualizar(id, request));
    }

    @Operation(
            summary = "Excluir pedido",
            description = "Remove um pedido pelo seu identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum pedido encontrado com o ID informado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador do pedido", required = true, example = "10")
            @PathVariable Long id
    ) {
        pedidoService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Histórico de um pedido",
            description = "Lista, em ordem cronológica, todas as entradas de histórico (`PEDIDO_PROCESSADO`, "
                    + "`PEDIDO_ATUALIZADO`) geradas para um pedido específico. Retornado como lista simples "
                    + "(não paginada), mantendo o contrato original deste endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico do pedido retornado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HistoricoPedidoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum pedido encontrado com o ID informado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoPedidoResponse>> listarHistorico(
            @Parameter(description = "Identificador do pedido", required = true, example = "10")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(pedidoService.listarHistorico(id));
    }
}
