package de.fhdw.fillialensystem.utility.scheduler;

import de.fhdw.fillialensystem.persistence.service.ReceiptService;
import org.springframework.stereotype.Service;

@Service
public class DailyReceiptReportingSchedule {

    private final ReceiptService receiptService;

    public DailyReceiptReportingSchedule(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }
}