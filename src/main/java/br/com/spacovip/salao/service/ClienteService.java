package br.com.spacovip.salao.service;

import br.com.spacovip.salao.domain.cliente.Cliente;
import br.com.spacovip.salao.dto.cliente.ClienteRequestDTO;
import br.com.spacovip.salao.dto.cliente.ClienteResponseDTO;
import br.com.spacovip.salao.mapper.ClienteMapper;
import br.com.spacovip.salao.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository repository;
    private final ClienteMapper mapper;

    public ClienteResponseDTO cadastrar(ClienteRequestDTO request){
        log.info("Iniciando cadastro de cliente: {}", request.nome());

        Cliente cliente = mapper.toEntity(request);
        Cliente clienteSalvo = repository.save(cliente);

        log.info("Cliente cadastrado com sucesso. ID: {}", clienteSalvo.getId());
        return mapper.toResponse(clienteSalvo);
    }
}
