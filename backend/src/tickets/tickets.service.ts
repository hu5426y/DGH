import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CheckIn } from '../entities/checkin.entity';
import { Ticket, TicketStatus } from '../entities/ticket.entity';
import { AssignTicketDto } from './dto/assign-ticket.dto';
import { CheckInDto } from './dto/checkin.dto';
import { CreateTicketDto } from './dto/create-ticket.dto';
import { FeedbackDto } from './dto/feedback.dto';
import { UpdateStatusDto } from './dto/update-status.dto';

@Injectable()
export class TicketsService {
  constructor(
    @InjectRepository(Ticket)
    private readonly ticketsRepository: Repository<Ticket>,
    @InjectRepository(CheckIn)
    private readonly checkinsRepository: Repository<CheckIn>,
  ) {}

  async createTicket(payload: CreateTicketDto): Promise<Ticket> {
    const autoAssignName = process.env.AUTO_ASSIGN_NAME;
    const autoAssignContact = process.env.AUTO_ASSIGN_CONTACT;
    const ticket = this.ticketsRepository.create({
      ...payload,
      images: payload.images ?? [],
      status: autoAssignName ? TicketStatus.InProgress : TicketStatus.Pending,
      assignedToName: autoAssignName ?? undefined,
      assignedToContact: autoAssignContact ?? undefined,
    });
    return this.ticketsRepository.save(ticket);
  }

  async listTickets(): Promise<Ticket[]> {
    return this.ticketsRepository.find({
      relations: ['checkIns'],
      order: { createdAt: 'DESC' },
    });
  }

  async getTicket(id: string): Promise<Ticket> {
    const ticket = await this.ticketsRepository.findOne({
      where: { id },
      relations: ['checkIns'],
    });
    if (!ticket) {
      throw new NotFoundException('工单不存在');
    }
    return ticket;
  }

  async updateStatus(id: string, payload: UpdateStatusDto): Promise<Ticket> {
    const ticket = await this.getTicket(id);
    ticket.status = payload.status;
    return this.ticketsRepository.save(ticket);
  }

  async assignTicket(id: string, payload: AssignTicketDto): Promise<Ticket> {
    const ticket = await this.getTicket(id);
    ticket.assignedToName = payload.assignedToName;
    ticket.assignedToContact = payload.assignedToContact;
    if (ticket.status === TicketStatus.Pending) {
      ticket.status = TicketStatus.InProgress;
    }
    return this.ticketsRepository.save(ticket);
  }

  async checkIn(id: string, payload: CheckInDto): Promise<CheckIn> {
    const ticket = await this.getTicket(id);
    const checkIn = this.checkinsRepository.create({
      technicianName: payload.technicianName,
      location: payload.location,
      ticket,
    });
    return this.checkinsRepository.save(checkIn);
  }

  async submitFeedback(id: string, payload: FeedbackDto): Promise<Ticket> {
    const ticket = await this.getTicket(id);
    ticket.rating = payload.rating;
    ticket.feedback = payload.feedback;
    return this.ticketsRepository.save(ticket);
  }

  async stats() {
    const tickets = await this.listTickets();
    const total = tickets.length;
    const completed = tickets.filter((ticket) => ticket.status === TicketStatus.Completed).length;
    const avgRating =
      tickets.filter((ticket) => ticket.rating).reduce((sum, ticket) => sum + (ticket.rating ?? 0), 0) /
      (tickets.filter((ticket) => ticket.rating).length || 1);

    const frequentLocations = tickets.reduce<Record<string, number>>((acc, ticket) => {
      acc[ticket.location] = (acc[ticket.location] ?? 0) + 1;
      return acc;
    }, {});

    return {
      total,
      completed,
      avgRating: Number(avgRating.toFixed(2)),
      frequentLocations,
    };
  }

  async findOverdueTickets(hours = 24): Promise<Ticket[]> {
    const deadline = new Date();
    deadline.setHours(deadline.getHours() - hours);
    return this.ticketsRepository
      .createQueryBuilder('ticket')
      .where('ticket.status != :status', { status: TicketStatus.Completed })
      .andWhere('ticket.createdAt <= :deadline', { deadline: deadline.toISOString() })
      .getMany();
  }
}
