package br.com.spacovip.salao.domain.cliente;

import br.com.spacovip.salao.enums.Sexo;
import br.com.spacovip.salao.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/*
Cria tabela e entity para clientes do salao
*/

@Table(name = "Clientes")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String nome;
    private String email;

    @Column(length = 11)
    private String telefone;
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @PrePersist
    public void prePersistir() {
        this.dataCadastro = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}
