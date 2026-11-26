import {useEffect, useState} from "react";

export async function api<T = void>(path: string, body?: unknown): Promise<T> {
  let options: RequestInit = {};
  if (body !== undefined) {
    options = { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) };
  }
  const response = await fetch("/api/" + path, options);
  const text = await response.text();
  if (!response.ok) throw new Error(text || "Požadavek se nezdařil.");
  if (text) return JSON.parse(text);
  return undefined as T;
}

// Po změně výběru zahodíme případnou opožděnou odpověď předchozího dotazu.
export function useData<T>(path: string | false | undefined, version: number, report: (message: string) => void, empty: T): T {
  const [result, setResult] = useState<{path: string; data: T}>();
  useEffect(() => {
    let active = true;
    if (path) api<T>(path).then(data => {
      if (active) setResult({ path, data });
    }).catch(error => { if (active) report(error.message); });
    return () => { active = false; };
  }, [path, version, report]);
  if (path && result?.path === path) return result.data;
  return empty;
}
