package br.ufc.llm.usuario.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PerfilCadastroValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PerfilCadastroValido {

    String message() default "Perfil inválido para cadastro. Valores aceitos: PROFESSOR, ALUNO";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
