package br.ufc.llm.prova.repository;

import br.ufc.llm.prova.domain.Prova;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProvaRepository extends JpaRepository<Prova, Long> {

    Optional<Prova> findByModuloId(Long moduloId);

    Optional<Prova> findByIdAndModuloCursoProfessorEmail(Long id, String email);

    Optional<Prova> findByModuloIdAndModuloCursoProfessorEmail(Long moduloId, String email);

    boolean existsByModuloId(Long moduloId);
}
