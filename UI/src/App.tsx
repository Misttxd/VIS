import {useEffect, useState} from "react";
import {flushSync} from "react-dom";
import {useReducedMotion} from "motion/react";
import type {Ingredient, NamedItem, Recipe, User} from "./types";
import Stock from "./pages/Stock";
import Catalog from "./pages/Catalog";
import Recipes from "./pages/Recipes";
import Plans from "./pages/Plans";
import Login from "./pages/Login";
import {api, useData} from "./api";


const tabs = [
  {id: "stock", name: "Sklad", screen: Stock},
  {id: "catalogSection", name: "Suroviny", screen: Catalog},
  {id: "recipes", name: "Recepty", screen: Recipes},
  {id: "plans", name: "Plánování", screen: Plans}
];

function currentTab() {
  return tabs.find(tab => "#" + tab.id === location.hash) || tabs[0];
}
function Workspace({user, onLogout}: {
  user: User;
  onLogout: () => void;
}) {
  const reducedMotion = useReducedMotion();
  const [active, setActive] = useState(currentTab);
  const [message, report] = useState("");
  const [version, setVersion] = useState(0);
  useEffect(() => {
    if (message !== "Uloženo") return;
    const timeout = window.setTimeout(() => report(""), 3000);
    return () => window.clearTimeout(timeout);
  }, [message, version]);

  const ingredients = useData<Ingredient[] | null>("ingredients", version, report, null);
  const categories = useData<NamedItem[] | null>("categories", version, report, null);
  const recipes = useData<Recipe[] | null>("recipes", version, report, null);
  useEffect(() => {
    let transition: ViewTransition | undefined;
    function navigate() {
      const update = () => {
        flushSync(() => {
          setActive(currentTab());
          report("");
        });
        window.scrollTo(0, 0);
      };
      transition?.skipTransition();
      if (reducedMotion || !document.startViewTransition) {
        update();
        return;
      }
      transition = document.startViewTransition(update);
    }
    window.addEventListener("hashchange", navigate);
    return () => {
      window.removeEventListener("hashchange", navigate);
      transition?.skipTransition();
    };
  }, [reducedMotion]);
  const ready = ingredients !== null && categories !== null && recipes !== null;
  const props = {user, ingredients: ingredients ?? [], categories: categories ?? [], recipes: recipes ?? [], version, report, refresh: () => setVersion(v => v + 1)};

  async function logout() {
    try {
      await api("auth/logout", {});
      onLogout();
    } catch {
      report("Odhlášení se nezdařilo.");
    }
  }

  return <><header><h1>XXXX</h1><nav aria-label="Hlavní navigace">{tabs.map(tab => {
    let current: "page" | undefined;
    if (tab === active) current = "page";
    return <a key={tab.id} href={"#" + tab.id} aria-current={current}>{tab.name}</a>;
  })}</nav>
    <div className="account">
      <span>{user.username}{user.role === "ADMIN" && " · Administrátor"}</span>
      <button onClick={logout}>Odhlásit se</button>
    </div>
  </header>
    <p role="status" id="message">{message}</p><main className="page-stack">
      {!ready && !message && <p role="status">Načítání…</p>}
      {ready && tabs.map(tab => (
        <div key={tab.id} hidden={tab !== active}>
          <tab.screen {...props}/>
        </div>
      ))}
    </main>
    <datalist id="names">{props.ingredients.map(i => <option key={i.id} value={i.name}/>)}</datalist></>;
}

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api<User | undefined>("auth/current")
      .then(current => setUser(current ?? null))
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <p role="status">Načítání…</p>;
  }
  if (!user) {
    return <Login onLogin={setUser}/>;
  }
  return <Workspace key={user.id} user={user} onLogout={() => setUser(null)}/>;
}
