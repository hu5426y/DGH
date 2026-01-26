import 'reflect-metadata';
import { ValidationPipe } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { TicketsService } from './tickets/tickets.service';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      transform: true,
    }),
  );
  app.enableCors();
  await app.listen(3000);

  const ticketsService = app.get(TicketsService);
  setInterval(async () => {
    const overdue = await ticketsService.findOverdueTickets();
    if (overdue.length) {
      console.warn(`[提醒] 当前有 ${overdue.length} 条工单超过处理时限，请及时处理。`);
    }
  }, 60 * 60 * 1000);
}

void bootstrap();
