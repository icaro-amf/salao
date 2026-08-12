package br.com.spacovip.salao.repository;

import br.com.spacovip.salao.domain.profissional.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfissionalRepository extends JpaRepository<Profissional, UUID> {
}
