package fhdw.de.einkauf_service.dto;

import lombok.Data;

@Data
public class ContactPersonResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String role;
    private String phone;
    private String email;
}
