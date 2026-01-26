import axios from 'axios';

export const api = axios.create({
  baseURL: 'http://localhost:3000',
});

export interface Ticket {
  id: string;
  title: string;
  description: string;
  images: string[];
  location: string;
  status: 'pending' | 'in_progress' | 'completed';
  reporterName: string;
  reporterContact: string;
  assignedToName?: string;
  assignedToContact?: string;
  rating?: number;
  feedback?: string;
  checkIns: Array<{ id: string; technicianName: string; location: string; createdAt: string }>;
  createdAt: string;
}
