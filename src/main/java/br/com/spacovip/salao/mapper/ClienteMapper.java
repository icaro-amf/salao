package br.com.spacovip.salao.mapper;

import br.com.spacovip.salao.domain.cliente.Cliente;
import br.com.spacovip.salao.dto.cliente.ClienteRequestDTO;
import br.com.spacovip.salao.dto.cliente.ClienteResponseDTO;
import br.com.spacovip.salao.enums.Status;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequestDTO request) {
        if (request == null) return null;

        Cliente cliente = new Cliente();
        cliente.setNome(request.nome());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.telefone());
        cliente.setDataNascimento(request.dataNascimento());
        cliente.setSexo(request.sexo());
        cliente.setStatus(Status.ATIVO);

        return cliente;
    }

    public ClienteResponseDTO toResponse(Cliente entity) {
        if (entity == null) return null;

        return new ClienteResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getTelefone(),
                entity.getStatus(),
                entity.getSexo()
        );
    }
}
