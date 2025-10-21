package com.example.application.views.gridwithfilters;

import com.example.application.data.ArticleInfo;
import com.example.application.services.ArticleInfoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;

@PageTitle("Logistik")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.FILTER_SOLID)
@Uses(Icon.class)
public class GridwithFiltersView extends Div {

    private Grid<ArticleInfo> grid;
    private com.vaadin.flow.component.dialog.Dialog addDialog;
    private Filters filters;
    private final ArticleInfoService articleInfoService;

    public GridwithFiltersView(ArticleInfoService articleInfoService) {
        this.articleInfoService = articleInfoService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new Filters(this::refreshGrid);

        // Grid erstellen
        Component gridComponent = createGrid();

        // Datenquelle an Grid binden
        setupDataProvider();

        // AddButton, der das AddDialogfenster öffnet
        Button addBtn = new Button("Neuen Artikel", e -> {
            if (addDialog == null) {
                addDialog = buildAddDialog();
            }
            addDialog.open();
        });
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Toolbar rechtsbündig
        HorizontalLayout toolbar = new HorizontalLayout(addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // Seite zusammensetzen
        VerticalLayout layout = new VerticalLayout(
                createMobileFilters(),
                filters,
                toolbar,
                gridComponent
        );
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div implements Specification<ArticleInfo> {

        private final TextField articleName = new TextField("Artikelname");
        private final IntegerField articleNumber = new IntegerField("Artikelnummer");
        private final TextField inventory = new TextField("Bestand");
        private final TextField storageLocation = new TextField("Lagerort");

        public Filters(Runnable onSearch) {
            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM, LumoUtility.BoxSizing.BORDER);

            articleName.setPlaceholder("Name des Artikels");
            articleNumber.setPlaceholder("z. B. 12345");
            articleNumber.setStepButtonsVisible(true);
            articleNumber.setMin(0);
            inventory.setPlaceholder("z. B. 5");
            storageLocation.setPlaceholder("Lagerort eingeben");
            storageLocation.setAriaLabel("Lagerort");

            Button resetBtn = new Button("Felder zurücksetzen", e -> {
                articleName.clear();
                articleNumber.clear();
                inventory.clear();
                storageLocation.clear();
                onSearch.run();
            });
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button searchBtn = new Button("Artikel suchen", e -> onSearch.run());
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(articleName, articleNumber, inventory, storageLocation, actions);
        }

        @Override
        public Predicate toPredicate(Root<ArticleInfo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            List<Predicate> ps = new ArrayList<>();

            if (!articleName.isEmpty()) {
                String v = "%" + articleName.getValue().toLowerCase() + "%";
                ps.add(cb.like(cb.lower(root.get("articleName")), v));
            }
            //vergleicht numerisch zwischen der DB Spalte und dem IntegerField
            if (articleNumber.getValue() != null) {
                ps.add(cb.equal(root.get("articleNumber"), articleNumber.getValue()));
            }
            if (!inventory.isEmpty()) {
                try {
                    int inv = Integer.parseInt(inventory.getValue().trim());
                    ps.add(cb.greaterThanOrEqualTo(root.get("inventory"), inv));
                } catch (NumberFormatException ignored) {}
            }
            if (!storageLocation.isEmpty()) {
                String v = "%" + storageLocation.getValue().toLowerCase() + "%";
                ps.add(cb.like(cb.lower(root.get("storageLocation")), v));
            }

            return ps.isEmpty() ? cb.conjunction() : cb.and(ps.toArray(Predicate[]::new));
        }
    }

    private Component createGrid() {

        grid = new Grid<>(ArticleInfo.class, false);

        grid.addColumn(ArticleInfo::getArticleName)
                .setHeader("Artikelname")
                .setKey("articleName")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ArticleInfo::getArticleNumber)
                .setHeader("Artikelnummer")
                .setKey("articleNumber")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ArticleInfo::getInventory)
                .setHeader("Bestand")
                .setKey("inventory")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(ArticleInfo::getStorageLocation)
                .setHeader("Lagerort")
                .setKey("storageLocation")
                .setAutoWidth(true)
                .setSortable(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setSizeFull();
        return grid;
    }
    private Dialog buildAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Neuen Artikel anlegen");

        TextField fName = new TextField("Artikelname");
        fName.setRequired(true);

        // Zahl statt Text
        IntegerField fNumber = new IntegerField("Artikelnummer");
        fNumber.setRequiredIndicatorVisible(true);
        fNumber.setMin(0);
        fNumber.setStepButtonsVisible(true);

        IntegerField fInventory = new IntegerField("Bestand");
        fInventory.setMin(0);
        fInventory.setStepButtonsVisible(true);
        fInventory.setValue(0);

        TextField fStorage = new TextField("Lagerort");

        FormLayout form = new FormLayout(fName, fNumber, fInventory, fStorage);
        dialog.add(form);

        Binder<ArticleInfo> binder = new Binder<>(ArticleInfo.class);

        binder.forField(fName)
                .asRequired("Bitte Artikelnamen angeben")
                .bind(ArticleInfo::getArticleName, ArticleInfo::setArticleName);

        binder.forField(fNumber)
                .asRequired("Bitte Artikelnummer angeben")
                .bind(ArticleInfo::getArticleNumber, ArticleInfo::setArticleNumber);

        binder.forField(fInventory)
                .withValidator(v -> v != null && v >= 0, "Bestand ≥ 0")
                .bind(ArticleInfo::getInventory, ArticleInfo::setInventory);

        binder.forField(fStorage)
                .asRequired("Bitte Lagerort angeben")
                .bind(ArticleInfo::getStorageLocation, ArticleInfo::setStorageLocation);


        Button cancel = new Button("Abbrechen", e -> dialog.close());
        Button save = new Button("Speichern", e -> {
            ArticleInfo bean = new ArticleInfo();
            if (binder.writeBeanIfValid(bean)) {
                articleInfoService.save(bean);
                dialog.close();
                refreshGrid();
                Notification.show("Artikel gespeichert");
            } else {
                Notification.show("Bitte Eingaben prüfen");
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, save);
        return dialog;
    }
    private void setupDataProvider() {
        DataProvider<ArticleInfo, Void> dataProvider = DataProvider.fromCallbacks(
                // FETCH
                (Query<ArticleInfo, Void> q) -> articleInfoService
                        .list(VaadinSpringDataHelpers.toSpringPageRequest(q), filters)
                        .stream(),
                // COUNT
                (Query<ArticleInfo, Void> q) -> (int) articleInfoService.count(filters)
        );

        grid.setDataProvider(dataProvider);
    }
    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}