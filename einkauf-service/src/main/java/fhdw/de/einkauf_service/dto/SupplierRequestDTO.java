package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SupplierRequestDTO {

    @NotBlank
    private String name;

    private String street;
    private String houseNumber;
    private String zip;
    private String city;

    private String country; // ✅ hinzugefügt, für Adressdaten
    @Email
    private String email;
    private String phone; // ✅ hinzugefügt, für Kontaktdaten

    @NotNull
    private Long paymentTermId;

    private List<ContactPersonRequestDTO> contactPeople;

    // ==================================================================================
    // GETTER & SETTER
    // ==================================================================================

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
}
