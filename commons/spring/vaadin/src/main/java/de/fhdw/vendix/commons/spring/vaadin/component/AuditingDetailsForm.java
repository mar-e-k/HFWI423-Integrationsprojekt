package de.fhdw.vendix.commons.spring.vaadin.component;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;

public class AuditingDetailsForm extends VerticalLayout {

    private final FormLayout form = new FormLayout();
    private final TextField version = new TextField("Version");
    private final TextField createdAt = new TextField("Created At");
    private final TextField createdBy = new TextField("Created By");
    private final TextField changedAt = new TextField("Changed At");
    private final TextField changedBy = new TextField("Changed By");
    private final Button closeButton = new Button("Close");

    private final Binder<AbstractSpringDataAuditingEntity> binder = new Binder<>(AbstractSpringDataAuditingEntity.class);

    public AuditingDetailsForm() {
        setSizeFull();
        setPadding(true);

        configureFields();
        configureBinding();
        configureLayout();

        add(form);
    }

    protected void configureFields() {
        version.setReadOnly(true);
        createdAt.setReadOnly(true);
        createdBy.setReadOnly(true);
        changedAt.setReadOnly(true);
        changedBy.setReadOnly(true);
        closeButton.addClickListener(event -> {
            fireEvent(new DetailClosePressEvent(this, false));
        });
    }

    protected void configureBinding() {
        binder.forField(version)
                .bind(e -> String.valueOf(e.getVersion()), null);
        binder.forField(createdAt)
                .bind(e -> String.valueOf(e.getChangedAt()), null);

        binder.forField(createdBy)
                .bind(AbstractSpringDataAuditingEntity::getCreatedBy, null);

        binder.forField(changedAt)
                .bind(e -> String.valueOf(e.getChangedAt()), null);

        binder.forField(changedBy)
                .bind(AbstractSpringDataAuditingEntity::getChangedBy, null);
    }

    private void configureLayout() {
        form.setAutoResponsive(true);
        form.addFormRow(version);
        form.addFormRow(createdAt, createdBy);
        form.addFormRow(changedAt, changedBy);
        form.addFormRow(closeButton);
    }

    public void setEntity(AbstractSpringDataAuditingEntity<?> entity) {
        binder.setBean(entity);
    }

    public void clear() {
        binder.setBean(null);
    }

    public Registration addCloseListener(ComponentEventListener<DetailClosePressEvent> listener) {
        return addListener(DetailClosePressEvent.class, listener);
    }

    @DomEvent("detail-close-press")
    public static class DetailClosePressEvent extends ComponentEvent<AuditingDetailsForm> {
        public DetailClosePressEvent(AuditingDetailsForm source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}