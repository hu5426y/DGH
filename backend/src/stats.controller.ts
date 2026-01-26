import { Controller, Get } from '@nestjs/common';
import { TicketsService } from './tickets/tickets.service';

@Controller('stats')
export class StatsController {
  constructor(private readonly ticketsService: TicketsService) {}

  @Get()
  summary() {
    return this.ticketsService.stats();
  }
}
