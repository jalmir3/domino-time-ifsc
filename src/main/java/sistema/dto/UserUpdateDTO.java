package sistema.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nickname;
    private String email;
    private String birthDate;
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
    private String avatarBase64;
}
