package com.example.dgh.service;

import com.example.dgh.dto.AssignTicketRequest;
import com.example.dgh.dto.CheckInRequest;
import com.example.dgh.dto.CreateTicketRequest;
import com.example.dgh.dto.FeedbackRequest;
import com.example.dgh.dto.StatsResponse;
import com.example.dgh.dto.UpdateStatusRequest;
import com.example.dgh.entity.CheckIn;
import com.example.dgh.entity.Ticket;
import com.example.dgh.entity.TicketStatus;
import com.example.dgh.repository.CheckInRepository;
import com.example.dgh.repository.TicketRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CheckInRepository checkInRepository;

    public TicketService(TicketRepository ticketRepository, CheckInRepository checkInRepository) {
        this.ticketRepository = ticketRepository;
        this.checkInRepository = checkInRepository;
    }

    public Ticket createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setImages(request.getImages());
        ticket.setLocation(request.getLocation());
        ticket.setReporterName(request.getReporterName());
        ticket.setReporterContact(request.getReporterContact());

        String autoAssignName = System.getenv("AUTO_ASSIGN_NAME");
        String autoAssignContact = System.getenv("AUTO_ASSIGN_CONTACT");
        if (autoAssignName != null && !autoAssignName.isBlank()) {
            ticket.setAssignedToName(autoAssignName);
            ticket.setAssignedToContact(autoAssignContact);
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        } else {
            ticket.setStatus(TicketStatus.PENDING);
        }

        return ticketRepository.save(ticket);
    }

    public List<Ticket> listTickets() {
        return ticketRepository.findAll().stream()
            .sorted(Comparator.comparing(Ticket::getCreatedAt).reversed())
            .collect(Collectors.toList());
    }

    public Ticket getTicket(String id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "工单不存在"));
    }

    public Ticket updateStatus(String id, UpdateStatusRequest request) {
        Ticket ticket = getTicket(id);
        ticket.setStatus(request.getStatus());
        return ticketRepository.save(ticket);
    }

    public Ticket assignTicket(String id, AssignTicketRequest request) {
        Ticket ticket = getTicket(id);
        ticket.setAssignedToName(request.getAssignedToName());
        ticket.setAssignedToContact(request.getAssignedToContact());
        if (ticket.getStatus() == TicketStatus.PENDING) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        return ticketRepository.save(ticket);
    }

    @Transactional
    public CheckIn checkIn(String id, CheckInRequest request) {
        Ticket ticket = getTicket(id);
        CheckIn checkIn = new CheckIn();
        checkIn.setTechnicianName(request.getTechnicianName());
        checkIn.setLocation(request.getLocation());
        checkIn.setTicket(ticket);
        CheckIn saved = checkInRepository.save(checkIn);
        ticket.addCheckIn(saved);
        return saved;
    }

    public Ticket submitFeedback(String id, FeedbackRequest request) {
        Ticket ticket = getTicket(id);
        ticket.setRating(request.getRating());
        ticket.setFeedback(request.getFeedback());
        return ticketRepository.save(ticket);
    }

    public StatsResponse stats() {
        List<Ticket> tickets = ticketRepository.findAll();
        long total = tickets.size();
        long completed = tickets.stream().filter(ticket -> ticket.getStatus() == TicketStatus.COMPLETED).count();
        List<Integer> ratings = tickets.stream()
            .map(Ticket::getRating)
            .filter(rating -> rating != null)
            .collect(Collectors.toList());
        double avgRating = ratings.isEmpty() ? 0 : ratings.stream().mapToInt(Integer::intValue).average().orElse(0);
        Map<String, Long> frequentLocations = tickets.stream()
            .collect(Collectors.groupingBy(Ticket::getLocation, Collectors.counting()));
        return new StatsResponse(total, completed, Math.round(avgRating * 100.0) / 100.0, frequentLocations);
    }

    public List<Ticket> findOverdueTickets(int hours) {
        LocalDateTime deadline = LocalDateTime.now().minusHours(hours);
        return ticketRepository.findByStatusNotAndCreatedAtBefore(TicketStatus.COMPLETED, deadline);
    }

    public List<Ticket> findOverdueTickets() {
        return findOverdueTickets(24);
    }
}
