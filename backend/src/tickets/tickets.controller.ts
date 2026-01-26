import { Body, Controller, Get, Param, Patch, Post } from '@nestjs/common';
import { AssignTicketDto } from './dto/assign-ticket.dto';
import { CheckInDto } from './dto/checkin.dto';
import { CreateTicketDto } from './dto/create-ticket.dto';
import { FeedbackDto } from './dto/feedback.dto';
import { UpdateStatusDto } from './dto/update-status.dto';
import { TicketsService } from './tickets.service';

@Controller('tickets')
export class TicketsController {
  constructor(private readonly ticketsService: TicketsService) {}

  @Post()
  create(@Body() payload: CreateTicketDto) {
    return this.ticketsService.createTicket(payload);
  }

  @Get()
  list() {
    return this.ticketsService.listTickets();
  }

  @Get(':id')
  detail(@Param('id') id: string) {
    return this.ticketsService.getTicket(id);
  }

  @Patch(':id/status')
  updateStatus(@Param('id') id: string, @Body() payload: UpdateStatusDto) {
    return this.ticketsService.updateStatus(id, payload);
  }

  @Patch(':id/assign')
  assign(@Param('id') id: string, @Body() payload: AssignTicketDto) {
    return this.ticketsService.assignTicket(id, payload);
  }

  @Post(':id/checkins')
  checkIn(@Param('id') id: string, @Body() payload: CheckInDto) {
    return this.ticketsService.checkIn(id, payload);
  }

  @Post(':id/feedback')
  feedback(@Param('id') id: string, @Body() payload: FeedbackDto) {
    return this.ticketsService.submitFeedback(id, payload);
  }

  @Get('overdue/list')
  overdue() {
    return this.ticketsService.findOverdueTickets();
  }
}
