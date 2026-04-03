package de.fhdw.vendix.store.api.controller;

import de.fhdw.vendix.commons.security.auth.AuthContext;
import de.fhdw.vendix.commons.security.jwt.JwtService;
import de.fhdw.vendix.commons.security.jwt.claims.AuthClaims;
import de.fhdw.vendix.commons.security.jwt.claims.ContextClaims;
import de.fhdw.vendix.commons.security.jwt.claims.JwtPayload;
import de.fhdw.vendix.store.utility.StoreClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Token-Endpunkt fuer JMeter")
public class AuthTokenController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final StoreClient storeClient;

    public AuthTokenController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            StoreClient storeClient) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.storeClient = storeClient;
    }

    @PostMapping("/token")
    @Operation(summary = "JWT fuer externen Client ausstellen")
    public ResponseEntity<Map<String, String>> issueToken(
            @RequestBody TokenRequest request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()));

        AuthContext ctx = (AuthContext) auth.getPrincipal();
        Long storeId = (storeClient.getStore() != null)
                ? storeClient.getStore().getId() : null;

        JwtPayload payload = new JwtPayload(
                AuthClaims.user(ctx.getUuid(), ctx.getRoles()),
                ContextClaims.context(storeId, request.registerId()));

        return ResponseEntity.ok(Map.of(
                "access_token", jwtService.generateToken(payload),
                "token_type",   "Bearer"));
    }

    public record TokenRequest(
            String username,
            String password,
            Long registerId) {}
}