export function withBase(base: string, path: string): string {
  const cleanBase = base.replace(/\/$/, "");
  if (!path || path === "/") return `${cleanBase}/`;
  const cleanPath = path.startsWith("/") ? path : `/${path}`;
  return `${cleanBase}${cleanPath}`;
}
