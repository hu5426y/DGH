import { ref } from 'vue';

export type RoleCode = 'USER' | 'REPAIRER' | 'ADMIN';

export interface AuthState {
  token: string;
  id: string;
  userId: string;
  name: string;
  phone: string;
  roleCode: RoleCode;
}

const AUTH_KEY = 'dgh_auth_state';

const loadAuth = (): AuthState | null => {
  const raw = localStorage.getItem(AUTH_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthState;
  } catch {
    return null;
  }
};

const authState = ref<AuthState | null>(loadAuth());

export const getAuth = (): AuthState | null => authState.value;

export const setAuth = (state: AuthState) => {
  authState.value = state;
  localStorage.setItem(AUTH_KEY, JSON.stringify(state));
};

export const clearAuth = () => {
  authState.value = null;
  localStorage.removeItem(AUTH_KEY);
};

export const isLoggedIn = () => !!authState.value;
