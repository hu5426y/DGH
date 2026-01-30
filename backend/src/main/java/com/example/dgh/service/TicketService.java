package com.example.dgh.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dgh.dto.AssignTicketRequest;
import com.example.dgh.dto.CheckInRequest;
import com.example.dgh.dto.CreateTicketRequest;
import com.example.dgh.dto.AreaLeadResponse;
import com.example.dgh.dto.FeedbackRequest;
import com.example.dgh.dto.InsightSummaryResponse;
import com.example.dgh.dto.IssueStatResponse;
import com.example.dgh.dto.OpsOverviewResponse;
import com.example.dgh.dto.StatsResponse;
import com.example.dgh.dto.TechnicianPerformanceResponse;
import com.example.dgh.dto.UpdateStatusRequest;
import com.example.dgh.entity.CheckIn;
import com.example.dgh.entity.Ticket;
import com.example.dgh.entity.TicketStatus;
import com.example.dgh.entity.User;
import com.example.dgh.entity.UserRole;
import com.example.dgh.entity.Location;
import com.example.dgh.mapper.CheckInMapper;
import com.example.dgh.mapper.LocationMapper;
import com.example.dgh.mapper.TicketMapper;
import com.example.dgh.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketService {
    private final TicketMapper ticketMapper;
    private final CheckInMapper checkInMapper;
    private final UserMapper userMapper;
    private final LocationMapper locationMapper;

    public TicketService(TicketMapper ticketMapper, CheckInMapper checkInMapper, UserMapper userMapper, LocationMapper locationMapper) {
        this.ticketMapper = ticketMapper;
        this.checkInMapper = checkInMapper;
        this.userMapper = userMapper;
        this.locationMapper = locationMapper;
    }

    public Ticket createTicket(CreateTicketRequest request) {
        User reporter = userMapper.selectById(request.getReporterId());
        if (reporter == null || reporter.getIsActive() == null || reporter.getIsActive() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "报修人不存在或已停用");
        }
        if (reporter.getRoleCode() != UserRole.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅普通用户可报修");
        }
        Location location = locationMapper.selectById(request.getLocationId());
        if (location == null || location.getIsActive() == null || location.getIsActive() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "位置无效或未启用");
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setImages(request.getImages());
        ticket.setLocationId(request.getLocationId());
        ticket.setReporterId(request.getReporterId());

        String autoAssignName = System.getenv("AUTO_ASSIGN_NAME");
        String autoAssignContact = System.getenv("AUTO_ASSIGN_CONTACT");
        if (autoAssignName != null && !autoAssignName.isBlank()) {
            ticket.setAssignedToName(autoAssignName);
            ticket.setAssignedToContact(autoAssignContact);
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        } else {
            ticket.setStatus(TicketStatus.PENDING);
        }

        ticketMapper.insert(ticket);
        Ticket saved = attachCheckIns(ticket);
        hydrateTicket(saved, reporter, location);
        return saved;
    }

    public List<Ticket> listTickets(String reporterId) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<Ticket>().orderByDesc(Ticket::getCreatedAt);
        if (reporterId != null && !reporterId.isBlank()) {
            wrapper.eq(Ticket::getReporterId, reporterId);
        }
        List<Ticket> tickets = ticketMapper.selectList(wrapper);
        List<Ticket> withCheckins = attachCheckIns(tickets);
        hydrateTickets(withCheckins);
        return withCheckins;
    }

    public Ticket getTicket(String id) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "工单不存在");
        }
        Ticket withCheckins = attachCheckIns(ticket);
        hydrateTickets(List.of(withCheckins));
        return withCheckins;
    }

    public Ticket updateStatus(String id, UpdateStatusRequest request) {
        Ticket ticket = getTicket(id);
        ticket.setStatus(request.getStatus());
        ticketMapper.updateById(ticket);
        Ticket withCheckins = attachCheckIns(ticket);
        hydrateTickets(List.of(withCheckins));
        return withCheckins;
    }

    public Ticket assignTicket(String id, AssignTicketRequest request) {
        Ticket ticket = getTicket(id);
        ticket.setAssignedToName(request.getAssignedToName());
        ticket.setAssignedToContact(request.getAssignedToContact());
        if (ticket.getStatus() == TicketStatus.PENDING) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        ticketMapper.updateById(ticket);
        Ticket withCheckins = attachCheckIns(ticket);
        hydrateTickets(List.of(withCheckins));
        return withCheckins;
    }

    @Transactional
    public CheckIn checkIn(String id, CheckInRequest request) {
        Ticket ticket = getTicket(id);
        CheckIn checkIn = new CheckIn();
        checkIn.setTicketId(ticket.getId());
        checkIn.setTechnicianName(request.getTechnicianName());
        checkIn.setLocation(request.getLocation());
        checkInMapper.insert(checkIn);
        return checkIn;
    }

    public Ticket submitFeedback(String id, FeedbackRequest request) {
        Ticket ticket = getTicket(id);
        ticket.setRating(request.getRating());
        ticket.setFeedback(request.getFeedback());
        ticketMapper.updateById(ticket);
        Ticket withCheckins = attachCheckIns(ticket);
        hydrateTickets(List.of(withCheckins));
        return withCheckins;
    }

    public StatsResponse stats() {
        List<Ticket> tickets = ticketMapper.selectList(new LambdaQueryWrapper<>());
        long total = tickets.size();
        long completed = tickets.stream().filter(ticket -> ticket.getStatus() == TicketStatus.COMPLETED).count();
        List<Integer> ratings = tickets.stream()
            .map(Ticket::getRating)
            .filter(rating -> rating != null)
            .collect(Collectors.toList());
        double avgRating = ratings.isEmpty() ? 0 : ratings.stream().mapToInt(Integer::intValue).average().orElse(0);
        Map<String, String> locationNames = locationMapper.selectList(new LambdaQueryWrapper<Location>())
            .stream()
            .collect(Collectors.toMap(Location::getId, Location::getName, (a, b) -> a));
        Map<String, Long> frequentLocations = tickets.stream()
            .map(ticket -> locationNames.getOrDefault(ticket.getLocationId(), "未知位置"))
            .collect(Collectors.groupingBy(name -> name, LinkedHashMap::new, Collectors.counting()));
        return new StatsResponse(total, completed, Math.round(avgRating * 100.0) / 100.0, frequentLocations);
    }

    public InsightSummaryResponse insightSummary() {
        List<Ticket> tickets = ticketMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, String> locationNames = locationMapper.selectList(new LambdaQueryWrapper<Location>())
            .stream()
            .collect(Collectors.toMap(Location::getId, Location::getName, (a, b) -> a));

        String highFrequencyArea = tickets.stream()
            .map(ticket -> locationNames.getOrDefault(ticket.getLocationId(), "未知位置"))
            .collect(Collectors.groupingBy(name -> name, Collectors.counting()))
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey())
            .orElse("暂无数据");

        int averageResponseMinutes = (int) Math.round(tickets.stream()
            .filter(ticket -> ticket.getStatus() != null && ticket.getStatus() != TicketStatus.PENDING)
            .mapToLong(ticket -> java.time.Duration.between(ticket.getCreatedAt(), ticket.getUpdatedAt()).toMinutes())
            .filter(minutes -> minutes >= 0)
            .average()
            .orElse(0));

        int nightShiftPending = (int) tickets.stream()
            .filter(ticket -> ticket.getStatus() == TicketStatus.IN_PROGRESS || ticket.getStatus() == TicketStatus.PENDING)
            .count();

        return new InsightSummaryResponse(highFrequencyArea, averageResponseMinutes, nightShiftPending);
    }

    public OpsOverviewResponse opsOverview() {
        List<Ticket> tickets = ticketMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, String> locationNames = locationMapper.selectList(new LambdaQueryWrapper<Location>())
            .stream()
            .collect(Collectors.toMap(Location::getId, Location::getName, (a, b) -> a));

        int slaHours = 24;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime slaDeadline = now.minusHours(slaHours);
        LocalDateTime escalateDeadline = now.minusHours(48);

        long overdueCount = tickets.stream()
            .filter(ticket -> ticket.getStatus() != TicketStatus.COMPLETED)
            .filter(ticket -> ticket.getCreatedAt() != null && ticket.getCreatedAt().isBefore(slaDeadline))
            .count();

        long escalatedCount = tickets.stream()
            .filter(ticket -> ticket.getStatus() != TicketStatus.COMPLETED)
            .filter(ticket -> ticket.getCreatedAt() != null && ticket.getCreatedAt().isBefore(escalateDeadline))
            .count();

        List<Ticket> completedTickets = tickets.stream()
            .filter(ticket -> ticket.getStatus() == TicketStatus.COMPLETED)
            .filter(ticket -> ticket.getCreatedAt() != null && ticket.getUpdatedAt() != null)
            .collect(Collectors.toList());
        long completedWithinSla = completedTickets.stream()
            .filter(ticket -> ticket.getUpdatedAt().isBefore(ticket.getCreatedAt().plusHours(slaHours)))
            .count();
        double slaComplianceRate = completedTickets.isEmpty()
            ? 0
            : Math.round((completedWithinSla * 1000.0 / completedTickets.size())) / 10.0;

        Map<String, List<Ticket>> ticketsByArea = tickets.stream()
            .collect(Collectors.groupingBy(ticket -> locationNames.getOrDefault(ticket.getLocationId(), "未知位置")));
        List<AreaLeadResponse> areaLeads = ticketsByArea.entrySet()
            .stream()
            .map(entry -> {
                String area = entry.getKey();
                List<Ticket> areaTickets = entry.getValue();
                String suggestedLead = areaTickets.stream()
                    .map(Ticket::getAssignedToName)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.groupingBy(name -> name, Collectors.counting()))
                    .entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("待分配");
                return new AreaLeadResponse(area, areaTickets.size(), suggestedLead);
            })
            .sorted((a, b) -> Long.compare(b.getTicketCount(), a.getTicketCount()))
            .limit(4)
            .collect(Collectors.toList());

        List<TechnicianPerformanceResponse> technicianPerformance = tickets.stream()
            .filter(ticket -> ticket.getAssignedToName() != null && !ticket.getAssignedToName().isBlank())
            .collect(Collectors.groupingBy(Ticket::getAssignedToName))
            .entrySet()
            .stream()
            .map(entry -> {
                String technician = entry.getKey();
                List<Ticket> techTickets = entry.getValue();
                long completed = techTickets.stream()
                    .filter(ticket -> ticket.getStatus() == TicketStatus.COMPLETED)
                    .count();
                int avgMinutes = (int) Math.round(techTickets.stream()
                    .filter(ticket -> ticket.getStatus() == TicketStatus.COMPLETED)
                    .filter(ticket -> ticket.getCreatedAt() != null && ticket.getUpdatedAt() != null)
                    .mapToLong(ticket -> java.time.Duration.between(ticket.getCreatedAt(), ticket.getUpdatedAt()).toMinutes())
                    .filter(minutes -> minutes >= 0)
                    .average()
                    .orElse(0));
                return new TechnicianPerformanceResponse(technician, techTickets.size(), completed, avgMinutes);
            })
            .sorted((a, b) -> Long.compare(b.getCompletedTickets(), a.getCompletedTickets()))
            .limit(5)
            .collect(Collectors.toList());

        List<IssueStatResponse> issueStats = tickets.stream()
            .map(Ticket::getTitle)
            .filter(title -> title != null && !title.isBlank())
            .collect(Collectors.groupingBy(title -> title, Collectors.counting()))
            .entrySet()
            .stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(5)
            .map(entry -> new IssueStatResponse(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        long pendingFeedbackCount = tickets.stream()
            .filter(ticket -> ticket.getStatus() == TicketStatus.COMPLETED)
            .filter(ticket -> ticket.getRating() == null)
            .count();

        return new OpsOverviewResponse(
            slaHours,
            overdueCount,
            slaComplianceRate,
            areaLeads,
            technicianPerformance,
            issueStats,
            pendingFeedbackCount,
            escalatedCount
        );
    }

    public List<Ticket> findOverdueTickets(int hours) {
        LocalDateTime deadline = LocalDateTime.now().minusHours(hours);
        List<Ticket> tickets = ticketMapper.selectList(new LambdaQueryWrapper<Ticket>()
            .ne(Ticket::getStatus, TicketStatus.COMPLETED)
            .lt(Ticket::getCreatedAt, deadline));
        List<Ticket> withCheckins = attachCheckIns(tickets);
        hydrateTickets(withCheckins);
        return withCheckins;
    }

    public List<Ticket> findOverdueTickets() {
        return findOverdueTickets(24);
    }

    private Ticket attachCheckIns(Ticket ticket) {
        if (ticket == null) {
            return null;
        }
        List<CheckIn> checkIns = checkInMapper.selectList(new LambdaQueryWrapper<CheckIn>()
            .eq(CheckIn::getTicketId, ticket.getId())
            .orderByAsc(CheckIn::getCreatedAt));
        ticket.setCheckIns(checkIns);
        return ticket;
    }

    private List<Ticket> attachCheckIns(List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return tickets;
        }
        List<String> ids = tickets.stream().map(Ticket::getId).collect(Collectors.toList());
        List<CheckIn> checkIns = checkInMapper.selectList(new LambdaQueryWrapper<CheckIn>()
            .in(CheckIn::getTicketId, ids)
            .orderByAsc(CheckIn::getCreatedAt));
        Map<String, List<CheckIn>> group = checkIns.stream()
            .collect(Collectors.groupingBy(CheckIn::getTicketId));
        for (Ticket ticket : tickets) {
            ticket.setCheckIns(group.getOrDefault(ticket.getId(), new ArrayList<>()));
        }
        return tickets;
    }

    private void hydrateTickets(List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return;
        }
        Set<String> reporterIds = tickets.stream()
            .map(Ticket::getReporterId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Set<String> locationIds = tickets.stream()
            .map(Ticket::getLocationId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<String, User> userMap = reporterIds.isEmpty()
            ? Map.of()
            : userMapper.selectBatchIds(reporterIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        Map<String, Location> locationMap = locationIds.isEmpty()
            ? Map.of()
            : locationMapper.selectBatchIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, location -> location));

        for (Ticket ticket : tickets) {
            User user = userMap.get(ticket.getReporterId());
            if (user != null) {
                ticket.setReporterName(user.getName());
                ticket.setReporterPhone(user.getPhone());
            }
            Location location = locationMap.get(ticket.getLocationId());
            if (location != null) {
                ticket.setLocationName(location.getName());
            }
        }
    }

    private void hydrateTicket(Ticket ticket, User reporter, Location location) {
        if (ticket == null) {
            return;
        }
        if (reporter != null) {
            ticket.setReporterName(reporter.getName());
            ticket.setReporterPhone(reporter.getPhone());
        }
        if (location != null) {
            ticket.setLocationName(location.getName());
        }
    }
}
