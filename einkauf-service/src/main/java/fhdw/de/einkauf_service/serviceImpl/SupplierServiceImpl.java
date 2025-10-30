package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.*;
import fhdw.de.einkauf_service.entity.*;
import fhdw.de.einkauf_service.repository.PaymentTermRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import fhdw.de.einkauf_service.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final PaymentTermRepository paymentTermRepository;

    public SupplierServiceImpl(SupplierRepository supplierRepository,
                               PaymentTermRepository paymentTermRepository) {
        this.supplierRepository = supplierRepository;
        this.paymentTermRepository = paymentTermRepository;
    }

    // ==================================================================================
    // 1. CREATE Supplier inkl. ContactPersons
    // ==================================================================================
    @Transactional
    @Override
    public SupplierResponseDTO createNewSupplier(SupplierRequestDTO requestDTO) {
        PaymentTerm paymentTerm = paymentTermRepository.findById(requestDTO.getPaymentTermId())
                .orElseThrow(() -> new NoSuchElementException(
                        "PaymentTerm with ID " + requestDTO.getPaymentTermId() + " not found."));

        Supplier newSupplier = toEntity(requestDTO);
        newSupplier.setPaymentTerm(paymentTerm);

        if (supplierRepository.existsByName(newSupplier.getName())) {
            throw new IllegalArgumentException("Ein Lieferant mit dem Namen '" + newSupplier.getName() + "' existiert bereits.");
        }

        Supplier savedSupplier = supplierRepository.save(newSupplier);
        return toResponseDTO(savedSupplier);
    }

    // ==================================================================================
    // 2. READ Supplier nach ID
    // ==================================================================================
    @Override
    public SupplierResponseDTO findSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Supplier with ID " + id + " not found."));
        return toResponseDTO(supplier);
    }

    // ==================================================================================
    // 3. READ ALL Suppliers
    // ==================================================================================
    @Override
    public List<SupplierResponseDTO> findAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================================================================================
    // 4. UPDATE Supplier inkl. ContactPersons
    // ==================================================================================
    @Transactional
    @Override
    public SupplierResponseDTO updateSupplier(Long id, SupplierRequestDTO updatedSupplierRequestDTO) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Supplier with ID " + id + " not found."));

        PaymentTerm paymentTerm = paymentTermRepository.findById(updatedSupplierRequestDTO.getPaymentTermId())
                .orElseThrow(() -> new NoSuchElementException(
                        "PaymentTerm with ID " + updatedSupplierRequestDTO.getPaymentTermId() + " not found."));

        if (!existingSupplier.getName().equals(updatedSupplierRequestDTO.getName())
                && supplierRepository.existsByName(updatedSupplierRequestDTO.getName())) {
            throw new IllegalArgumentException("Supplier name '" + updatedSupplierRequestDTO.getName() + "' already exists.");
        }

        existingSupplier.setName(updatedSupplierRequestDTO.getName());
        existingSupplier.setStreet(updatedSupplierRequestDTO.getStreet());
        existingSupplier.setHouseNumber(updatedSupplierRequestDTO.getHouseNumber());
        existingSupplier.setZip(updatedSupplierRequestDTO.getZip());
        existingSupplier.setCity(updatedSupplierRequestDTO.getCity());
        existingSupplier.setEmail(updatedSupplierRequestDTO.getEmail());
        existingSupplier.setPhone(updatedSupplierRequestDTO.getPhone());
        existingSupplier.setPaymentTerm(paymentTerm);

        // ContactPersons aktualisieren
        existingSupplier.getContactPeople().clear();
        if (updatedSupplierRequestDTO.getContactPeople() != null) {
            Set<ContactPerson> newContactPeople = updatedSupplierRequestDTO.getContactPeople().stream()
                    .map(this::toContactPersonEntity)
                    .collect(Collectors.toSet());
            existingSupplier.getContactPeople().addAll(newContactPeople);
        }

        Supplier savedSupplier = supplierRepository.save(existingSupplier);
        return toResponseDTO(savedSupplier);
    }

    // ==================================================================================
    // 5. DELETE Supplier inkl. ContactPersons
    // ==================================================================================
    @Transactional
    @Override
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new NoSuchElementException("Supplier with ID " + id + " not found.");
        }
        supplierRepository.deleteById(id);
    }

    // ==================================================================================
    // PRIVATE MAPPING METHODS
    // ==================================================================================
    private Supplier toEntity(SupplierRequestDTO dto) {
        Supplier entity = new Supplier();
        entity.setName(dto.getName());
        entity.setStreet(dto.getStreet());
        entity.setHouseNumber(dto.getHouseNumber());
        entity.setZip(dto.getZip());
        entity.setCity(dto.getCity());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());

        if (dto.getContactPeople() != null) {
            Set<ContactPerson> contactPeople = dto.getContactPeople().stream()
                    .map(this::toContactPersonEntity)
                    .collect(Collectors.toSet());
            entity.setContactPeople(contactPeople);
        } else {
            entity.setContactPeople(new HashSet<>());
        }
        return entity;
    }

    private ContactPerson toContactPersonEntity(ContactPersonRequestDTO dto) {
        ContactPerson entity = new ContactPerson();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setRole(dto.getRole());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        return entity;
    }

    private SupplierResponseDTO toResponseDTO(Supplier entity) {
        SupplierResponseDTO dto = new SupplierResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStreet(entity.getStreet());
        dto.setHouseNumber(entity.getHouseNumber());
        dto.setZip(entity.getZip());
        dto.setCity(entity.getCity());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());

        // PaymentTerm als DTO setzen
        if (entity.getPaymentTerm() != null) {
            PaymentTermResponseDTO ptDto = new PaymentTermResponseDTO();
            ptDto.setId(entity.getPaymentTerm().getId());
            ptDto.setDefinition(entity.getPaymentTerm().getDefinition());
            ptDto.setDescription(entity.getPaymentTerm().getDescription());
            dto.setPaymentTerm(ptDto);
        }

        // ContactPeople setzen
        if (entity.getContactPeople() != null) {
            dto.setContactPeople(entity.getContactPeople().stream()
                    .map(this::toContactPersonResponseDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private ContactPersonResponseDTO toContactPersonResponseDTO(ContactPerson entity) {
        ContactPersonResponseDTO dto = new ContactPersonResponseDTO();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setRole(entity.getRole());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        return dto;
    }
}
