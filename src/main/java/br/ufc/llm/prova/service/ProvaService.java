package br.ufc.llm.prova.service;

import br.ufc.llm.modulo.exception.ModuloNaoEncontradoException;
import br.ufc.llm.modulo.repository.ModuloRepository;
import br.ufc.llm.prova.domain.Alternativa;
import br.ufc.llm.prova.domain.Pergunta;
import br.ufc.llm.prova.domain.Prova;
import br.ufc.llm.prova.dto.*;
import br.ufc.llm.prova.exception.*;
import br.ufc.llm.prova.repository.AlternativaRepository;
import br.ufc.llm.prova.repository.PerguntaRepository;
import br.ufc.llm.prova.repository.ProvaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProvaService {

    private final ProvaRepository provaRepository;
    private final PerguntaRepository perguntaRepository;
    private final AlternativaRepository alternativaRepository;
    private final ModuloRepository moduloRepository;

    public ProvaResponse criar(Long moduloId, String emailProfessor) {
        var modulo = moduloRepository.findByIdAndCursoProfessorEmail(moduloId, emailProfessor)
                .orElseThrow(() -> new ModuloNaoEncontradoException(moduloId));

        if (provaRepository.existsByModuloId(moduloId)) {
            throw new ProvaJaExisteException(moduloId);
        }

        var prova = Prova.builder().modulo(modulo).build();
        return toResponse(provaRepository.save(prova));
    }

    public ProvaResponse buscar(Long moduloId, String emailProfessor) {
        var prova = provaRepository.findByModuloIdAndModuloCursoProfessorEmail(moduloId, emailProfessor)
                .orElseThrow(() -> new ProvaNaoEncontradaException(moduloId));
        return toResponse(prova);
    }

    public ProvaResponse configurar(Long provaId, ConfigurarProvaRequest request, String emailProfessor) {
        var prova = provaRepository.findByIdAndModuloCursoProfessorEmail(provaId, emailProfessor)
                .orElseThrow(() -> new ProvaNaoEncontradaException(provaId));

        prova.setMostrarRespostasErradas(request.mostrarRespostasErradas());
        prova.setMostrarRespostasCorretas(request.mostrarRespostasCorretas());
        prova.setMostrarValores(request.mostrarValores());

        return toResponse(provaRepository.save(prova));
    }

    public void excluir(Long provaId, String emailProfessor) {
        var prova = provaRepository.findByIdAndModuloCursoProfessorEmail(provaId, emailProfessor)
                .orElseThrow(() -> new ProvaNaoEncontradaException(provaId));
        provaRepository.delete(prova);
    }

    public PerguntaResponse adicionarPergunta(Long provaId, PerguntaRequest request, String emailProfessor) {
        var prova = provaRepository.findByIdAndModuloCursoProfessorEmail(provaId, emailProfessor)
                .orElseThrow(() -> new ProvaNaoEncontradaException(provaId));

        int total = perguntaRepository.countByProvaId(provaId);
        int novaOrdem = total + 1;

        var pergunta = Pergunta.builder()
                .enunciado(request.enunciado())
                .pontos(request.pontos())
                .ordem(novaOrdem)
                .prova(prova)
                .build();

        List<Alternativa> alternativas = request.alternativas().stream()
                .map(a -> Alternativa.builder()
                        .texto(a.texto())
                        .correta(a.correta())
                        .pergunta(pergunta)
                        .build())
                .toList();
        pergunta.setAlternativas(alternativas);

        return toPerguntaResponse(perguntaRepository.save(pergunta));
    }

    public PerguntaResponse editarPergunta(Long perguntaId, PerguntaRequest request, String emailProfessor) {
        var pergunta = perguntaRepository.findByIdAndProvaModuloCursoProfessorEmail(perguntaId, emailProfessor)
                .orElseThrow(() -> new PerguntaNaoEncontradaException(perguntaId));

        pergunta.setEnunciado(request.enunciado());
        pergunta.setPontos(request.pontos());
        pergunta.getAlternativas().clear();
        List<Alternativa> novas = request.alternativas().stream()
                .map(a -> Alternativa.builder()
                        .texto(a.texto())
                        .correta(a.correta())
                        .pergunta(pergunta)
                        .build())
                .toList();
        pergunta.getAlternativas().addAll(novas);

        return toPerguntaResponse(perguntaRepository.save(pergunta));
    }

    public void excluirPergunta(Long perguntaId, String emailProfessor) {
        var pergunta = perguntaRepository.findByIdAndProvaModuloCursoProfessorEmail(perguntaId, emailProfessor)
                .orElseThrow(() -> new PerguntaNaoEncontradaException(perguntaId));
        perguntaRepository.delete(pergunta);
    }

    public AlternativaResponse adicionarAlternativa(Long perguntaId, AlternativaRequest request, String emailProfessor) {
        var pergunta = perguntaRepository.findByIdAndProvaModuloCursoProfessorEmail(perguntaId, emailProfessor)
                .orElseThrow(() -> new PerguntaNaoEncontradaException(perguntaId));

        var alternativa = Alternativa.builder()
                .texto(request.texto())
                .correta(request.correta())
                .pergunta(pergunta)
                .build();

        return toAlternativaResponse(alternativaRepository.save(alternativa));
    }

    public AlternativaResponse editarAlternativa(Long alternativaId, AlternativaRequest request, String emailProfessor) {
        var alternativa = alternativaRepository.findByIdAndPerguntaProvaModuloCursoProfessorEmail(alternativaId, emailProfessor)
                .orElseThrow(() -> new AlternativaNaoEncontradaException(alternativaId));

        alternativa.setTexto(request.texto());
        alternativa.setCorreta(request.correta());

        return toAlternativaResponse(alternativaRepository.save(alternativa));
    }

    public void excluirAlternativa(Long alternativaId, String emailProfessor) {
        var alternativa = alternativaRepository.findByIdAndPerguntaProvaModuloCursoProfessorEmail(alternativaId, emailProfessor)
                .orElseThrow(() -> new AlternativaNaoEncontradaException(alternativaId));
        alternativaRepository.delete(alternativa);
    }

    private ProvaResponse toResponse(Prova prova) {
        List<PerguntaResponse> perguntas = prova.getPerguntas().stream()
                .map(this::toPerguntaResponse)
                .toList();
        return new ProvaResponse(
                prova.getId(),
                prova.getModulo().getId(),
                prova.isMostrarRespostasErradas(),
                prova.isMostrarRespostasCorretas(),
                prova.isMostrarValores(),
                perguntas
        );
    }

    private PerguntaResponse toPerguntaResponse(Pergunta pergunta) {
        List<AlternativaResponse> alternativas = pergunta.getAlternativas().stream()
                .map(this::toAlternativaResponse)
                .toList();
        return new PerguntaResponse(
                pergunta.getId(),
                pergunta.getEnunciado(),
                pergunta.getPontos(),
                pergunta.getOrdem(),
                alternativas
        );
    }

    private AlternativaResponse toAlternativaResponse(Alternativa alternativa) {
        return new AlternativaResponse(
                alternativa.getId(),
                alternativa.getTexto(),
                alternativa.isCorreta()
        );
    }
}
