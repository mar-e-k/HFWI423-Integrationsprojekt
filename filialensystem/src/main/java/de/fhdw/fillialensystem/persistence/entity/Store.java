package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Store extends AbstractEntity {

    @OneToMany(mappedBy = "store")
    private List<Register> registers = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<Receipt> receipts = new ArrayList<>();

    @Column(nullable = false)
    @NotBlank(message = "Country must not be blank")
    private String country;

    @Column(nullable = false)
    @NotBlank(message = "City must not be blank")
    private String city;

    @Column(nullable = false)
    @NotBlank(message = "Street must not be blank")
    private String street;

    @Column(nullable = false)
    @NotNull(message = "Street number cannot be null")
    @Min(value = 0, message = "Street number must be greater than 0")
    private Short streetNumber;

    public Store() {
        super();
    }

    public Store(List<Register> registers, List<Receipt> receipts, String country, String city, String street, Short streetNumber) {
        this.registers = registers;
        this.receipts = receipts;
        this.country = country;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
    }

    public List<Register> getRegisters() {
        return registers;
    }

    public void setRegisters(List<Register> registers) {
        this.registers = registers;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Short getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(Short streetNumber) {
        this.streetNumber = streetNumber;
    }
}