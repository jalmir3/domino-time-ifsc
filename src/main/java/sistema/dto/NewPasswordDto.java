package sistema.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class NewPasswordDto {
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String newPassword;
    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmPassword;
    public void validatePasswordMatch() {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }
    }
}