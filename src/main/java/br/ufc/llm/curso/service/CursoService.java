package br.ufc.llm.curso.service;

import br.ufc.llm.curso.domain.Curso;
import br.ufc.llm.curso.domain.StatusCurso;
import br.ufc.llm.curso.dto.ConfigurarMatriculaRequest;
import br.ufc.llm.curso.dto.CriarCursoRequest;
import br.ufc.llm.curso.dto.CursoResponse;
import br.ufc.llm.curso.dto.ListaCursosResponse;
import br.ufc.llm.curso.exception.CursoNaoEncontradoException;
import br.ufc.llm.curso.repository.CursoRepository;
import br.ufc.llm.usuario.exception.UsuarioNaoEncontradoException;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final String uploadDir;

    public CursoService(CursoRepository cursoRepository,
                        UsuarioRepository usuarioRepository,
                        @Value("${upload.directory}") String uploadDir) {
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.uploadDir = uploadDir;
    }

    public CursoResponse criar(CriarCursoRequest request, MultipartFile capa, String emailProfessor) {
        var professor = usuarioRepository.findByEmail(emailProfessor)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(0L));

        String nomeCapa = null;
        if (capa != null && !capa.isEmpty()) {
            nomeCapa = salvarArquivo(capa, emailProfessor);
        }

        var curso = Curso.builder()
                .titulo(request.titulo())
                .categoria(request.categoria().toLowerCase())
                .descricao(request.descricao())
                .cargaHoraria(request.cargaHoraria())
                .capa(nomeCapa)
                .professor(professor)
                .build();

        return toResponse(cursoRepository.save(curso));
    }

    public ListaCursosResponse listar(String emailProfessor) {
        var ativos = cursoRepository.findByProfessorEmailAndStatusIn(emailProfessor, List.of(StatusCurso.RASCUNHO, StatusCurso.PUBLICADO))
                .stream().map(this::toResponse).toList();
        var arquivados = cursoRepository.findByProfessorEmailAndStatusIn(emailProfessor, List.of(StatusCurso.ARQUIVADO))
                .stream().map(this::toResponse).toList();
        return new ListaCursosResponse(ativos, arquivados);
    }

    public void configurarMatricula(Long cursoId, ConfigurarMatriculaRequest request, String emailProfessor) {
        var curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new CursoNaoEncontradoException(cursoId));

        if (!curso.getProfessor().getEmail().equals(emailProfessor)) {
            throw new CursoNaoEncontradoException(cursoId);
        }

        curso.setRequerEndereco(request.requerEndereco());
        curso.setRequerGenero(request.requerGenero());
        curso.setRequerIdade(request.requerIdade());
        cursoRepository.save(curso);
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
            throw new RuntimeException("Erro ao salvar a capa do curso.", e);
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

    private CursoResponse toResponse(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getTitulo(),
                curso.getCategoria(),
                curso.getDescricao(),
                curso.getCargaHoraria(),
                curso.getCapa(),
                curso.getStatus(),
                curso.getProfessor().getId(),
                curso.getCriadoEm()
        );
    }
}
