package br.com.spacovip.salao.domain.servico;

import br.com.spacovip.salao.domain.profissional.Profissional;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
Cria tabela e entity para servicos prestados pelo salao
*/

@Table(name = "Servicos")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Servico {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;
    private String descricao;
    private Long duracaoMinutos;
    private BigDecimal preco;

    @ManyToMany
    @JoinTable(
            name = "profissional_servico",
            joinColumns = @JoinColumn(name = "servico_id"),
            inverseJoinColumns = @JoinColumn(name = "profissional_id")
    )
    private List<Profissional> profissionais = new ArrayList<>();
}
