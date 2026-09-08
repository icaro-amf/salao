package br.com.spacovip.salao.repository;

import br.com.spacovip.salao.domain.servico.Servico;
import br.com.spacovip.salao.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ServicoRepository extends JpaRepository<Servico, UUID> {

    boolean existsByNome(String nome);

    @Query("SELECT DISTINCT s.id FROM Servico s " +
           "JOIN s.profissionais p " +
           "WHERE s.id IN :servicoIds AND p.status = :status")
    List<UUID> findServicoIdsWithActiveProfissional(
            @Param("servicoIds") List<UUID> servicoIds,
            @Param("status") Status status
    );
}
