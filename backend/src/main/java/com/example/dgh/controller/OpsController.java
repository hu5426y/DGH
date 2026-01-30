package com.example.dgh.controller;

import com.example.dgh.dto.OpsOverviewResponse;
import com.example.dgh.service.TicketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ops")
public class OpsController {
    private final TicketService ticketService;

    public OpsController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/overview")
    public OpsOverviewResponse overview() {
        return ticketService.opsOverview();
    }
}
