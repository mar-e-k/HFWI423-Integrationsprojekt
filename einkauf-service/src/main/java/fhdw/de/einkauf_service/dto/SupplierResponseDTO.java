package fhdw.de.einkauf_service.dto;

import java.util.List;

public class SupplierResponseDTO {
    public SupplierResponseDTO(Long id, String name, String street, String houseNumber, String zip, String city, String country, String email, String phone, PaymentTermResponseDTO paymentTerm, List<ContactPersonResponseDTO> contactPeople, Boolean isActive, Long paymentTermId, String paymentTermDefinition, String paymentTermDescription) {
        this.id = id;
        this.name = name;
        this.street = street;
        this.houseNumber = houseNumber;
        this.zip = zip;
        this.city = city;
        this.country = country;
        this.email = email;
        this.phone = phone;
        this.paymentTerm = paymentTerm;
        this.contactPeople = contactPeople;
        this.isActive = isActive;
        this.paymentTermId = paymentTermId;
        this.paymentTermDefinition = paymentTermDefinition;
        this.paymentTermDescription = paymentTermDescription;
    }

    public SupplierResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public PaymentTermResponseDTO getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(PaymentTermResponseDTO paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public List<ContactPersonResponseDTO> getContactPeople() {
        return contactPeople;
    }

    public void setContactPeople(List<ContactPersonResponseDTO> contactPeople) {
        this.contactPeople = contactPeople;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Long getPaymentTermId() {
        return paymentTermId;
    }

    public void setPaymentTermId(Long paymentTermId) {
        this.paymentTermId = paymentTermId;
    }

    public String getPaymentTermDefinition() {
        return paymentTermDefinition;
    }

    public void setPaymentTermDefinition(String paymentTermDefinition) {
        this.paymentTermDefinition = paymentTermDefinition;
    }

    public String getPaymentTermDescription() {
        return paymentTermDescription;
    }

    public void setPaymentTermDescription(String paymentTermDescription) {
        this.paymentTermDescription = paymentTermDescription;
    }

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
    private Boolean isActive;

    private Long paymentTermId;
    private String paymentTermDefinition;
    private String paymentTermDescription;
}
