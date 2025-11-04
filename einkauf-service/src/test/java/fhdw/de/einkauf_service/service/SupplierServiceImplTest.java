package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.ContactPersonRequestDTO;
import fhdw.de.einkauf_service.dto.SupplierRequestDTO;
import fhdw.de.einkauf_service.entity.PaymentTerm;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.repository.PaymentTermRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import fhdw.de.einkauf_service.serviceImpl.SupplierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private PaymentTermRepository paymentTermRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private SupplierRequestDTO requestDTO;
    private Supplier savedSupplier;
    private PaymentTerm paymentTerm;

    @BeforeEach
    void setUp() {
        ContactPersonRequestDTO contact = new ContactPersonRequestDTO();
        contact.setFirstName("Max");
        contact.setLastName("Mustermann");
        contact.setRole("Einkaufsleiter");
        contact.setPhone("+491231234567");
        contact.setEmail("max@test.de");

        requestDTO = new SupplierRequestDTO();
        requestDTO.setName("LieferantTest");
        requestDTO.setStreet("Musterstraße");
        requestDTO.setHouseNumber("1A");
        requestDTO.setZip("12345");
        requestDTO.setCity("Teststadt");
        requestDTO.setEmail("supplier@test.de");
        requestDTO.setPaymentTermId(13L);
        requestDTO.setContactPeople(List.of(contact));

        paymentTerm = new PaymentTerm();
        paymentTerm.setId(13L);
        paymentTerm.setDefinition("Netto 30");

        savedSupplier = new Supplier();
        savedSupplier.setId(1L);
        savedSupplier.setName(requestDTO.getName());
        savedSupplier.setPaymentTerm(paymentTerm);
    }

    @Test
    void shouldCreateSupplierSuccessfully() {
        when(paymentTermRepository.findById(anyLong())).thenReturn(Optional.of(paymentTerm));
        when(supplierRepository.existsByName(anyString())).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(savedSupplier);

        var response = supplierService.createNewSupplier(requestDTO);

        assertEquals("LieferantTest", response.getName());
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void shouldThrowExceptionWhenPaymentTermNotFound() {
        when(paymentTermRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> supplierService.createNewSupplier(requestDTO));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenSupplierNameExists() {
        when(paymentTermRepository.findById(anyLong())).thenReturn(Optional.of(paymentTerm));
        when(supplierRepository.existsByName("LieferantTest")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> supplierService.createNewSupplier(requestDTO));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void shouldFindSupplierById() {
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(savedSupplier));

        var response = supplierService.findSupplierById(1L);
        assertEquals(1L, response.getId());
    }

    @Test
    void shouldThrowExceptionWhenFindingNonExistingSupplier() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> supplierService.findSupplierById(99L));
    }

    @Test
    void shouldUpdateSupplierSuccessfully() {
        // Die PaymentTerm-ID, die im requestDTO gesetzt ist
        Long paymentTermId = requestDTO.getPaymentTermId();

        // Mock für SupplierRepository
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(savedSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(savedSupplier);

        // Mock für PaymentTermRepository für die konkrete ID
        when(paymentTermRepository.findById(paymentTermId)).thenReturn(Optional.of(paymentTerm));

        // Aufruf der Update-Methode
        var response = supplierService.updateSupplier(1L, requestDTO);

        // Assertions
        assertEquals("LieferantTest", response.getName());
        assertEquals(paymentTermId, response.getPaymentTerm().getId());

        // Verify, dass save aufgerufen wurde
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void shouldDeleteSupplierSuccessfully() {
        when(supplierRepository.existsById(anyLong())).thenReturn(true);
        supplierService.deleteSupplier(1L);
        verify(supplierRepository).deleteById(anyLong());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingSupplier() {
        when(supplierRepository.existsById(anyLong())).thenReturn(false);
        assertThrows(NoSuchElementException.class, () -> supplierService.deleteSupplier(99L));
    }
}
