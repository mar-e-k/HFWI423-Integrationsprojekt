package fhdw.de.einkauf_service.dto;

import fhdw.de.einkauf_service.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderFilterDTO {

    private String orderNumber;

    private Long supplierId;

    private LocalDate orderDateFrom;

    private LocalDate orderDateTo;

    private OrderStatus status;

    private Long articleId;
}
