package br.ufc.llm.curso.domain;

import br.ufc.llm.usuario.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cursos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "carga_horaria", nullable = false, length = 20)
    private String cargaHoraria;

    @Column(length = 500)
    private String capa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusCurso status = StatusCurso.RASCUNHO;

    @Column(name = "requer_endereco", nullable = false)
    @Builder.Default
    private boolean requerEndereco = false;

    @Column(name = "requer_genero", nullable = false)
    @Builder.Default
    private boolean requerGenero = false;

    @Column(name = "requer_idade", nullable = false)
    @Builder.Default
    private boolean requerIdade = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Usuario professor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}
