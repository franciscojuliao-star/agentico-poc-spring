package br.ufc.llm.prova.repository;

import br.ufc.llm.prova.domain.Alternativa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlternativaRepository extends JpaRepository<Alternativa, Long> {

    Optional<Alternativa> findByIdAndPerguntaProvaModuloCursoProfessorEmail(Long id, String email);
}
