export interface Timer {
  id: string;
  name: string;
  durationSeconds: number;
  startedAt: number;
}

export function remainingSeconds(timer: Timer, now: number): number {
  const elapsed = Math.floor((now - timer.startedAt) / 1000);
  return timer.durationSeconds - elapsed;
}

export function isExpired(timer: Timer, now: number): boolean {
  return remainingSeconds(timer, now) <= 0;
}

export function addTimer(timers: Timer[], timer: Timer): Timer[] {
  if (timers.some((t) => t.id === timer.id)) return timers;
  return [...timers, timer];
}

export function removeTimer(timers: Timer[], id: string): Timer[] {
  return timers.filter((t) => t.id !== id);
}
