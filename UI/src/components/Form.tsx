import {useState, type ReactNode, type FormEvent} from "react";
import type {PageProps} from "../types";

interface FormProps<T> extends Pick<PageProps, "refresh" | "report"> {
  save: (data: FormData) => Promise<T>;
  after?: (result: T) => void;
  children: ReactNode;
  id?: string;
}

export function Form<T>({save, report, refresh, children, id, after}: FormProps<T>) {
  const [busy, setBusy] = useState(false);
  const [reset, setReset] = useState(0);
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    try {
      const data = new FormData(event.currentTarget);
      const result = await save(data);
      setReset(n => n + 1);
      if (after) after(result);
      refresh();
      report("Uloženo");
    } catch (error) {
      report(String(error instanceof Error && error.message || error));
    } finally {
      setBusy(false);
    }
  }
  return <form key={reset} id={id} onSubmit={submit}><fieldset className="form-body" disabled={busy}>{children}</fieldset></form>;
}
