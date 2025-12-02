package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Store extends AbstractEntity {

    @OneToMany(mappedBy = "store")
    private List<Register> registers = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<Receipt> receipts = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<StoreLinkStock> storeStocks = new ArrayList<>();

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
    @NotBlank(message = "Street number must not be blank")
    @Pattern(regexp = "^[1-9]\\d*[A-Z]?$", message = "Street number must include a number in the beginning")
    private String streetNumber;

    public Store() {
        super();
    }

    public Store(Long id) {
        super(id);
    }

    public Store(List<Register> registers, List<Receipt> receipts, List<StoreLinkStock> storeStocks, String country, String city, String street, String streetNumber) {
        this.registers = registers;
        this.receipts = receipts;
        this.storeStocks = storeStocks;
        this.country = country;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
    }

    public Store(Long id, List<Register> registers, List<Receipt> receipts, List<StoreLinkStock> storeStocks, String country, String city, String street, String streetNumber) {
        super(id);
        this.registers = registers;
        this.receipts = receipts;
        this.storeStocks = storeStocks;
        this.country = country;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
    }

    public List<StoreLinkStock> getStoreStocks() {
        return storeStocks;
    }

    public void setStoreStocks(List<StoreLinkStock> storeStocks) {
        this.storeStocks = storeStocks;
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

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return Objects.equals(getId(), ((Store) o).getId()) && Objects.equals(country, store.country) && Objects.equals(city, store.city) && Objects.equals(street, store.street) && Objects.equals(streetNumber, store.streetNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registers, receipts, storeStocks, country, city, street, streetNumber);
    }
}
