package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.register.Register;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.*;

@Entity
public class Store extends AbstractSpringDataAuditingEntity<Long> {

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

    @OneToMany(mappedBy = "store")
    private Set<Register> registers = new HashSet<>();

    @OneToMany(mappedBy = "store")
    private Set<Receipt> receipts = new HashSet<>();

    @OneToMany(mappedBy = "store")
    private Set<StoreStock> stocks = new HashSet<>();

    protected Store() {}

    protected Store(String country, String city, String street, String streetNumber) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
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

    public Set<Register> getRegisters() {
        return Collections.unmodifiableSet(registers);
    }

    public Set<Receipt> getReceipts() {
        return Collections.unmodifiableSet(receipts);
    }

    public Set<StoreStock> getStocks() {
        return Collections.unmodifiableSet(stocks);
    }
}