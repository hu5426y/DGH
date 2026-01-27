package com.example.dgh.repository;

import com.example.dgh.entity.Ticket;
import com.example.dgh.entity.TicketStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, String> {
    List<Ticket> findByStatusNotAndCreatedAtBefore(TicketStatus status, LocalDateTime createdAt);
}
