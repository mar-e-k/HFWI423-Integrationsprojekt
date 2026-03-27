package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

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
