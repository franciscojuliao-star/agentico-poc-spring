package br.ufc.llm.aula.service;

import br.ufc.llm.aula.domain.Aula;
import br.ufc.llm.aula.domain.TipoArquivo;
import br.ufc.llm.aula.dto.AulaResponse;
import br.ufc.llm.aula.dto.CriarAulaRequest;
import br.ufc.llm.aula.dto.EditarAulaRequest;
import br.ufc.llm.aula.exception.AulaNaoEncontradaException;
import br.ufc.llm.aula.repository.AulaRepository;
import br.ufc.llm.modulo.exception.ModuloNaoEncontradoException;
import br.ufc.llm.modulo.repository.ModuloRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AulaService {

    private static final Set<String> EXTENSOES_VIDEO = Set.of(".mp4", ".avi", ".mov", ".mkv", ".webm");
    private static final Set<String> EXTENSOES_PDF = Set.of(".pdf");

    private final AulaRepository aulaRepository;
    private final ModuloRepository moduloRepository;
    private final String uploadDir;

    public AulaService(AulaRepository aulaRepository,
                       ModuloRepository moduloRepository,
                       @Value("${upload.directory}") String uploadDir) {
        this.aulaRepository = aulaRepository;
        this.moduloRepository = moduloRepository;
        this.uploadDir = uploadDir;
    }

    public List<AulaResponse> listar(Long moduloId, String emailProfessor) {
        moduloRepository.findByIdAndCursoProfessorEmail(moduloId, emailProfessor)
                .orElseThrow(() -> new ModuloNaoEncontradoException(moduloId));

        return aulaRepository.findByModuloIdOrderByOrdem(moduloId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AulaResponse adicionar(Long moduloId, CriarAulaRequest request, MultipartFile arquivo, String emailProfessor) {
        var modulo = moduloRepository.findByIdAndCursoProfessorEmail(moduloId, emailProfessor)
                .orElseThrow(() -> new ModuloNaoEncontradoException(moduloId));

        int total = aulaRepository.countByModuloId(moduloId);
        int novaOrdem = total + 1;

        var builder = Aula.builder()
                .nome(request.nome())
                .ordem(novaOrdem)
                .conteudoCkEditor(request.conteudoCkEditor())
                .modulo(modulo);

        if (arquivo != null && !arquivo.isEmpty()) {
            builder.arquivo(salvarArquivo(arquivo, emailProfessor))
                   .tipoArquivo(detectarTipo(arquivo.getOriginalFilename()));
        }

        return toResponse(aulaRepository.save(builder.build()));
    }

    public AulaResponse atualizarArquivo(Long aulaId, MultipartFile arquivo, String emailProfessor) {
        var aula = aulaRepository.findByIdAndModuloCursoProfessorEmail(aulaId, emailProfessor)
                .orElseThrow(() -> new AulaNaoEncontradaException(aulaId));

        String nome = salvarArquivo(arquivo, emailProfessor);
        TipoArquivo tipo = detectarTipo(arquivo.getOriginalFilename());

        aula.setArquivo(nome);
        aula.setTipoArquivo(tipo);

        return toResponse(aulaRepository.save(aula));
    }

    public AulaResponse editar(Long aulaId, EditarAulaRequest request, String emailProfessor) {
        var aula = aulaRepository.findByIdAndModuloCursoProfessorEmail(aulaId, emailProfessor)
                .orElseThrow(() -> new AulaNaoEncontradaException(aulaId));

        aula.setNome(request.nome());
        aula.setConteudoCkEditor(request.conteudoCkEditor());

        return toResponse(aulaRepository.save(aula));
    }

    public void excluir(Long aulaId, String emailProfessor) {
        var aula = aulaRepository.findByIdAndModuloCursoProfessorEmail(aulaId, emailProfessor)
                .orElseThrow(() -> new AulaNaoEncontradaException(aulaId));

        Long moduloId = aula.getModulo().getId();
        aulaRepository.delete(aula);

        List<Aula> restantes = aulaRepository.findByModuloIdOrderByOrdem(moduloId)
                .stream()
                .filter(a -> !a.getId().equals(aulaId))
                .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < restantes.size(); i++) {
            restantes.get(i).setOrdem(i + 1);
        }
        aulaRepository.saveAll(restantes);
    }

    public void reordenar(Long aulaId, int novaOrdem, String emailProfessor) {
        var alvo = aulaRepository.findByIdAndModuloCursoProfessorEmail(aulaId, emailProfessor)
                .orElseThrow(() -> new AulaNaoEncontradaException(aulaId));

        List<Aula> todas = aulaRepository.findByModuloIdOrderByOrdem(alvo.getModulo().getId());

        todas.remove(alvo);
        int posicao = Math.min(novaOrdem - 1, todas.size());
        todas.add(posicao, alvo);

        for (int i = 0; i < todas.size(); i++) {
            todas.get(i).setOrdem(i + 1);
        }

        aulaRepository.saveAll(todas);
    }

    private TipoArquivo detectarTipo(String nomeOriginal) {
        if (nomeOriginal == null) return TipoArquivo.PDF;
        String ext = nomeOriginal.substring(nomeOriginal.lastIndexOf('.')).toLowerCase();
        if (EXTENSOES_VIDEO.contains(ext)) return TipoArquivo.VIDEO;
        return TipoArquivo.PDF;
    }

    private String salvarArquivo(MultipartFile arquivo, String email) {
        String nome = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "_" + extrairNomeUsuario(email)
                + extrairExtensao(arquivo.getOriginalFilename());
        try {
            Path destino = Path.of(uploadDir, nome);
            Files.createDirectories(destino.getParent());
            arquivo.transferTo(destino);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo da aula.", e);
        }
        return nome;
    }

    private String extrairNomeUsuario(String email) {
        int arroba = email.indexOf('@');
        return arroba > 0 ? email.substring(0, arroba) : email;
    }

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) return ".bin";
        return nomeOriginal.substring(nomeOriginal.lastIndexOf('.'));
    }

    private AulaResponse toResponse(Aula aula) {
        return new AulaResponse(
                aula.getId(),
                aula.getNome(),
                aula.getOrdem(),
                aula.getArquivo(),
                aula.getTipoArquivo(),
                aula.getConteudoCkEditor(),
                aula.getConteudoGerado(),
                aula.getModulo().getId()
        );
    }
}
