package fhdw.de.einkauf_service.entity;

import fhdw.de.einkauf_service.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_order")
@SequenceGenerator(name = "orderNumberGenerator", sequenceName = "order_number_seq", allocationSize = 1)
public class Order {

    public Order(Long id, String orderNumber, Supplier supplier, LocalDateTime orderDate, LocalDate expectedDeliveryDate, OrderStatus status, Double totalAmount) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.supplier = supplier;
        this.orderDate = orderDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public Order() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column
    private LocalDate expectedDeliveryDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PLACED;

    private Double totalAmount;
}
