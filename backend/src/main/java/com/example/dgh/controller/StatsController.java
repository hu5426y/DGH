package com.example.dgh.controller;

import com.example.dgh.dto.InsightSummaryResponse;
import com.example.dgh.dto.StatsResponse;
import com.example.dgh.service.TicketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final TicketService ticketService;

    public StatsController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public StatsResponse stats() {
        return ticketService.stats();
    }

    @GetMapping("/insights/summary")
    public InsightSummaryResponse insightSummary() {
        return ticketService.insightSummary();
    }
}
