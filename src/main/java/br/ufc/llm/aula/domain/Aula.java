package br.ufc.llm.aula.domain;

import br.ufc.llm.modulo.domain.Modulo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "aulas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private int ordem;

    @Column(length = 500)
    private String arquivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_arquivo", length = 10)
    private TipoArquivo tipoArquivo;

    @Column(name = "conteudo_ck_editor", columnDefinition = "TEXT")
    private String conteudoCkEditor;

    @Column(name = "conteudo_gerado", columnDefinition = "TEXT")
    private String conteudoGerado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;
}
