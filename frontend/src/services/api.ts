import axios from 'axios';
import { getAuth } from './auth';

export const api = axios.create({
  baseURL: '/api',
});

api.interceptors.request.use((config) => {
  const auth = getAuth();
  if (auth?.token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

export interface Ticket {
  id: string;
  title: string;
  description: string;
  images: string[];
  locationId: string;
  locationName?: string;
  status: 'pending' | 'in_progress' | 'completed';
  reporterId: string;
  reporterName?: string;
  reporterPhone?: string;
  assignedToName?: string;
  assignedToContact?: string;
  rating?: number;
  feedback?: string;
  checkIns: Array<{ id: string; technicianName: string; location: string; createdAt: string }>;
  createdAt: string;
}
