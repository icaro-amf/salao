package br.com.spacovip.salao.controller;

import br.com.spacovip.salao.dto.profissional.ProfissionalRequestDTO;
import br.com.spacovip.salao.dto.profissional.ProfissionalResponseDTO;
import br.com.spacovip.salao.service.ProfissionalService;
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
@RequestMapping("/spacovip/profissionais")
@RequiredArgsConstructor
@Validated
@Tag(name = "Profissionais", description = "Endpoints para gerenciamento de profissionais do salão e seus serviços")
public class ProfissionalController {

    private final ProfissionalService service;

    @PostMapping
    @Operation(summary = "Cadastrar novo profissional", description = "Cria um novo profissional no sistema. Email e telefone devem ser únicos. Pode vincular serviços opcionalmente via lista de IDs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Profissional criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProfissionalResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (validação)"),
            @ApiResponse(responseCode = "409", description = "Email ou telefone já cadastrado"),
            @ApiResponse(responseCode = "415", description = "Content-Type não suportado")
    })
    public ResponseEntity<ProfissionalResponseDTO> cadastrar(
            @Parameter(description = "Dados do profissional a ser cadastrado", required = true)
            @Valid @RequestBody ProfissionalRequestDTO request) {
        ProfissionalResponseDTO response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar profissionais com paginação", description = "Retorna uma página de profissionais ordenados por nome e ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de profissionais retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    })
    public ResponseEntity<Page<ProfissionalResponseDTO>> listar(
            @Parameter(description = "Parâmetros de paginação (page, size, sort)")
            @PageableDefault(size = 10, sort = {"nome", "id"}) Pageable paginacao) {
        Page<ProfissionalResponseDTO> response = service.buscarTodosProfissionais(paginacao);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar profissional por ID", description = "Retorna os detalhes de um profissional específico, incluindo serviços vinculados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profissional encontrado",
                    content = @Content(schema = @Schema(implementation = ProfissionalResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado"),
            @ApiResponse(responseCode = "400", description = "UUID malformado")
    })
    public ResponseEntity<ProfissionalResponseDTO> buscarPorId(
            @Parameter(description = "ID do profissional (UUID)", required = true, example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable @NotNull UUID id) {
        ProfissionalResponseDTO response = service.buscarPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar profissional", description = "Atualiza os dados de um profissional existente. A lista de servicosIds substitui completamente os vínculos anteriores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profissional atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProfissionalResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou UUID malformado"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado"),
            @ApiResponse(responseCode = "409", description = "Email ou telefone já em uso por outro profissional")
    })
    public ResponseEntity<ProfissionalResponseDTO> atualizar(
            @Parameter(description = "ID do profissional a atualizar", required = true, example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable @NotNull UUID id,
            @Parameter(description = "Novos dados do profissional", required = true)
            @Valid @RequestBody ProfissionalRequestDTO request) {
        ProfissionalResponseDTO response = service.atualizar(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}/desativar")
    @Operation(summary = "Desativar profissional", description = "Altera o status do profissional para INATIVO. O profissional permanece no banco mas não aparece em listagens ativas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Profissional desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado"),
            @ApiResponse(responseCode = "400", description = "UUID malformado")
    })
    public ResponseEntity<Void> desativar(
            @Parameter(description = "ID do profissional a desativar", required = true, example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable @NotNull UUID id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir profissional", description = "Remove permanentemente o profissional do banco de dados. Vínculos com serviços são removidos automaticamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Profissional excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Profissional não encontrado"),
            @ApiResponse(responseCode = "400", description = "UUID malformado")
    })
    public ResponseEntity<Void> remover(
            @Parameter(description = "ID do profissional a excluir", required = true, example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable @NotNull UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}