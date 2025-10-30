package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ContactPersonRequestDTO {

    private Long id;

    @NotBlank(message = "First name is mandatory.")
    private String firstName;

    @NotBlank(message = "Last name is mandatory.")
    private String lastName;

    @Size(max = 100, message = "Role is too long (max 100 characters).")
    private String role;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()/]*$", message = "Please enter a valid phone number.")
    private String phone;

    @NotBlank(message = "E-Mail is mandatory.")
    @Email(message = "Please enter a valid E-Mail.")
    @Size(max = 255, message = "E-Mail is too long.")
    private String email;
}
