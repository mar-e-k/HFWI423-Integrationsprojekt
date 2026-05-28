package de.fhdw.vendix.orchestrator.core.domain.store;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.persistance.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.Nullable;

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

    protected Store() {}

    protected Store(String country, String city, String street, String streetNumber) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
    }

    @Default
    protected Store(@Nullable Long id, String country, String city, String street, String streetNumber) {
        super(id);
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
}