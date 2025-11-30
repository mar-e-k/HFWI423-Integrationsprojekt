package de.fhdw.commons.api.dto;

public class StoreDTO extends AbstractDTO<Long> {

    private String country;
    private String city;
    private String street;
    private String streetNumber;

    public StoreDTO() {
        super();
    }

    public StoreDTO(Long id) {
        super(id);
    }

    public StoreDTO(String country, String city, String street, String streetNumber) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
    }

    public StoreDTO(Long id, String country, String city, String street, String streetNumber) {
        super(id);
        this.country = country;
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
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
}