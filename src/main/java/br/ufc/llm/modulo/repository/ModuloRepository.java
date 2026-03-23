package br.ufc.llm.modulo.repository;

import br.ufc.llm.modulo.domain.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    List<Modulo> findByCursoIdOrderByOrdem(Long cursoId);

    int countByCursoId(Long cursoId);

    Optional<Modulo> findByIdAndCursoProfessorEmail(Long id, String email);
}
