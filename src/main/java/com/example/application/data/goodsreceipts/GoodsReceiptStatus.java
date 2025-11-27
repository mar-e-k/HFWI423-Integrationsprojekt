package com.example.application.data.goodsreceipts;

public enum GoodsReceiptStatus {
    IN_PRUEFUNG,  // direkt nach Anlage
    GEPRUEFT,     // Prüfung abgeschlossen
    FREIGEGEBEN,  // Lieferung insgesamt freigegeben / i.O.
    ABGELEHNT     // (optional für später, falls ganze Lieferung verworfen wird)
}
