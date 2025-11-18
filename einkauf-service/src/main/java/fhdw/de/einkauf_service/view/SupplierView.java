package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import fhdw.de.einkauf_service.dto.*;
import fhdw.de.einkauf_service.service.SupplierService;
import fhdw.de.einkauf_service.repository.PaymentTermRepository;
import fhdw.de.einkauf_service.repository.ContactPersonRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Lieferantenansicht in Vaadin.
 * Bietet CRUD-Funktionalität für Lieferanten mit:
 * • Grid-Ansicht mit Filterung nach Name und Stadt
 * • Detailansicht mit allen Lieferanteninformationen
 * • Formular zum Erstellen/Bearbeiten mit PaymentTerm-Auswahl
 * • Verwaltung von 1-3 Kontaktpersonen pro Lieferant
 */
@Route(value = "suppliers", layout = MainLayout.class)
public class SupplierView extends VerticalLayout {

    private final SupplierService supplierService;
    private final PaymentTermRepository paymentTermRepository;
    private final ContactPersonRepository contactPersonRepository;

    private final Grid<SupplierResponseDTO> grid = new Grid<>(SupplierResponseDTO.class);

    // Search fields
    private final TextField nameField = createSearchField("Lieferantenname");
    private final TextField cityField = createSearchField("Stadt");
    private final Button clearButton = new Button("Suche abbrechen");

    // CRUD buttons
    private final Button addButton = new Button("Lieferant hinzufügen");
    private final Button editButton = new Button("Bearbeiten");
    private final Button deleteButton = new Button("Löschen");

    public SupplierView(SupplierService supplierService,
                        PaymentTermRepository paymentTermRepository,
                        ContactPersonRepository contactPersonRepository) {
        this.supplierService = supplierService;
        this.paymentTermRepository = paymentTermRepository;
        this.contactPersonRepository = contactPersonRepository;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        add(new H2("Lieferantenverwaltung"));

        configureGrid();
        configureCrudButtons();
        configureSearchFields();


        HorizontalLayout searchLayout = new HorizontalLayout(
                nameField, cityField, clearButton
        );
        searchLayout.setAlignItems(Alignment.END);

        HorizontalLayout crudButtons = new HorizontalLayout(addButton, editButton, deleteButton);

        add(crudButtons, searchLayout, grid);

        updateList();
    }

    private static TextField createSearchField(String label) {
        TextField tf = new TextField(label);
        tf.setClearButtonVisible(true);
        tf.setValueChangeMode(ValueChangeMode.EAGER);
        return tf;
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.setColumns();
        grid.addColumn(SupplierResponseDTO::getName).setHeader("Lieferantenname").setAutoWidth(true).setSortable(true);
        grid.addColumn(SupplierResponseDTO::getCity).setHeader("Stadt").setAutoWidth(true).setSortable(true);
        grid.addColumn(SupplierResponseDTO::getPhone).setHeader("Telefon").setAutoWidth(true).setSortable(true);
        grid.addColumn(SupplierResponseDTO::getEmail).setHeader("E-Mail").setAutoWidth(true).setSortable(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            SupplierResponseDTO selected = event.getValue();
            if (selected != null) {
                showSupplierDetails(selected);
            }
        });
    }

    private void configureSearchFields() {
        nameField.addValueChangeListener(e -> updateList());
        cityField.addValueChangeListener(e -> updateList());
        clearButton.addClickListener(e -> {
            nameField.clear();
            cityField.clear();
            updateList();
        });
    }

    private void configureCrudButtons() {
        addButton.addClickListener(e -> openSupplierForm(null));

        editButton.addClickListener(e -> {
            SupplierResponseDTO selected = grid.asSingleSelect().getValue();
            if (selected != null) {
                openSupplierForm(selected);
            }
        });

        deleteButton.addClickListener(e -> {
            SupplierResponseDTO selected = grid.asSingleSelect().getValue();
            if (selected != null) {
                showDeleteConfirmationDialog(selected);
            }
        });

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        grid.asSingleSelect().addValueChangeListener(event -> {
            boolean hasSelection = event.getValue() != null;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
    }

    private void updateList() {
        String nameFilter = nameField.getValue();
        String cityFilter = cityField.getValue();

        List<SupplierResponseDTO> suppliers = supplierService.findAllSuppliers();

        // Client-side filtering
        if (nameFilter != null && !nameFilter.isEmpty()) {
            suppliers = suppliers.stream()
                    .filter(s -> s.getName() != null &&
                            s.getName().toLowerCase().contains(nameFilter.toLowerCase()))
                    .toList();
        }

        if (cityFilter != null && !cityFilter.isEmpty()) {
            suppliers = suppliers.stream()
                    .filter(s -> s.getCity() != null &&
                            s.getCity().toLowerCase().contains(cityFilter.toLowerCase()))
                    .toList();
        }

        grid.setItems(suppliers);
    }

    private void openSupplierForm(SupplierResponseDTO supplier) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeaderTitle(supplier == null ? "Neuen Lieferanten hinzufügen" : "Lieferant bearbeiten");

        // Basic supplier fields
        TextField name = new TextField("Lieferantenname");
        name.setWidthFull();
        name.setRequired(true);

        TextField street = new TextField("Straße");
        street.setRequired(true);

        TextField houseNumber = new TextField("Hausnummer");
        houseNumber.setRequired(true);
        HorizontalLayout addressLine1 = new HorizontalLayout(street, houseNumber);
        addressLine1.setWidthFull();
        street.setWidth("70%");
        houseNumber.setWidth("30%");

        TextField zip = new TextField("PLZ");
        TextField city = new TextField("Stadt");
        HorizontalLayout addressLine2 = new HorizontalLayout(zip, city);
        addressLine2.setWidthFull();
        zip.setWidth("30%");
        city.setWidth("70%");
        zip.setRequired(true);
        city.setRequired(true);


        TextField country = new TextField("Land");
        country.setWidthFull();
        country.setRequired(true);


        EmailField email = new EmailField("E-Mail");
        email.setWidthFull();
        email.setRequired(true);


        TextField phone = new TextField("Telefon");
        phone.setWidthFull();
        phone.setRequired(true);

        // Payment term selection
        ComboBox<PaymentTermResponseDTO> paymentTermBox = new ComboBox<>("Zahlungsbedingungen");
        paymentTermBox.setWidthFull();
        paymentTermBox.setRequired(true);

        // Load payment terms from repository
        List<PaymentTermResponseDTO> paymentTerms = paymentTermRepository.findAll().stream()
                .map(pt -> {
                    PaymentTermResponseDTO dto = new PaymentTermResponseDTO();
                    dto.setId(pt.getId());
                    dto.setDefinition(pt.getDefinition());
                    dto.setDescription(pt.getDescription());
                    return dto;
                })
                .toList();

        paymentTermBox.setItems(paymentTerms);
        paymentTermBox.setItemLabelGenerator(pt -> pt.getDefinition() +
                (pt.getDescription() != null ? " (" + pt.getDescription() + ")" : ""));

        // Contact persons section
        VerticalLayout contactPersonsLayout = new VerticalLayout();
        contactPersonsLayout.setPadding(false);
        contactPersonsLayout.setSpacing(true);

        List<ContactPersonForm> contactForms = new ArrayList<>();

        // Button to add contact person forms
        Button addContactButton = new Button("Kontaktperson hinzufügen");
        addContactButton.addClickListener(e -> {
            if (contactForms.size() < 3) {
                ContactPersonForm cpForm = new ContactPersonForm(contactPersonRepository);
                contactForms.add(cpForm);

                // Add remove button for this contact form
                Button removeButton = new Button("Entfernen", ev -> {
                    contactForms.remove(cpForm);
                    contactPersonsLayout.remove(cpForm);
                    if (contactForms.size() < 3) {
                        addContactButton.setEnabled(true);
                    }
                });
                cpForm.add(removeButton);

                contactPersonsLayout.addComponentAtIndex(contactPersonsLayout.getComponentCount() - 1, cpForm);

                if (contactForms.size() >= 3) {
                    addContactButton.setEnabled(false);
                }
            }
        });

        contactPersonsLayout.add(new H3("Kontaktpersonen (optional)"));
        contactPersonsLayout.add(addContactButton);

        // Prefill for editing
        if (supplier != null) {
            name.setValue(safe(supplier.getName()));
            street.setValue(safe(supplier.getStreet()));
            houseNumber.setValue(safe(supplier.getHouseNumber()));
            zip.setValue(safe(supplier.getZip()));
            city.setValue(safe(supplier.getCity()));
            country.setValue(safe(supplier.getCountry()));
            email.setValue(safe(supplier.getEmail()));
            phone.setValue(safe(supplier.getPhone()));

            if (supplier.getPaymentTerm() != null) {
                paymentTermBox.setValue(supplier.getPaymentTerm());
            }

            // Load existing contact persons
            if (supplier.getContactPeople() != null && !supplier.getContactPeople().isEmpty()) {
                for (ContactPersonResponseDTO cp : supplier.getContactPeople()) {
                    if (contactForms.size() < 3) {
                        ContactPersonForm cpForm = new ContactPersonForm(contactPersonRepository);
                        cpForm.setContactPerson(cp);
                        contactForms.add(cpForm);

                        Button removeButton = new Button("Entfernen", ev -> {
                            contactForms.remove(cpForm);
                            contactPersonsLayout.remove(cpForm);
                            if (contactForms.size() < 3) {
                                addContactButton.setEnabled(true);
                            }
                        });
                        cpForm.add(removeButton);

                        contactPersonsLayout.addComponentAtIndex(contactPersonsLayout.getComponentCount() - 1, cpForm);
                    }
                }
                if (contactForms.size() >= 3) {
                    addContactButton.setEnabled(false);
                }
            }
        }

        // Save button
        Button saveButton = new Button("Speichern", event -> {
            try {
                // Validation
                if (name.isEmpty() || street.isEmpty() || houseNumber.isEmpty() || zip.isEmpty() || city.isEmpty()
                        || country.isEmpty() || email.isEmpty() || phone.isEmpty() || paymentTermBox.isEmpty()) {
                    throw new IllegalArgumentException("Alle Pflichtfelder müssen ausgefüllt werden.");
                }

                SupplierRequestDTO req = new SupplierRequestDTO();
                req.setName(name.getValue());
                req.setStreet(street.getValue());
                req.setHouseNumber(houseNumber.getValue());
                req.setZip(zip.getValue());
                req.setCity(city.getValue());
                req.setCountry(country.getValue());
                req.setEmail(email.getValue());
                req.setPhone(phone.getValue());
                req.setPaymentTermId(paymentTermBox.getValue().getId());

                // Collect contact persons
                List<ContactPersonRequestDTO> contactPersonRequests = new ArrayList<>();
                for (ContactPersonForm cpForm : contactForms) {
                    ContactPersonRequestDTO cpReq = cpForm.getContactPersonRequest();
                    if (cpReq != null) {
                        contactPersonRequests.add(cpReq);
                    }
                }
                req.setContactPeople(contactPersonRequests);

                if (supplier == null) {
                    supplierService.createNewSupplier(req);
                    showSuccessNotification("Lieferant wurde erfolgreich gespeichert!");
                } else {
                    supplierService.updateSupplier(supplier.getId(), req);
                    showSuccessNotification("Lieferant wurde erfolgreich aktualisiert!");
                }

                dialog.close();
                updateList();
            } catch (Exception ex) {
                ex.printStackTrace();
                Span errorMsg = new Span("Fehler: " + ex.getMessage());
                errorMsg.getStyle().set("color", "red");
                dialog.add(errorMsg);
            }
        });

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        VerticalLayout formLayout = new VerticalLayout(
                name, addressLine1, addressLine2, country, email, phone,
                paymentTermBox, contactPersonsLayout, buttons
        );
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        dialog.add(formLayout);
        dialog.open();
    }

    private void showSupplierDetails(SupplierResponseDTO supplier) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setHeaderTitle("Lieferantendetails");

        VerticalLayout detailsLayout = new VerticalLayout(
                new Span("Lieferantenname: " + safe(supplier.getName())),
                new Span("Adresse: " + safe(supplier.getStreet()) + " " + safe(supplier.getHouseNumber())),
                new Span("PLZ / Stadt: " + safe(supplier.getZip()) + " " + safe(supplier.getCity())),
                new Span("Land: " + safe(supplier.getCountry())),
                new Span("E-Mail: " + safe(supplier.getEmail())),
                new Span("Telefon: " + safe(supplier.getPhone())),
                new Span("Zahlungsbedingungen: " +
                        (supplier.getPaymentTerm() != null ? supplier.getPaymentTerm().getDefinition() : "-"))
        );

        // Show contact persons
        if (supplier.getContactPeople() != null && !supplier.getContactPeople().isEmpty()) {
            System.out.println("Contact persons gefunden ");
            detailsLayout.add(new H3("Kontaktpersonen:"));
            for (ContactPersonResponseDTO cp : supplier.getContactPeople()) {
                VerticalLayout cpLayout = new VerticalLayout(
                        new Span("Name: " + safe(cp.getFirstName()) + " " + safe(cp.getLastName())),
                        new Span("Position: " + safe(cp.getRole())),
                        new Span("Telefon: " + safe(cp.getPhone())),
                        new Span("E-Mail: " + safe(cp.getEmail()))
                );
                cpLayout.setPadding(false);
                cpLayout.getStyle().set("margin-left", "20px");
                cpLayout.getStyle().set("border-left", "3px solid #ccc");
                cpLayout.getStyle().set("padding-left", "10px");
                detailsLayout.add(cpLayout);
            }
        }

        dialog.add(detailsLayout);
        dialog.getFooter().add(new Button("Schließen", e -> dialog.close()));
        dialog.open();
    }

    private String safe(Object value) {
        return value == null || value.toString().isEmpty() ? "-" : value.toString();
    }

    /**
     * Shows a confirmation dialog before deleting a supplier
     */
    private void showDeleteConfirmationDialog(SupplierResponseDTO supplier) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Löschen bestätigen");

        VerticalLayout content = new VerticalLayout(
                new Span("Möchten Sie den Lieferanten \"" + supplier.getName() + "\" wirklich endgültig löschen?"),
                new Span("Diese Aktion kann nicht rückgängig gemacht werden.")
        );
        content.setPadding(false);

        Button confirmButton = new Button("Löschen", event -> {
            try {
                supplierService.deleteSupplier(supplier.getId());
                confirmDialog.close();
                updateList();
                showSuccessNotification("Lieferant wurde erfolgreich gelöscht!");
            } catch (Exception ex) {
                confirmDialog.close();
                showErrorNotification("Fehler beim Löschen: " + ex.getMessage());
            }
        });
        confirmButton.getStyle().set("color", "white");
        confirmButton.getStyle().set("background-color", "#d32f2f");

        Button cancelButton = new Button("Abbrechen", event -> confirmDialog.close());

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        content.add(buttons);

        confirmDialog.add(content);
        confirmDialog.open();
    }

    /**
     * Shows a success notification to the user
     */
    private void showSuccessNotification(String message) {
        Dialog notification = new Dialog();
        notification.setWidth("400px");

        VerticalLayout content = new VerticalLayout(new Span(message));
        content.setPadding(true);
        content.getStyle().set("color", "#2e7d32");
        content.getStyle().set("font-weight", "bold");

        Button closeButton = new Button("OK", e -> notification.close());
        closeButton.getStyle().set("background-color", "#4caf50");
        closeButton.getStyle().set("color", "white");

        content.add(closeButton);
        notification.add(content);
        notification.open();

        // Auto-close after 3 seconds
        notification.getElement().executeJs(
                "setTimeout(() => $0.opened = false, 3000)", notification.getElement()
        );
    }

    /**
     * Shows an error notification to the user
     */
    private void showErrorNotification(String message) {
        Dialog notification = new Dialog();
        notification.setWidth("400px");

        VerticalLayout content = new VerticalLayout(new Span(message));
        content.setPadding(true);
        content.getStyle().set("color", "#c62828");
        content.getStyle().set("font-weight", "bold");

        Button closeButton = new Button("OK", e -> notification.close());
        closeButton.getStyle().set("background-color", "#f44336");
        closeButton.getStyle().set("color", "white");

        content.add(closeButton);
        notification.add(content);
        notification.open();
    }

    /**
     * Inner class for managing contact person input within the supplier form.
     * Allows selecting existing contact persons or creating new ones.
     */
    private static class ContactPersonForm extends VerticalLayout {

        private final ContactPersonRepository contactPersonRepository;
        private final ComboBox<ContactPersonResponseDTO> existingContactBox;
        private final TextField firstNameField;
        private final TextField lastNameField;
        private final TextField roleField;
        private final TextField phoneField;
        private final EmailField emailField;
        private final Button toggleButton;

        private boolean useExisting = true;
        private Long existingContactId = null;

        public ContactPersonForm(ContactPersonRepository contactPersonRepository) {
            this.contactPersonRepository = contactPersonRepository;

            setPadding(true);
            setSpacing(true);
            getStyle().set("border", "1px solid #ddd");
            getStyle().set("border-radius", "4px");
            getStyle().set("margin-bottom", "10px");
            getStyle().set("background-color", "#f9f9f9");

            // Toggle between existing and new contact
            toggleButton = new Button("Neu anlegen", e -> toggleMode());

            // Existing contact selection
            existingContactBox = new ComboBox<>("Bestehende Kontaktperson auswählen");
            existingContactBox.setWidthFull();

            // Load all contact persons from repository
            List<ContactPersonResponseDTO> contacts = contactPersonRepository.findAll().stream()
                    .map(cp -> {
                        ContactPersonResponseDTO dto = new ContactPersonResponseDTO();
                        dto.setId(cp.getId());
                        dto.setFirstName(cp.getFirstName());
                        dto.setLastName(cp.getLastName());
                        dto.setRole(cp.getRole());
                        dto.setPhone(cp.getPhone());
                        dto.setEmail(cp.getEmail());
                        return dto;
                    })
                    .toList();

            existingContactBox.setItems(contacts);
            existingContactBox.setItemLabelGenerator(cp ->
                    cp.getFirstName() + " " + cp.getLastName() + " (" + cp.getEmail() + ")"
            );
            existingContactBox.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    existingContactId = e.getValue().getId();
                }
            });

            // New contact fields
            firstNameField = new TextField("Vorname");
            firstNameField.setRequired(true);

            lastNameField = new TextField("Nachname");
            lastNameField.setRequired(true);

            roleField = new TextField("Position");
            phoneField = new TextField("Telefon");

            emailField = new EmailField("E-Mail");
            emailField.setRequired(true);

            firstNameField.setWidthFull();
            lastNameField.setWidthFull();
            roleField.setWidthFull();
            phoneField.setWidthFull();
            emailField.setWidthFull();

            // Initially show existing contact selection
            add(toggleButton, existingContactBox);
            setNewFieldsVisible(false);
        }

        private void toggleMode() {
            useExisting = !useExisting;
            if (useExisting) {
                toggleButton.setText("Neu anlegen");
                setNewFieldsVisible(false);
                if (!getChildren().anyMatch(c -> c == existingContactBox)) {
                    addComponentAtIndex(1, existingContactBox);
                }
            } else {
                toggleButton.setText("Bestehende auswählen");
                remove(existingContactBox);
                setNewFieldsVisible(true);
            }
        }

        private void setNewFieldsVisible(boolean visible) {
            if (visible) {
                if (!getChildren().anyMatch(c -> c == firstNameField)) {
                    add(firstNameField, lastNameField, roleField, phoneField, emailField);
                }
            } else {
                remove(firstNameField, lastNameField, roleField, phoneField, emailField);
            }
        }

        public void setContactPerson(ContactPersonResponseDTO cp) {
            useExisting = true;
            existingContactBox.setValue(cp);
            existingContactId = cp.getId();
        }

        public ContactPersonRequestDTO getContactPersonRequest() {
            ContactPersonRequestDTO req = new ContactPersonRequestDTO();

            if (useExisting && existingContactId != null) {
                // Use existing contact - set the ID so backend can link it
                req.setId(existingContactId);
                return req;
            } else if (!useExisting && !firstNameField.isEmpty() &&
                    !lastNameField.isEmpty() && !emailField.isEmpty()) {
                // Create new contact
                req.setFirstName(firstNameField.getValue());
                req.setLastName(lastNameField.getValue());
                req.setRole(roleField.getValue());
                req.setPhone(phoneField.getValue());
                req.setEmail(emailField.getValue());
                return req;
            }
            return null;
        }
    }
}