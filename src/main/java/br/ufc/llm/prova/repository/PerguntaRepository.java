package br.ufc.llm.prova.repository;

import br.ufc.llm.prova.domain.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {

    int countByProvaId(Long provaId);

    Optional<Pergunta> findByIdAndProvaModuloCursoProfessorEmail(Long id, String email);
}
