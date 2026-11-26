import {useState, type FormEvent} from "react";
import {api} from "../api";
import type {User} from "../types";

export default function Login({onLogin}: {onLogin: (user: User) => void}) {
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    const data = new FormData(event.currentTarget);
    setBusy(true);
    setError("");

    try {
      const user = await api<User>("auth/login", {
        username: data.get("username"),
        password: data.get("password")
      });
      onLogin(user);
    } catch {
      setError("Přihlášení se nezdařilo. Zkontroluj jméno a heslo.");
    } finally {
      setBusy(false);
    }
  }

  return <main className="card login">
    <h1>Přihlášení</h1>
    <form onSubmit={submit}>
      <label>Uživatelské jméno
        <input name="username" autoComplete="username" required/>
      </label>
      <label>Heslo
        <input name="password" type="password"
          autoComplete="current-password" required/>
      </label>
      {error && <p role="alert">{error}</p>}
      <button disabled={busy}>Přihlásit se</button>
    </form>
  </main>;
}
