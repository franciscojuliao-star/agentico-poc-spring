package br.ufc.llm.prova.domain;

import br.ufc.llm.modulo.domain.Modulo;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "provas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prova {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id", nullable = false, unique = true)
    private Modulo modulo;

    @Column(name = "mostrar_respostas_erradas", nullable = false)
    private boolean mostrarRespostasErradas;

    @Column(name = "mostrar_respostas_corretas", nullable = false)
    private boolean mostrarRespostasCorretas;

    @Column(name = "mostrar_valores", nullable = false)
    private boolean mostrarValores;

    @OneToMany(mappedBy = "prova", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    @Builder.Default
    private List<Pergunta> perguntas = new ArrayList<>();
}
