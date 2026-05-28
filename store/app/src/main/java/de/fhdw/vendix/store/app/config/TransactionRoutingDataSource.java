package de.fhdw.vendix.store.app.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Leitet Datenbankverbindungen je nach Transaktionstyp weiter:
 * <ul>
 *   <li>{@code readOnly=true}  → Read-Pool  (GTIN-Scans, Leseanfragen)</li>
 *   <li>{@code readOnly=false} → Write-Pool (Checkouts, Voucher-Einlösungen)</li>
 * </ul>
 *
 * <p>Der Routing-Schlüssel wird zur Laufzeit beim Öffnen jeder Connection
 * bestimmt — zu diesem Zeitpunkt ist der Transaktionsstatus bereits gesetzt,
 * sodass {@link TransactionSynchronizationManager#isCurrentTransactionReadOnly()}
 * den korrekten Wert liefert.
 */
class TransactionRoutingDataSource extends AbstractRoutingDataSource {

    static final String READ  = "READ";
    static final String WRITE = "WRITE";

    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                ? READ
                : WRITE;
    }
}
