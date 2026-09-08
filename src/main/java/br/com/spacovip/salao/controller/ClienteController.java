package br.com.spacovip.salao.controller;

import br.com.spacovip.salao.dto.cliente.ClienteRequestDTO;
import br.com.spacovip.salao.dto.cliente.ClienteResponseDTO;
import br.com.spacovip.salao.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/spacovip/clientes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes do salão")
public class ClienteController {

    private final ClienteService service;

    @PostMapping
    @Operation(summary = "Cadastrar novo cliente", description = "Cria um novo cliente no sistema. Email e telefone devem ser únicos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (validação)"),
            @ApiResponse(responseCode = "409", description = "Email ou telefone já cadastrado"),
            @ApiResponse(responseCode = "415", description = "Content-Type não suportado")
    })
    public ResponseEntity<ClienteResponseDTO> cadastrar(
            @Parameter(description = "Dados do cliente a ser cadastrado", required = true)
            @Valid @RequestBody ClienteRequestDTO request) {
        ClienteResponseDTO response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar clientes com paginação", description = "Retorna uma página de clientes ordenados por nome e ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<ClienteResponseDTO>> listar(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)")
            @PageableDefault(size = 10, sort = {"nome", "id"}) Pageable paginacao) {
        Page<ClienteResponseDTO> response = service.buscarTodosClientes(paginacao);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna os detalhes de um cliente específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "400", description = "UUID malformado")
    })
    public ResponseEntity<ClienteResponseDTO> buscarPorId(
            @Parameter(description = "ID do cliente (UUID)", required = true, example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable @NotNull UUID id) {
        ClienteResponseDTO response = service.buscarPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente. Email e telefone não podem conflitar com outros clientes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou UUID malformado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "Email ou telefone já em uso por outro cliente")
    })
    public ResponseEntity<ClienteResponseDTO> atualizar(
            @Parameter(description = "ID do cliente a atualizar", required = true, example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable @NotNull UUID id,
            @Parameter(description = "Novos dados do cliente", required = true)
            @Valid @RequestBody ClienteRequestDTO request) {
        ClienteResponseDTO response = service.atualizar(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}/desativar")
    @Operation(summary = "Desativar cliente", description = "Altera o status do cliente para INATIVO. O cliente permanece no banco mas não aparece em listagens ativas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "400", description = "UUID malformado")
    })
    public ResponseEntity<Void> desativar(
            @Parameter(description = "ID do cliente a desativar", required = true, example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable @NotNull UUID id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente", description = "Remove permanentemente o cliente do banco de dados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "400", description = "UUID malformado")
    })
    public ResponseEntity<Void> remover(
            @Parameter(description = "ID do cliente a excluir", required = true, example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable @NotNull UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
