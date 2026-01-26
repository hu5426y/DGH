import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CheckIn } from './entities/checkin.entity';
import { Ticket } from './entities/ticket.entity';
import { StatsController } from './stats.controller';
import { TicketsModule } from './tickets/tickets.module';

@Module({
  imports: [
    TypeOrmModule.forRoot({
      type: 'sqlite',
      database: 'data.sqlite',
      entities: [Ticket, CheckIn],
      synchronize: true,
    }),
    TicketsModule,
  ],
  controllers: [StatsController],
})
export class AppModule {}
