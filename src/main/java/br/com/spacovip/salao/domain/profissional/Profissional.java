package br.com.spacovip.salao.domain.profissional;

import br.com.spacovip.salao.domain.servico.Servico;
import br.com.spacovip.salao.dto.profissional.ProfissionalRequestDTO;
import br.com.spacovip.salao.enums.Sexo;
import br.com.spacovip.salao.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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
    private String email;
    private String telefone;
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

    @ManyToMany(mappedBy = "profissionais")
    private List<Servico> servicos = new ArrayList<>();

    public Profissional(ProfissionalRequestDTO profissional) {
        this.nome = profissional.nome();
        this.descricao = profissional.descricao();
        this.email = profissional.email();
        this.telefone = profissional.telefone();
        this.dataNascimento = profissional.dataNascimento();
    }
}
