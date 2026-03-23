package br.ufc.llm.aula.repository;

import br.ufc.llm.aula.domain.Aula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AulaRepository extends JpaRepository<Aula, Long> {

    List<Aula> findByModuloIdOrderByOrdem(Long moduloId);

    int countByModuloId(Long moduloId);

    Optional<Aula> findByIdAndModuloCursoProfessorEmail(Long id, String email);
}
