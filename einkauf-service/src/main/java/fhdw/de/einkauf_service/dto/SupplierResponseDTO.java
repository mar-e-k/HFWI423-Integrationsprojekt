package fhdw.de.einkauf_service.dto;


import lombok.Data;
import java.util.List;

@Data
public class SupplierResponseDTO {
    private Long id;
    private String name;
    private String street;
    private String houseNumber;
    private String zip;
    private String city;
    private String country;
    private String email;
    private String phone;
    private PaymentTermResponseDTO paymentTerm;
    private List<ContactPersonResponseDTO> contactPeople;

    private Long paymentTermId;
    private String paymentTermDefinition;
    private String paymentTermDescription;
}
