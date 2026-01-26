import { Column, CreateDateColumn, Entity, ManyToOne, PrimaryGeneratedColumn } from 'typeorm';
import { Ticket } from './ticket.entity';

@Entity()
export class CheckIn {
  @PrimaryGeneratedColumn('uuid')
  id!: string;

  @Column()
  technicianName!: string;

  @Column()
  location!: string;

  @ManyToOne(() => Ticket, (ticket) => ticket.checkIns)
  ticket!: Ticket;

  @CreateDateColumn()
  createdAt!: Date;
}
