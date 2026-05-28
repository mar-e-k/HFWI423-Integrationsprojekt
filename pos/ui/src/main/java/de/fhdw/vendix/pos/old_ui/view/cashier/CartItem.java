package de.fhdw.vendix.pos.old_ui.view.cashier;

//public class CartItem implements GenericEntity<Long> {
//    private ArticleDTO article;
//    private int position;
//    private int quantity;
//    private BigDecimal overriddenPrice;
//    private DepositStatus depositStatus;
//
//    // NEU: Rabatt-Infos
//    private BigDecimal discountPercent;   // z.B. 30 für 30 %
//    private Integer discountedQuantity;   // wie viele Stück der Position bekommen Rabatt
//
//    public CartItem() {
//        super();
//    }
//
//    public CartItem(ArticleDTO article, int position, int quantity, BigDecimal overriddenPrice, DepositStatus depositStatus) {
//        this.article = article;
//        this.position = position;
//        this.quantity = quantity;
//        this.overriddenPrice = overriddenPrice;
//        this.discountedQuantity = 0;
//        this.discountPercent = BigDecimal.ZERO;
//        this.depositStatus = depositStatus;
//    }
//
//    public ArticleDTO getArticle() {
//        return article;
//    }
//
//    public void setArticle(ArticleDTO article) {
//        this.article = article;
//    }
//
//    public int getPosition() {
//        return position;
//    }
//
//    public void setPosition(int position) {
//        this.position = position;
//    }
//
//    public int getQuantity() {
//        return quantity;
//    }
//
//    public void setQuantity(int quantity) {
//        this.quantity = quantity;
//    }
//
//    public BigDecimal getOverriddenPrice() {
//        return overriddenPrice;
//    }
//
//    public void setOverriddenPrice(BigDecimal overriddenPrice) {
//        this.overriddenPrice = overriddenPrice;
//    }
//
//    public DepositStatus getDepositStatus() {
//        return depositStatus;
//    }
//
//    public void setDepositStatus(DepositStatus depositStatus) {
//        this.depositStatus = depositStatus;
//    }
//
//    // -----------------------------
//    // Rabatt-spezifische Methoden
//    // -----------------------------
//
//    public BigDecimal getDiscountPercent() {
//        return discountPercent;
//    }
//
//    public void setDiscountPercent(BigDecimal discountPercent) {
//        this.discountPercent = discountPercent;
//    }
//
//    public Integer getDiscountedQuantity() {
//        return discountedQuantity;
//    }
//
//    public void setDiscountedQuantity(Integer discountedQuantity) {
//        this.discountedQuantity = discountedQuantity;
//    }
//
//    public void clearDiscount() {
//        this.discountPercent = BigDecimal.ZERO;
//        this.discountedQuantity = 0;
//    }
//
//    public boolean hasDiscount() {
//        return discountPercent != null
//                && discountedQuantity != null
//                && discountPercent.compareTo(BigDecimal.ZERO) > 0
//                && discountedQuantity > 0;
//    }
//
//    /**
//     * Basis-Stückpreis (ohne Rabatt, aber mit Overwrite, falls gesetzt).
//     */
//    public BigDecimal getBaseUnitPrice() {
//        // Wenn ein overriddenPrice gesetzt ist, hat dieser immer Vorrang
//        if (overriddenPrice != null) {
//            return overriddenPrice;
//        }
//        // Wenn es eine leere Pfandflasche ist und kein overriddenPrice gesetzt ist,
//        // dann den Standard-Pfandbetrag verwenden
//        if (depositStatus == DepositStatus.EMPTY) {
//            return new BigDecimal("-0.25");
//        }
//        // Ansonsten den normalen Verkaufspreis des Artikels verwenden
//        Double sellingPrice = article.getSellingPrice();
//        if (sellingPrice == null) {
//            return BigDecimal.ZERO;
//        }
//        return BigDecimal.valueOf(sellingPrice);
//    }
//
//    /**
//     * Rabattierter Stückpreis für die rabattierten Artikel.
//     */
//    public BigDecimal getDiscountedUnitPrice() {
//        if (!hasDiscount()) {
//            return getBaseUnitPrice();
//        }
//
//        BigDecimal base = getBaseUnitPrice();
//        // Faktor = 1 - (Rabatt% / 100)
//        BigDecimal factor = BigDecimal.ONE.subtract(
//                discountPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
//        );
//
//        return base
//                .multiply(factor)
//                .setScale(2, RoundingMode.HALF_UP);
//    }
//
//    /**
//     * Gesamtpreis dieser Position inkl. Teil-Rabatt.
//     * - Wenn kein Rabatt gepflegt ist: quantity * Basispreis
//     * - Wenn rabattierte Menge >= Gesamtmenge: alles rabattiert
//     * - Sonst: Teilmenge rabattiert, Rest zum Basispreis
//     */
//    public BigDecimal getTotalPriceWithDiscount() {
//        BigDecimal base = getBaseUnitPrice();
//
//        if (!hasDiscount() || discountedQuantity == null || discountedQuantity <= 0) {
//            return base
//                    .multiply(BigDecimal.valueOf(quantity))
//                    .setScale(2, RoundingMode.HALF_UP);
//        }
//
//        int discountQty = Math.min(discountedQuantity, quantity);
//        int normalQty = quantity - discountQty;
//
//        BigDecimal discountedTotal = getDiscountedUnitPrice()
//                .multiply(BigDecimal.valueOf(discountQty));
//        BigDecimal normalTotal = base
//                .multiply(BigDecimal.valueOf(normalQty));
//
//        return discountedTotal
//                .add(normalTotal)
//                .setScale(2, RoundingMode.HALF_UP);
//    }
//
//    @Override
//    @Deprecated
//    public Long getId() {
//        return null;
//    }
//
//    @Override
//    @Deprecated
//    public void setId(Long id) {}
//}
