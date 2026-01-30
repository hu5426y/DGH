import { api } from './api';

export interface AreaLead {
  area: string;
  ticketCount: number;
  suggestedLead: string;
}

export interface TechnicianPerformance {
  technicianName: string;
  totalTickets: number;
  completedTickets: number;
  averageCompletionMinutes: number;
}

export interface IssueStat {
  issue: string;
  count: number;
}

export interface OpsOverview {
  slaHours: number;
  overdueCount: number;
  slaComplianceRate: number;
  areaLeads: AreaLead[];
  technicianPerformance: TechnicianPerformance[];
  issueStats: IssueStat[];
  pendingFeedbackCount: number;
  escalatedCount: number;
}

export const fetchOpsOverview = async (): Promise<OpsOverview> => {
  const response = await api.get('/ops/overview');
  return response.data;
};
