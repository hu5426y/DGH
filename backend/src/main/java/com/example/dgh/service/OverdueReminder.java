package com.example.dgh.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueReminder {
    private static final Logger logger = LoggerFactory.getLogger(OverdueReminder.class);
    private final TicketService ticketService;

    public OverdueReminder(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void remind() {
        List<?> overdue = ticketService.findOverdueTickets();
        if (!overdue.isEmpty()) {
            logger.warn("[提醒] 当前有 {} 条工单超过处理时限，请及时处理。", overdue.size());
        }
    }
}
