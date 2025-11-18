package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SupplierRequestDTO {

    @NotBlank
    private String name;

    private String street;
    private String houseNumber;
    private String zip;
    private String city;

    private String country;
    @Email
    private String email;
    private String phone;

    @NotNull
    private Long paymentTermId;

    private List<ContactPersonRequestDTO> contactPeople;
    private Boolean isActive;

}
