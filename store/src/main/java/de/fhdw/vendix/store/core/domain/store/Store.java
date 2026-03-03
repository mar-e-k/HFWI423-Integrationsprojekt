package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.register.Register;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStock;
import de.fhdw.vendix.store.core.domain.store_system.StoreSystem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Store extends AbstractSpringDataAuditingEntity<Long> {

    @OneToMany(mappedBy = "store")
    private List<Register> registers = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<Receipt> receipts = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<StoreStock> storeStocks = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<StoreSystem> storeSystems = new ArrayList<>();

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
    @Pattern(regexp = "^[0-9]\\d*[A-Z]?$", message = "Street number must include a number in the beginning")
    private String streetNumber;

    protected Store() {}

    public Store(String country, String city, String street, String streetNumber) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
    }

    public Store(List<Register> registers, List<Receipt> receipts, List<StoreStock> storeStocks, List<StoreSystem> storeSystems, String country, String city, String street, String streetNumber) {
        this.registers = registers;
        this.receipts = receipts;
        this.storeStocks = storeStocks;
        this.storeSystems = storeSystems;
        this.country = country;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
    }

    public List<Register> getRegisters() {
        return registers;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public List<StoreStock> getStoreStocks() {
        return storeStocks;
    }

    public List<StoreSystem> getStoreSystems() {
        return storeSystems;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }
}