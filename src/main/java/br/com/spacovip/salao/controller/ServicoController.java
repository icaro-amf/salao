package br.com.spacovip.salao.controller;

import br.com.spacovip.salao.dto.servico.ServicoRequestDTO;
import br.com.spacovip.salao.dto.servico.ServicoResponseDTO;
import br.com.spacovip.salao.service.ServicoService;
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
@RequestMapping("/spacovip/servicos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Serviços", description = "Endpoints para gerenciamento de serviços do salão")
public class ServicoController {

    private final ServicoService service;

    @PostMapping
    @Operation(summary = "Cadastrar novo serviço", description = "Cria um novo serviço no sistema. O nome deve ser único. Status inicial será INATIVO até que um profissional ativo seja vinculado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ServicoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (validação)"),
            @ApiResponse(responseCode = "409", description = "Nome de serviço já cadastrado"),
            @ApiResponse(responseCode = "415", description = "Content-Type não suportado")
    })
    public ResponseEntity<ServicoResponseDTO> cadastrar(
            @Parameter(description = "Dados do serviço a ser cadastrado", required = true)
            @Valid @RequestBody ServicoRequestDTO request) {
        ServicoResponseDTO response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar serviços com paginação", description = "Retorna uma página de serviços ordenados por nome e ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de serviços retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<ServicoResponseDTO>> listar(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)")
            @PageableDefault(size = 10, sort = {"nome", "id"}) Pageable paginacao) {
        Page<ServicoResponseDTO> response = service.buscarTodosServicos(paginacao);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por ID", description = "Retorna os detalhes de um serviço específico, incluindo profissionais ativos vinculados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço encontrado",
                    content = @Content(schema = @Schema(implementation = ServicoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado"),
            @ApiResponse(responseCode = "400", description = "UUID malformado")
    })
    public ResponseEntity<ServicoResponseDTO> buscarPorId(
            @Parameter(description = "ID do serviço (UUID)", required = true, example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable @NotNull UUID id) {
        ServicoResponseDTO response = service.buscarPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar serviço", description = "Atualiza os dados de um serviço existente. O nome não pode conflitar com outro serviço.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ServicoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou UUID malformado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado"),
            @ApiResponse(responseCode = "409", description = "Nome de serviço já em uso")
    })
    public ResponseEntity<ServicoResponseDTO> atualizar(
            @Parameter(description = "ID do serviço a atualizar", required = true, example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable @NotNull UUID id,
            @Parameter(description = "Novos dados do serviço", required = true)
            @Valid @RequestBody ServicoRequestDTO request) {
        ServicoResponseDTO response = service.atualizar(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir serviço", description = "Remove permanentemente o serviço do banco de dados. Vínculos com profissionais são removidos automaticamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado"),
            @ApiResponse(responseCode = "400", description = "UUID malformado")
    })
    public ResponseEntity<Void> remover(
            @Parameter(description = "ID do serviço a excluir", required = true, example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable @NotNull UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}