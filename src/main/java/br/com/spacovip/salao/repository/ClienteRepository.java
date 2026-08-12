package br.com.spacovip.salao.repository;

import br.com.spacovip.salao.domain.cliente.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
}
