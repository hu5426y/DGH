import { IsNotEmpty, IsString } from 'class-validator';

export class CheckInDto {
  @IsString()
  @IsNotEmpty()
  technicianName!: string;

  @IsString()
  @IsNotEmpty()
  location!: string;
}
