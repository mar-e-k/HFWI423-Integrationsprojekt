package de.fhdw.vendix.commons.spring.web.api.store;

import de.fhdw.vendix.commons.api.domain.voucher.VoucherRequestDTO;
import de.fhdw.vendix.commons.api.domain.voucher.VoucherResponseDTO;
import de.fhdw.vendix.commons.spring.web.api.scheme.KeycloakOpenApiScheme;
import de.fhdw.vendix.commons.spring.web.api.scheme.StoreRoutingOpenApiScheme;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;
import java.util.UUID;

@HttpExchange("/api/voucher")
@Tag(
        name = "Voucher",
        description = "Operations related to vouchers."
)
@KeycloakOpenApiScheme
@StoreRoutingOpenApiScheme
public interface VoucherApi {

    @Operation(
            summary = "[DNT] Get a List of all Vouchers",
            description = "[DNT] Get a List of all Vouchers"
    )
    @GetExchange
    ResponseEntity<List<VoucherResponseDTO>> getVouchers();

    @Operation(
            summary = "Get a voucher by code",
            description = "Retrieves a single voucher using its unique code."
    )
    @GetExchange("/{voucherCode}")
    ResponseEntity<VoucherResponseDTO> getVoucherByCode(@PathVariable UUID voucherCode);

    @Operation(
            summary = "[DNT] Create Voucher",
            description = "[DNT] TODO"
    )
    @PostExchange
    ResponseEntity<VoucherResponseDTO> createVoucher(@RequestBody VoucherRequestDTO requestDTO);

    @Operation(
            summary = "Redeem a voucher",
            description = "Marks a voucher as redeemed if it is valid and not expired or already redeemed."
    )
    @PutExchange("/{voucherCode}/redeem")
    ResponseEntity<VoucherResponseDTO> redeemVoucherByCode(@PathVariable UUID voucherCode);
}