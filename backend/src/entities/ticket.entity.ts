import {
  Column,
  CreateDateColumn,
  Entity,
  OneToMany,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
} from 'typeorm';
import { CheckIn } from './checkin.entity';

export enum TicketStatus {
  Pending = 'pending',
  InProgress = 'in_progress',
  Completed = 'completed',
}

@Entity()
export class Ticket {
  @PrimaryGeneratedColumn('uuid')
  id!: string;

  @Column()
  title!: string;

  @Column('text')
  description!: string;

  @Column('text', {
    transformer: {
      to: (value: string[]) => JSON.stringify(value ?? []),
      from: (value: string) => (value ? JSON.parse(value) : []),
    },
    default: '[]',
  })
  images!: string[];

  @Column()
  location!: string;

  @Column({
    type: 'varchar',
    default: TicketStatus.Pending,
  })
  status!: TicketStatus;

  @Column()
  reporterName!: string;

  @Column()
  reporterContact!: string;

  @Column({ nullable: true })
  assignedToName?: string;

  @Column({ nullable: true })
  assignedToContact?: string;

  @Column({ type: 'int', nullable: true })
  rating?: number;

  @Column({ type: 'text', nullable: true })
  feedback?: string;

  @OneToMany(() => CheckIn, (checkIn) => checkIn.ticket, {
    cascade: true,
  })
  checkIns!: CheckIn[];

  @CreateDateColumn()
  createdAt!: Date;

  @UpdateDateColumn()
  updatedAt!: Date;
}
