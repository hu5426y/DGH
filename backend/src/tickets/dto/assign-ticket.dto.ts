import { IsNotEmpty, IsOptional, IsString } from 'class-validator';

export class AssignTicketDto {
  @IsString()
  @IsNotEmpty()
  assignedToName!: string;

  @IsString()
  @IsOptional()
  assignedToContact?: string;
}
