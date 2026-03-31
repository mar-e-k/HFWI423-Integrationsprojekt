package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.ContactPersonRequestDTO;
import fhdw.de.einkauf_service.dto.SupplierRequestDTO;
import fhdw.de.einkauf_service.dto.SupplierResponseDTO;
import fhdw.de.einkauf_service.entity.ContactPerson;
import fhdw.de.einkauf_service.entity.PaymentTerm;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.repository.ContactPersonRepository;
import fhdw.de.einkauf_service.repository.PaymentTermRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private PaymentTermRepository paymentTermRepository;

    @Mock
    private ContactPersonRepository contactPersonRepository;

    @InjectMocks
    private SupplierServiceImpl service;

    private PaymentTerm buildPaymentTerm(Long id) {
        return new PaymentTerm(id, "30 Tage netto", "Zahlung innerhalb 30 Tagen");
    }

    private Supplier buildSupplier(Long id, String name, PaymentTerm pt) {
        Supplier s = new Supplier();
        s.setId(id);
        s.setName(name);
        s.setStreet("Musterstraße");
        s.setHouseNumber("1");
        s.setZip("12345");
        s.setCity("Musterstadt");
        s.setCountry("Deutschland");
        s.setEmail("test@test.de");
        s.setPhone("0123456789");
        s.setPaymentTerm(pt);
        s.setActive(true);
        s.setContactPeople(new HashSet<>());
        return s;
    }

    private SupplierRequestDTO buildValidRequest(String name) {
        return new SupplierRequestDTO(name, "Musterstraße", "1", "12345", "Musterstadt",
                "Deutschland", "test@test.de", "0123456789", 1L, null, true);
    }

    // --- createNewSupplier ---

    @Test
    void createNewSupplier_happyPath_savesAndReturnsDTO() {
        PaymentTerm pt = buildPaymentTerm(1L);
        Supplier saved = buildSupplier(1L, "BrewCo", pt);

        when(paymentTermRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(supplierRepository.existsByName("BrewCo")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(saved);

        SupplierResponseDTO result = service.createNewSupplier(buildValidRequest("BrewCo"));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("BrewCo");
    }

    @Test
    void createNewSupplier_paymentTermNotFound_throwsNoSuchElementException() {
        when(paymentTermRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createNewSupplier(buildValidRequest("BrewCo")))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void createNewSupplier_duplicateName_throwsIllegalArgumentException() {
        PaymentTerm pt = buildPaymentTerm(1L);
        when(paymentTermRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(supplierRepository.existsByName("BrewCo")).thenReturn(true);

        assertThatThrownBy(() -> service.createNewSupplier(buildValidRequest("BrewCo")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createNewSupplier_withExistingContactPersonId_loadsFromRepo() {
        PaymentTerm pt = buildPaymentTerm(1L);
        ContactPerson cp = new ContactPerson();
        cp.setId(5L);
        cp.setFirstName("Max");
        cp.setLastName("Mustermann");

        ContactPersonRequestDTO cpRequest = new ContactPersonRequestDTO();
        cpRequest.setId(5L);

        SupplierRequestDTO request = buildValidRequest("BrewCo");
        request.setContactPeople(List.of(cpRequest));

        Supplier saved = buildSupplier(1L, "BrewCo", pt);

        when(paymentTermRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(supplierRepository.existsByName("BrewCo")).thenReturn(false);
        when(contactPersonRepository.findById(5L)).thenReturn(Optional.of(cp));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(saved);

        service.createNewSupplier(request);

        verify(contactPersonRepository).findById(5L);
    }

    @Test
    void createNewSupplier_withNewContactPerson_doesNotQueryRepo() {
        PaymentTerm pt = buildPaymentTerm(1L);
        ContactPersonRequestDTO cpRequest = new ContactPersonRequestDTO();
        cpRequest.setId(null);
        cpRequest.setFirstName("Anna");
        cpRequest.setLastName("Schmidt");
        cpRequest.setEmail("anna@test.de");

        SupplierRequestDTO request = buildValidRequest("BrewCo");
        request.setContactPeople(List.of(cpRequest));

        Supplier saved = buildSupplier(1L, "BrewCo", pt);

        when(paymentTermRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(supplierRepository.existsByName("BrewCo")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(saved);

        service.createNewSupplier(request);

        verify(contactPersonRepository, never()).findById(anyLong());
    }

    // --- findSupplierById ---

    @Test
    void findSupplierById_found_returnsDTO() {
        PaymentTerm pt = buildPaymentTerm(1L);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(buildSupplier(1L, "BrewCo", pt)));

        SupplierResponseDTO result = service.findSupplierById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("BrewCo");
    }

    @Test
    void findSupplierById_notFound_throwsNoSuchElementException() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findSupplierById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- findAllSuppliers ---

    @Test
    void findAllSuppliers_returnsListOfDTOs() {
        PaymentTerm pt = buildPaymentTerm(1L);
        when(supplierRepository.findAll()).thenReturn(List.of(
                buildSupplier(1L, "BrewCo", pt),
                buildSupplier(2L, "FoodCorp", pt)
        ));

        List<SupplierResponseDTO> result = service.findAllSuppliers();

        assertThat(result).hasSize(2);
    }

    // --- updateSupplier ---

    @Test
    void updateSupplier_happyPath_updatesAndReturnsDTO() {
        PaymentTerm pt = buildPaymentTerm(1L);
        Supplier existing = buildSupplier(1L, "AltName", pt);
        Supplier updated = buildSupplier(1L, "NeuName", pt);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(paymentTermRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(supplierRepository.existsByName("NeuName")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(updated);

        SupplierResponseDTO result = service.updateSupplier(1L, buildValidRequest("NeuName"));

        assertThat(result.getName()).isEqualTo("NeuName");
    }

    @Test
    void updateSupplier_sameNameAllowed_doesNotThrow() {
        PaymentTerm pt = buildPaymentTerm(1L);
        Supplier existing = buildSupplier(1L, "BrewCo", pt);
        Supplier updated = buildSupplier(1L, "BrewCo", pt);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(paymentTermRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(updated);

        assertThatCode(() -> service.updateSupplier(1L, buildValidRequest("BrewCo")))
                .doesNotThrowAnyException();

        verify(supplierRepository, never()).existsByName(anyString());
    }

    @Test
    void updateSupplier_differentNameAlreadyExists_throwsIllegalArgumentException() {
        PaymentTerm pt = buildPaymentTerm(1L);
        Supplier existing = buildSupplier(1L, "AltName", pt);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(paymentTermRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(supplierRepository.existsByName("NeuName")).thenReturn(true);

        assertThatThrownBy(() -> service.updateSupplier(1L, buildValidRequest("NeuName")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateSupplier_notFound_throwsNoSuchElementException() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSupplier(99L, buildValidRequest("X")))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- deleteSupplier ---

    @Test
    void deleteSupplier_found_callsDeleteById() {
        when(supplierRepository.existsById(1L)).thenReturn(true);

        service.deleteSupplier(1L);

        verify(supplierRepository).deleteById(1L);
    }

    @Test
    void deleteSupplier_notFound_throwsNoSuchElementException() {
        when(supplierRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteSupplier(99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
