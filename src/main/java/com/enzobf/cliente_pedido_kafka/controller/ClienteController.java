package com.enzobf.cliente_pedido_kafka.controller;

import com.enzobf.cliente_pedido_kafka.dto.request.ClienteRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.ClienteResponse;
import com.enzobf.cliente_pedido_kafka.dto.response.ErroResponse;
import com.enzobf.cliente_pedido_kafka.service.ClienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

/**
 * Endpoints de cadastro, consulta, atualização e exclusão de clientes,
 * com listagem paginada e filtros combináveis por CPF e/ou nome.
 */
@Tag(name = "Clientes", description = "Cadastro, consulta, atualização, exclusão e listagem paginada de clientes")
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(
            summary = "Cadastrar cliente",
            description = "Cria um novo cliente. O CPF deve ser único: uma tentativa de cadastro com um "
                    + "CPF já existente retorna 409 Conflict."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (campos obrigatórios ausentes ou e-mail em formato inválido)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe um cliente cadastrado com esse CPF",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(
            @Valid @RequestBody ClienteRequest request
    ) {
        ClienteResponse response = clienteService.cadastrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Buscar cliente por ID",
            description = "Retorna os dados de um cliente a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente encontrado com o ID informado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(
            @Parameter(description = "Identificador do cliente", required = true, example = "1")
            @PathVariable Long id
    ) {
        ClienteResponse response = clienteService.buscarPorId(id);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Listar clientes",
            description = "Lista clientes de forma paginada, com filtros opcionais e combináveis por CPF "
                    + "(igualdade exata) e/ou nome (contém, sem diferenciar maiúsculas de minúsculas). "
                    + "Suporta os parâmetros padrão de paginação e ordenação do Spring Data (`page`, `size`, `sort`)."
    )
    @ApiResponse(responseCode = "200", description = "Página de clientes retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClienteResponse.class)))
    @GetMapping
    public ResponseEntity<Page<ClienteResponse>> listar(
            @Parameter(description = "Filtra por CPF exato", example = "12345678901")
            @RequestParam(required = false) String cpf,
            @Parameter(description = "Filtra por nome (contém, case-insensitive)", example = "Enzo")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Paginação e ordenação: `page` (padrão 0), `size` (padrão 10) "
                    + "e `sort` no formato `campo,asc|desc` (padrão `nome,asc`)")
            @PageableDefault(size = 10, sort = "nome") Pageable pageable
    ) {
        return ResponseEntity.ok(clienteService.listar(cpf, nome, pageable));
    }

    @Operation(
            summary = "Atualizar cliente",
            description = "Atualiza nome, CPF e e-mail de um cliente existente. Se o CPF for alterado para "
                    + "um valor já usado por outro cliente, a operação falha com 409 Conflict."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente encontrado com o ID informado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "O novo CPF já pertence a outro cliente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(
            @Parameter(description = "Identificador do cliente", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request
    ) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }

    @Operation(
            summary = "Excluir cliente",
            description = "Remove um cliente pelo seu identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente encontrado com o ID informado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador do cliente", required = true, example = "1")
            @PathVariable Long id
    ) {
        clienteService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
