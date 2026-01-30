import { api } from './api';

export interface InsightSnapshot {
  highFrequencyArea: string;
  averageResponseMinutes: number;
  nightShiftPending: number;
}

export const fetchInsightSnapshot = async (): Promise<InsightSnapshot> => {
  const response = await api.get('/stats/insights/summary');
  return response.data;
};
