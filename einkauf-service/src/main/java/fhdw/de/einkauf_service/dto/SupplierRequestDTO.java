package fhdw.de.einkauf_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Eingabe-DTO zum Anlegen oder Aktualisieren eines Lieferanten")
public class SupplierRequestDTO {

    public SupplierRequestDTO() {
    }

    public SupplierRequestDTO(String name, String street, String houseNumber, String zip, String city, String country, String email, String phone, Long paymentTermId, List<ContactPersonRequestDTO> contactPeople, Boolean isActive) {
        this.name = name;
        this.street = street;
        this.houseNumber = houseNumber;
        this.zip = zip;
        this.city = city;
        this.country = country;
        this.email = email;
        this.phone = phone;
        this.paymentTermId = paymentTermId;
        this.contactPeople = contactPeople;
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getPaymentTermId() {
        return paymentTermId;
    }

    public void setPaymentTermId(Long paymentTermId) {
        this.paymentTermId = paymentTermId;
    }

    public List<ContactPersonRequestDTO> getContactPeople() {
        return contactPeople;
    }

    public void setContactPeople(List<ContactPersonRequestDTO> contactPeople) {
        this.contactPeople = contactPeople;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    @Schema(description = "Firmenname des Lieferanten", example = "Mustermann GmbH", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "Straße", example = "Musterweg")
    private String street;
    @Schema(description = "Hausnummer", example = "12a")
    private String houseNumber;
    @Schema(description = "PLZ", example = "33100")
    private String zip;
    @Schema(description = "Ort", example = "Paderborn")
    private String city;

    @Schema(description = "Land", example = "Deutschland")
    private String country;
    @Schema(description = "E-Mail-Kontakt", example = "kontakt@mustermann.de")
    @Email
    private String email;
    @Schema(description = "Telefonnummer", example = "+49 5251 123456")
    private String phone;

    @Schema(description = "ID der zugeordneten Zahlungsbedingung", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long paymentTermId;

    private List<ContactPersonRequestDTO> contactPeople;
    private Boolean isActive;

}
