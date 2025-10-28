package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.fhdw.kassensystem.persistence.service.ArticleService;
import jakarta.annotation.security.RolesAllowed;

@Route("cashier")
@RolesAllowed("CASHIER")
public class CashierView extends BaseView {

    private final ArticleService articleService;

    private TextField txArticleInput;
    private Button btArticleButton;

    public CashierView(ArticleService articleService) {
        super();
        this.articleService = articleService;
        init();
        setViewTitle("Kassenansicht");

        // Hier kommt die weitere Cashier-Implementierung hin
    }

    private void init() {
        txArticleInput = new TextField("Artikelnummer");
        btArticleButton = new Button("Artikel hinzufügen");

        btArticleButton.addClickListener(clickEvent -> {
            String value = txArticleInput.getValue();
            try {
                articleService.findById(Long.parseLong(value)).ifPresent(System.out::println);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });

        // Add components to the view (BaseView should likely extend VerticalLayout)
        add(txArticleInput, btArticleButton);
    }
}