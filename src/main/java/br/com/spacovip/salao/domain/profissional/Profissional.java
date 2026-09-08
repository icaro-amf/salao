package br.com.spacovip.salao.domain.profissional;

import br.com.spacovip.salao.domain.servico.Servico;
import br.com.spacovip.salao.enums.Sexo;
import br.com.spacovip.salao.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
Cria tabela e entity para os profissionais do salao
*/

@Table(name = "Profissionais")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profissional {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String nome;
    @Column(length = 100)
    private String descricao;

    @Column(unique = true)
    private String email;

    @Column(length = 11, unique = true)
    private String telefone;
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;

    @ManyToMany(mappedBy = "profissionais")
    private List<Servico> servicos = new ArrayList<>();

    @PrePersist
    public void prePersistir() {
        this.dataCadastro = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}
