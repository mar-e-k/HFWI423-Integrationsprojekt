package fhdw.de.einkauf_service.dto;

import fhdw.de.einkauf_service.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDate;

public class OrderFilterDTO {
    public String getOrderNumber() {
        return orderNumber;
    }

    public OrderFilterDTO(String orderNumber, Long supplierId, LocalDate orderDateFrom, LocalDate orderDateTo, OrderStatus status, Long articleId) {
        this.orderNumber = orderNumber;
        this.supplierId = supplierId;
        this.orderDateFrom = orderDateFrom;
        this.orderDateTo = orderDateTo;
        this.status = status;
        this.articleId = articleId;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public LocalDate getOrderDateFrom() {
        return orderDateFrom;
    }

    public void setOrderDateFrom(LocalDate orderDateFrom) {
        this.orderDateFrom = orderDateFrom;
    }

    public LocalDate getOrderDateTo() {
        return orderDateTo;
    }

    public void setOrderDateTo(LocalDate orderDateTo) {
        this.orderDateTo = orderDateTo;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    private String orderNumber;

    private Long supplierId;

    private LocalDate orderDateFrom;

    private LocalDate orderDateTo;

    private OrderStatus status;

    private Long articleId;
}
