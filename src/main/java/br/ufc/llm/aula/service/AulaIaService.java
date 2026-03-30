package br.ufc.llm.aula.service;

import br.ufc.llm.aula.domain.Aula;
import br.ufc.llm.aula.domain.TipoArquivo;
import br.ufc.llm.aula.dto.ConteudoGeradoResponse;
import br.ufc.llm.aula.exception.AulaNaoEncontradaException;
import br.ufc.llm.aula.exception.ConteudoGeradoAusenteException;
import br.ufc.llm.aula.exception.ConteudoInsuficienteException;
import br.ufc.llm.aula.repository.AulaRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Service
public class AulaIaService {

    private static final String PROMPT_TEMPLATE = """
            Você é um especialista em educação online. A partir do conteúdo abaixo, gere um conteúdo de aula \
            formatado em HTML semântico, bem estruturado, com títulos (h2, h3), parágrafos, listas e destaques \
            onde apropriado. Retorne apenas o HTML sem ```html ``` delimitadores.

            Conteúdo:
            %s
            """;

    private final AulaRepository aulaRepository;
    private final ChatClient chatClient;
    private final String uploadDir;

    public AulaIaService(AulaRepository aulaRepository,
                         ChatClient chatClient,
                         @Value("${upload.directory}") String uploadDir) {
        this.aulaRepository = aulaRepository;
        this.chatClient = chatClient;
        this.uploadDir = uploadDir;
    }

    public ConteudoGeradoResponse gerarConteudo(Long aulaId, String emailProfessor) {
        var aula = aulaRepository.findByIdAndModuloCursoProfessorEmail(aulaId, emailProfessor)
                .orElseThrow(() -> new AulaNaoEncontradaException(aulaId));

        String textoBase = extrairTexto(aula);

        String conteudoGerado = chatClient.prompt()
                .user(PROMPT_TEMPLATE.formatted(textoBase))
                .call()
                .content();

        aula.setConteudoGerado(conteudoGerado);
        aulaRepository.save(aula);

        return new ConteudoGeradoResponse(aula.getId(), conteudoGerado);
    }

    public ConteudoGeradoResponse confirmarConteudo(Long aulaId, String emailProfessor, String conteudo) {
        var aula = aulaRepository.findByIdAndModuloCursoProfessorEmail(aulaId, emailProfessor)
                .orElseThrow(() -> new AulaNaoEncontradaException(aulaId));

        if (aula.getConteudoGerado() == null || aula.getConteudoGerado().isBlank()) {
            throw new ConteudoGeradoAusenteException();
        }

        aula.setConteudoGerado(conteudo);
        aulaRepository.save(aula);
        return new ConteudoGeradoResponse(aula.getId(), aula.getConteudoGerado());
    }

    private String extrairTexto(Aula aula) {
        if (aula.getArquivo() != null && aula.getTipoArquivo() == TipoArquivo.PDF) {
            try {
                return extrairTextoPdf(Path.of(uploadDir, aula.getArquivo()));
            } catch (IOException e) {
                log.warn("Falha ao extrair texto do PDF, usando CKEditor como fallback. aulaId={}", aula.getId(), e);
            }
        }

        if (aula.getConteudoCkEditor() != null && !aula.getConteudoCkEditor().isBlank()) {
            return aula.getConteudoCkEditor();
        }

        throw new ConteudoInsuficienteException();
    }

    private String extrairTextoPdf(Path caminho) throws IOException {
        try (PDDocument doc = Loader.loadPDF(caminho.toFile())) {
            return new PDFTextStripper().getText(doc);
        }
    }
}
