package br.com.spacovip.salao.repository;

import br.com.spacovip.salao.domain.cliente.Cliente;
import br.com.spacovip.salao.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);

    @Query("SELECT c FROM Cliente c WHERE c.status = :statusAtivo " +
           "AND (c.ultimoAgendamento < :cutoff OR (c.ultimoAgendamento IS NULL AND c.dataCadastro < :cutoff))")
    List<Cliente> findClientesParaInativacao(@Param("statusAtivo") Status statusAtivo, @Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT c FROM Cliente c WHERE c.status = :statusInativo AND c.dataAtualizacao < :cutoff")
    List<Cliente> findClientesInativosParaExclusao(@Param("statusInativo") Status statusInativo, @Param("cutoff") LocalDateTime cutoff);
}
