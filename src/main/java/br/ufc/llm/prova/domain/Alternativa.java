package br.ufc.llm.prova.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alternativas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alternativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(nullable = false)
    private boolean correta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pergunta_id", nullable = false)
    private Pergunta pergunta;
}
