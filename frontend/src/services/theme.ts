export type ThemeMode = 'manual' | 'auto';
export type ThemeName = 'day' | 'night';

const MODE_KEY = 'dgh_theme_mode';
const MANUAL_KEY = 'dgh_theme_manual';
const OVERRIDE_KEY = 'dgh_theme_override';
const LEGACY_KEY = 'dgh-theme';

const normalizeTheme = (value: string | null | undefined): ThemeName => {
  return value === 'night' ? 'night' : 'day';
};

export const initThemeStorage = () => {
  const existingMode = localStorage.getItem(MODE_KEY);
  if (!existingMode) {
    const legacy = localStorage.getItem(LEGACY_KEY);
    if (legacy) {
      localStorage.setItem(MODE_KEY, 'manual');
      localStorage.setItem(MANUAL_KEY, normalizeTheme(legacy));
    }
  }
};

export const getThemeMode = (): ThemeMode => {
  return localStorage.getItem(MODE_KEY) === 'auto' ? 'auto' : 'manual';
};

export const setThemeMode = (mode: ThemeMode) => {
  localStorage.setItem(MODE_KEY, mode);
};

export const getManualTheme = (): ThemeName => {
  const stored = localStorage.getItem(MANUAL_KEY);
  if (stored) {
    return normalizeTheme(stored);
  }
  return normalizeTheme(localStorage.getItem(LEGACY_KEY));
};

export const setManualTheme = (theme: ThemeName) => {
  localStorage.setItem(MANUAL_KEY, theme);
};

export const getThemeOverride = (): ThemeName | null => {
  const stored = localStorage.getItem(OVERRIDE_KEY);
  if (stored === 'day' || stored === 'night') {
    return stored;
  }
  return null;
};

export const setThemeOverride = (theme: ThemeName | null) => {
  if (theme) {
    localStorage.setItem(OVERRIDE_KEY, theme);
  } else {
    localStorage.removeItem(OVERRIDE_KEY);
  }
};

export const emitThemeUpdate = () => {
  window.dispatchEvent(new Event('dgh-theme-updated'));
};
