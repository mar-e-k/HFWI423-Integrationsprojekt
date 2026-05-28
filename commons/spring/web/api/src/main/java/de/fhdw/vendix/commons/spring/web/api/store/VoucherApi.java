package de.fhdw.vendix.commons.spring.web.api.store;

import de.fhdw.vendix.commons.api.domain.voucher.VoucherDTO;
import de.fhdw.vendix.commons.spring.web.api.scheme.KeycloakOpenApiScheme;
import de.fhdw.vendix.commons.spring.web.api.scheme.StoreRoutingOpenApiScheme;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

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
            summary = "Get a voucher by code",
            description = "Retrieves a single voucher using its unique code."
    )
    @GetExchange("/{code}")
    ResponseEntity<VoucherDTO> getVoucherByCode(@PathVariable UUID code);

    @Operation(
            summary = "Redeem a voucher",
            description = "Marks a voucher as redeemed if it is valid and not expired or already redeemed."
    )
    @PostExchange("/{code}/redeem")
    ResponseEntity<VoucherDTO> redeemVoucherByCode(@PathVariable UUID code);
}