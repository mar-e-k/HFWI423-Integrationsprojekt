package fhdw.de.einkauf_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Ansprechpartner eines Lieferanten")
public class ContactPersonRequestDTO {
    public ContactPersonRequestDTO(Long id, String firstName, String lastName, String role, String phone, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.phone = phone;
        this.email = email;
    }

    public ContactPersonRequestDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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
