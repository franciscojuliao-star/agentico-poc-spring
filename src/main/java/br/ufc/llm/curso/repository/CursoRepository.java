package br.ufc.llm.curso.repository;

import br.ufc.llm.curso.domain.Curso;
import br.ufc.llm.curso.domain.StatusCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    List<Curso> findByProfessorEmailAndStatusIn(String email, List<StatusCurso> statuses);
}
