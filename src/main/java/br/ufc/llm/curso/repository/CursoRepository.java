package br.ufc.llm.curso.repository;

import br.ufc.llm.curso.domain.Curso;
import br.ufc.llm.curso.domain.StatusCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    List<Curso> findByProfessorEmailAndStatusIn(String email, List<StatusCurso> statuses);

    @org.springframework.data.jpa.repository.Query("""
            SELECT c FROM Curso c
            WHERE c.professor.email = :email
            AND (LOWER(c.titulo) LIKE LOWER(CONCAT('%', :termo, '%'))
              OR LOWER(c.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))
              OR LOWER(c.categoria) LIKE LOWER(CONCAT('%', :termo, '%')))
            """)
    List<Curso> buscarPorTexto(String termo, String email);
}
