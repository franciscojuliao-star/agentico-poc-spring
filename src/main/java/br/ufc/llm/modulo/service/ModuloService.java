package br.ufc.llm.modulo.service;

import br.ufc.llm.curso.exception.CursoNaoEncontradoException;
import br.ufc.llm.curso.repository.CursoRepository;
import br.ufc.llm.modulo.domain.Modulo;
import br.ufc.llm.modulo.dto.ModuloResponse;
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

@Slf4j
@Service
public class ModuloService {

    private final ModuloRepository moduloRepository;
    private final CursoRepository cursoRepository;
    private final String uploadDir;

    public ModuloService(ModuloRepository moduloRepository,
                         CursoRepository cursoRepository,
                         @Value("${upload.directory}") String uploadDir) {
        this.moduloRepository = moduloRepository;
        this.cursoRepository = cursoRepository;
        this.uploadDir = uploadDir;
    }

    public List<ModuloResponse> listar(Long cursoId, String emailProfessor) {
        var curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new CursoNaoEncontradoException(cursoId));

        if (!curso.getProfessor().getEmail().equals(emailProfessor)) {
            throw new CursoNaoEncontradoException(cursoId);
        }

        return moduloRepository.findByCursoIdOrderByOrdem(cursoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ModuloResponse adicionar(Long cursoId, String emailProfessor) {
        var curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new CursoNaoEncontradoException(cursoId));

        if (!curso.getProfessor().getEmail().equals(emailProfessor)) {
            throw new CursoNaoEncontradoException(cursoId);
        }

        int total = moduloRepository.countByCursoId(cursoId);
        int novaOrdem = total + 1;
        String nome = String.format("Módulo %02d", novaOrdem);

        var modulo = Modulo.builder()
                .nome(nome)
                .ordem(novaOrdem)
                .curso(curso)
                .build();

        return toResponse(moduloRepository.save(modulo));
    }

    public ModuloResponse atualizarCapa(Long moduloId, MultipartFile capa, String emailProfessor) {
        var modulo = moduloRepository.findByIdAndCursoProfessorEmail(moduloId, emailProfessor)
                .orElseThrow(() -> new ModuloNaoEncontradoException(moduloId));

        modulo.setCapa(salvarArquivo(capa, emailProfessor));
        return toResponse(moduloRepository.save(modulo));
    }

    public void excluir(Long moduloId, String emailProfessor) {
        var modulo = moduloRepository.findByIdAndCursoProfessorEmail(moduloId, emailProfessor)
                .orElseThrow(() -> new ModuloNaoEncontradoException(moduloId));

        Long cursoId = modulo.getCurso().getId();
        moduloRepository.delete(modulo);

        List<Modulo> restantes = moduloRepository.findByCursoIdOrderByOrdem(cursoId)
                .stream()
                .filter(m -> !m.getId().equals(moduloId))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < restantes.size(); i++) {
            Modulo m = restantes.get(i);
            m.setOrdem(i + 1);
            m.setNome(String.format("Módulo %02d", i + 1));
        }
        moduloRepository.saveAll(restantes);
    }

    public void reordenar(Long moduloId, int novaOrdem, String emailProfessor) {
        var alvo = moduloRepository.findByIdAndCursoProfessorEmail(moduloId, emailProfessor)
                .orElseThrow(() -> new ModuloNaoEncontradoException(moduloId));

        List<Modulo> todos = moduloRepository.findByCursoIdOrderByOrdem(alvo.getCurso().getId());

        todos.remove(alvo);
        int posicao = Math.min(novaOrdem - 1, todos.size());
        todos.add(posicao, alvo);

        for (int i = 0; i < todos.size(); i++) {
            todos.get(i).setOrdem(i + 1);
        }

        moduloRepository.saveAll(todos);
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
            throw new RuntimeException("Erro ao salvar a capa do módulo.", e);
        }
        return nome;
    }

    private String extrairNomeUsuario(String email) {
        int arroba = email.indexOf('@');
        return arroba > 0 ? email.substring(0, arroba) : email;
    }

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) return ".jpg";
        return nomeOriginal.substring(nomeOriginal.lastIndexOf('.'));
    }

    private ModuloResponse toResponse(Modulo modulo) {
        return new ModuloResponse(
                modulo.getId(),
                modulo.getNome(),
                modulo.getOrdem(),
                modulo.getCapa(),
                modulo.getCurso().getId()
        );
    }
}
