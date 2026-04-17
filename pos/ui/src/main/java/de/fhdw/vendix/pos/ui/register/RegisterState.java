package de.fhdw.vendix.pos.ui.register;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@SessionScope
public class RegisterState {

    private Optional<ArticleDTO> selectedArticle = Optional.empty();
    private Optional<CartLine> selectedCartLine = Optional.empty();
    private int currentAmount = 1;

    private final List<Runnable> listeners = new ArrayList<>();

    public void setSelectedArticle(@Nullable ArticleDTO article) {
        this.selectedArticle = Optional.ofNullable(article);
        notifyListeners();
    }

    public Optional<ArticleDTO> getSelectedArticle() {
        return selectedArticle;
    }

    public void setSelectedCartLine(CartLine line) {
        this.selectedCartLine = Optional.ofNullable(line);
        notifyListeners();
    }

    public Optional<CartLine> getSelectedCartLine() {
        return selectedCartLine;
    }

    public int getAmount() {
        return currentAmount;
    }

    public void setAmount(int amount) {
        this.currentAmount = amount;
        notifyListeners();
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        listeners.forEach(Runnable::run);
    }
}