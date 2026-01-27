package com.example.dgh.controller;

import com.example.dgh.dto.AssignTicketRequest;
import com.example.dgh.dto.CheckInRequest;
import com.example.dgh.dto.CreateTicketRequest;
import com.example.dgh.dto.FeedbackRequest;
import com.example.dgh.dto.UpdateStatusRequest;
import com.example.dgh.entity.CheckIn;
import com.example.dgh.entity.Ticket;
import com.example.dgh.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public Ticket create(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(request);
    }

    @GetMapping
    public List<Ticket> list() {
        return ticketService.listTickets();
    }

    @GetMapping("/{id}")
    public Ticket detail(@PathVariable String id) {
        return ticketService.getTicket(id);
    }

    @PatchMapping("/{id}/status")
    public Ticket updateStatus(@PathVariable String id, @Valid @RequestBody UpdateStatusRequest request) {
        return ticketService.updateStatus(id, request);
    }

    @PatchMapping("/{id}/assign")
    public Ticket assign(@PathVariable String id, @Valid @RequestBody AssignTicketRequest request) {
        return ticketService.assignTicket(id, request);
    }

    @PostMapping("/{id}/checkins")
    public CheckIn checkIn(@PathVariable String id, @Valid @RequestBody CheckInRequest request) {
        return ticketService.checkIn(id, request);
    }

    @PostMapping("/{id}/feedback")
    public Ticket feedback(@PathVariable String id, @Valid @RequestBody FeedbackRequest request) {
        return ticketService.submitFeedback(id, request);
    }

    @GetMapping("/overdue/list")
    public List<Ticket> overdue() {
        return ticketService.findOverdueTickets();
    }
}
