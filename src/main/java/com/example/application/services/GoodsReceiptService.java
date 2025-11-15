package com.example.application.services;

import com.example.application.data.goodsreceipts.GoodsReceipt;
import com.example.application.data.goodsreceipts.GoodsReceiptRepository;
import com.example.application.data.goodsreceipts.GoodsReceiptStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Service
public class GoodsReceiptService {

    private final GoodsReceiptRepository repo;
    private final JdbcTemplate jdbc;

    public GoodsReceiptService(GoodsReceiptRepository repo, JdbcTemplate jdbc) {
        this.repo = repo;
        this.jdbc = jdbc;
    }

    // Zieht die nächste Zahl aus goods_receipt_seq und formatiert WE-YYYY-00001
    private String nextReceiptNumber() {
        Long next = jdbc.queryForObject("select nextval('goods_receipt_seq')", Long.class);
        String year = String.valueOf(Year.now().getValue());
        return String.format("WE-%s-%05d", year, next);
    }
        //Hier abhängig von der DB. NACHFRAGEN!!!
    @Transactional
    public GoodsReceipt create(String supplierName, String deliveryNoteNumber, LocalDate deliveryDate) {
        GoodsReceipt gr = new GoodsReceipt();
        gr.setReceiptNumber(nextReceiptNumber());
        gr.setSupplierName(supplierName);
        gr.setDeliveryNoteNumber(deliveryNoteNumber);
        gr.setDeliveryDate(deliveryDate);
        gr.setStatus(GoodsReceiptStatus.IN_PRUEFUNG);
        return repo.save(gr);
    }

    public List<GoodsReceipt> findAll() { return repo.findAll(); }

    @Transactional
    public GoodsReceipt updateStatus(Long id, GoodsReceiptStatus status) {
        GoodsReceipt gr = repo.findById(id).orElseThrow();
        gr.setStatus(status);
        return repo.save(gr);
    }
}
