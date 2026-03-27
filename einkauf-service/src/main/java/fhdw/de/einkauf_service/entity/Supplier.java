package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "supplier")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Supplier {

    public Supplier() {
    }

    public Supplier(Long id, String name, String street, String houseNumber, String zip, String city, String country, String email, String phone, PaymentTerm paymentTerm, Set<ContactPerson> contactPeople, Boolean isActive, Set<Article> articles) {
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
        this.articles = articles;
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

    public PaymentTerm getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(PaymentTerm paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public Set<ContactPerson> getContactPeople() {
        return contactPeople;
    }

    public void setContactPeople(Set<ContactPerson> contactPeople) {
        this.contactPeople = contactPeople;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Supplier name is mandatory.")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Street is mandatory.")
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "House number is mandatory.")
    @Column(nullable = false)
    private String houseNumber;

    @NotBlank(message = "ZIP code is mandatory.")
    @Column(nullable = false)
    private String zip;

    @NotBlank(message = "City is mandatory.")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "Country is mandatory.")
    @Column(nullable = false)
    private String country;

    @Email(message = "Please enter a correct E-Mail.")
    @Size(max = 255, message = "E-Mail is too long.")
    private String email;

    @NotBlank(message = "Phone is mandatory.")
    @Column(nullable = false)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_term_id", nullable = false)
    private PaymentTerm paymentTerm;

    @ManyToMany(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "connector_supplier_cp",
            joinColumns = @JoinColumn(name = "supplier_id"),
            inverseJoinColumns = @JoinColumn(name = "cp_id")
    )
    private Set<ContactPerson> contactPeople = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;  // Standard: aktiv

    @ManyToMany(mappedBy = "suppliers")
    private Set<Article> articles = new HashSet<>();
}
