package br.ufc.llm.prova.service;

import br.ufc.llm.aula.domain.Aula;
import br.ufc.llm.aula.domain.TipoArquivo;
import br.ufc.llm.aula.exception.ConteudoInsuficienteException;
import br.ufc.llm.aula.repository.AulaRepository;
import br.ufc.llm.modulo.exception.ModuloNaoEncontradoException;
import br.ufc.llm.modulo.repository.ModuloRepository;
import br.ufc.llm.prova.domain.Alternativa;
import br.ufc.llm.prova.domain.Pergunta;
import br.ufc.llm.prova.dto.AlternativaRequest;
import br.ufc.llm.prova.dto.AlternativaResponse;
import br.ufc.llm.prova.dto.PerguntaRequest;
import br.ufc.llm.prova.dto.PerguntaResponse;
import br.ufc.llm.prova.dto.QuizGeradoResponse;
import br.ufc.llm.prova.exception.ProvaNaoEncontradaException;
import br.ufc.llm.prova.repository.PerguntaRepository;
import br.ufc.llm.prova.repository.ProvaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class QuizIaService {

    private static final String PROMPT_TEMPLATE = """
            Você é um especialista em avaliações educacionais. A partir do conteúdo abaixo, gere %d perguntas \
            de múltipla escolha em formato JSON. Retorne APENAS um array JSON sem nenhum texto adicional, \
            seguindo exatamente este formato:
            [{"enunciado":"...","pontos":1,"alternativas":[{"texto":"...","correta":true},{"texto":"...","correta":false}]}]

            Cada pergunta deve ter exatamente 4 alternativas, com apenas 1 correta.

            Conteúdo do módulo:
            %s
            """;

    private static final int QUANTIDADE_PERGUNTAS_DEFAULT = 5;

    private final ProvaRepository provaRepository;
    private final PerguntaRepository perguntaRepository;
    private final ModuloRepository moduloRepository;
    private final AulaRepository aulaRepository;
    private final ChatClient chatClient;
    private final String uploadDir;
    private final ObjectMapper objectMapper;

    public QuizIaService(ProvaRepository provaRepository,
                         PerguntaRepository perguntaRepository,
                         ModuloRepository moduloRepository,
                         AulaRepository aulaRepository,
                         ChatClient chatClient,
                         @Value("${upload.directory}") String uploadDir,
                         ObjectMapper objectMapper) {
        this.provaRepository = provaRepository;
        this.perguntaRepository = perguntaRepository;
        this.moduloRepository = moduloRepository;
        this.aulaRepository = aulaRepository;
        this.chatClient = chatClient;
        this.uploadDir = uploadDir;
        this.objectMapper = objectMapper;
    }

    public QuizGeradoResponse gerarQuiz(Long moduloId, String emailProfessor) {
        moduloRepository.findByIdAndCursoProfessorEmail(moduloId, emailProfessor)
                .orElseThrow(() -> new ModuloNaoEncontradoException(moduloId));

        provaRepository.findByModuloIdAndModuloCursoProfessorEmail(moduloId, emailProfessor)
                .orElseThrow(() -> new ProvaNaoEncontradaException(moduloId));

        String conteudo = coletarConteudoModulo(moduloId);

        String json = chatClient.prompt()
                .user(PROMPT_TEMPLATE.formatted(QUANTIDADE_PERGUNTAS_DEFAULT, conteudo))
                .call()
                .content();

        List<PerguntaResponse> perguntas = parsePerguntas(json);
        return new QuizGeradoResponse(perguntas);
    }

    public void salvarPerguntas(Long moduloId, List<PerguntaRequest> perguntas, String emailProfessor) {
        var prova = provaRepository.findByModuloIdAndModuloCursoProfessorEmail(moduloId, emailProfessor)
                .orElseThrow(() -> new ProvaNaoEncontradaException(moduloId));

        int total = perguntaRepository.countByProvaId(prova.getId());
        int ordem = total + 1;

        for (PerguntaRequest req : perguntas) {
            var pergunta = Pergunta.builder()
                    .enunciado(req.enunciado())
                    .pontos(req.pontos())
                    .ordem(ordem++)
                    .prova(prova)
                    .build();

            List<Alternativa> alternativas = req.alternativas().stream()
                    .map(a -> Alternativa.builder()
                            .texto(a.texto())
                            .correta(a.correta())
                            .pergunta(pergunta)
                            .build())
                    .toList();
            pergunta.setAlternativas(alternativas);

            perguntaRepository.save(pergunta);
        }
    }

    private String coletarConteudoModulo(Long moduloId) {
        var aulas = aulaRepository.findByModuloIdOrderByOrdem(moduloId);
        StringBuilder sb = new StringBuilder();

        for (Aula aula : aulas) {
            if (aula.getConteudoCkEditor() != null && !aula.getConteudoCkEditor().isBlank()) {
                sb.append(aula.getConteudoCkEditor()).append("\n");
            } else if (aula.getConteudoGerado() != null && !aula.getConteudoGerado().isBlank()) {
                sb.append(aula.getConteudoGerado()).append("\n");
            } else if (aula.getArquivo() != null && aula.getTipoArquivo() == TipoArquivo.PDF) {
                try {
                    sb.append(extrairTextoPdf(Path.of(uploadDir, aula.getArquivo()))).append("\n");
                } catch (IOException e) {
                    log.warn("Falha ao extrair PDF da aula {}. Ignorando.", aula.getId(), e);
                }
            }
        }

        if (sb.isEmpty()) {
            throw new ConteudoInsuficienteException();
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private List<PerguntaResponse> parsePerguntas(String json) {
        try {
            String jsonLimpo = json.trim()
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("```", "")
                    .trim();

            List<Map<String, Object>> lista = objectMapper.readValue(jsonLimpo, new TypeReference<>() {});
            return lista.stream().map(m -> {
                String enunciado = (String) m.get("enunciado");
                int pontos = ((Number) m.getOrDefault("pontos", 1)).intValue();
                List<Map<String, Object>> alts = (List<Map<String, Object>>) m.get("alternativas");
                List<AlternativaResponse> alternativas = alts == null ? List.of() : alts.stream()
                        .map(a -> new AlternativaResponse(null, (String) a.get("texto"), Boolean.TRUE.equals(a.get("correta"))))
                        .toList();
                return new PerguntaResponse(null, enunciado, pontos, 0, alternativas);
            }).toList();
        } catch (Exception e) {
            log.error("Falha ao parsear JSON do quiz gerado pela IA", e);
            return List.of();
        }
    }

    private String extrairTextoPdf(Path caminho) throws IOException {
        try (PDDocument doc = Loader.loadPDF(caminho.toFile())) {
            return new PDFTextStripper().getText(doc);
        }
    }
}
