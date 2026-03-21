package br.ufc.llm.usuario.validation;

import br.ufc.llm.usuario.domain.PerfilUsuario;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PerfilCadastroValidator implements ConstraintValidator<PerfilCadastroValido, PerfilUsuario> {

    @Override
    public boolean isValid(PerfilUsuario perfil, ConstraintValidatorContext context) {
        if (perfil == null) return true; // @NotNull cuida do null
        return perfil == PerfilUsuario.PROFESSOR || perfil == PerfilUsuario.ALUNO;
    }
}
