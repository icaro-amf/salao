package br.com.spacovip.salao.repository;

import br.com.spacovip.salao.domain.servico.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServicoRepository extends JpaRepository<Servico, UUID> {
}
