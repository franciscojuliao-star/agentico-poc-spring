package br.ufc.llm.curso.repository;

import br.ufc.llm.curso.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
