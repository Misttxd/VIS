import {PageContent} from "../components/PageContent";
import {useState} from "react";
import {api, useData} from "../api";
import {Card, Rows} from "../components/Controls";
import {Form} from "../components/Form";
import {today} from "../date";
import type {PageProps, Plan, PlanItem, Shortage, Availability} from "../types";

function freeDay(plans: Plan[]) {
  const date = new Date(today() + "T00:00:00Z");
  while (plans.some(plan => plan.day === date.toISOString().slice(0, 10))) date.setUTCDate(date.getUTCDate() + 1);
  return date.toISOString().slice(0, 10);
}

export default function Plans(props: PageProps) {
  const {user, recipes, version, report} = props;
  const plans = useData<Plan[] | null>("plans", version, report, null);
  const ownPlans = (plans ?? []).filter(plan => plan.userId === user.id);
  const [date, setDate] = useState<string | null>(null);
  const day = date ?? freeDay(ownPlans);
  const [selected, select] = useState(0);
  const [lines, setLines] = useState([{key: 0, recipe: ""}]);
  const [busy, setBusy] = useState(false);
  const plan = plans?.find(p => p.id === selected) || plans?.[0];
  const path = plan && "plans/" + plan.id;
  const items = useData<PlanItem[] | null>(path && path + "/items", version, report, null);
  const shopping = useData<Shortage[] | null>(path && !plan.prepared && path + "/shopping", version, report, null);
  const available = useData<Availability[] | null>(day && "availability?day=" + day, version, report, null);
  const ids = lines.map(line => Number(line.recipe || recipes[0]?.id));
  let feedback = "";
  if (!day) feedback = "Vyber datum plánu.";
  else if (ownPlans.some(p => p.day === day)) feedback = "Pro tento den už máš plán.";
  else if (!lines.length || !recipes.length) feedback = "Přidej alespoň jedno jídlo.";
  else if (new Set(ids).size !== ids.length) feedback = "Stejné jídlo je v plánu dvakrát.";

  function save(data: FormData) {
    const meals = lines.map((line, index) => ({recipeId: ids[index], portions: Number(data.get("portions" + line.key))}));
    return api<number>("plans", {day, items: meals});
  }
  function saved(id: number) {
    select(id);
    setDate(null);
    setLines([{key: 0, recipe: ""}]);
  }
  async function prepare() {
    setBusy(true);
    try {
      await api(path + "/prepare", {});
      props.refresh();
      report("Příprava potvrzena. Suroviny byly odepsány ze skladu.");
    } catch (error) {
      report(String(error instanceof Error && error.message || error));
    } finally {
      setBusy(false);
    }
  }
  function changeRecipe(key: number, recipe: string) {
    setLines(lines.map(line => {
      if (line.key === key) return {key, recipe};
      return line;
    }));
  }

  let plansTitle = "Moje plány";
  if (user.role === "ADMIN") plansTitle = "Všechny plány";

  function planLabel(plan: Plan) {
    let label = plan.day;
    if (user.role === "ADMIN") {
      if (plan.userId === user.id) label += " · můj plán";
      else label += " · jiný uživatel";
    }
    return label;
  }
  return <PageContent ready={plans !== null && available !== null && (!plan || (items !== null && (plan.prepared || shopping !== null)))}><h2>Plánování jídel</h2><div className="grid">
    <Card title="Nový denní plán"><Form {...props} save={save} after={saved}>
      <label>Datum plánu<input type="date" required value={day} onChange={e => setDate(e.target.value)}/></label>
      <fieldset><legend>Jídla a porce</legend>{lines.map(line => <div className="line" key={line.key}>
        <label>Recept<select required value={line.recipe || recipes[0]?.id || ""} onChange={e => changeRecipe(line.key, e.target.value)}>
          {recipes.map(r => {
            const info = available?.find(a => a.recipeId === r.id);
            let label = r.name;
            if (info) label += " · dostupné porce: " + info.portions;
            return <option key={r.id} value={r.id}>{label}</option>;
          })}</select></label>
        <label>Plánovaný počet porcí<input name={"portions" + line.key} type="number" min="1" defaultValue="1" required/></label>
        <button className="remove" type="button" onClick={() => setLines(lines.filter(l => l.key !== line.key))}>Odebrat jídlo</button></div>)}
        <button id="addPlanLine" type="button" onClick={() => setLines([...lines, {key: (lines.at(-1)?.key || 0) + 1, recipe: ""}])}>Přidat jídlo</button></fieldset>
      {feedback && <p>{feedback}</p>}<button disabled={!!feedback}>Uložit denní plán</button></Form></Card>
    <Card title="Dostupné porce podle receptu"><p>{day}</p><Rows items={(available ?? []).map(i => {
      let text = i.name + " · dostupné porce: " + i.portions;
      if (i.useSoon) text += " · doporučeno kvůli expiraci";
      return text;
    })} empty="Žádné dostupné údaje."/></Card>
    <Card title={plansTitle}><label>Den plánu<select value={plan?.id || ""} onChange={e => select(Number(e.target.value))}>
      {(plans ?? []).map(p => <option key={p.id} value={p.id}>{planLabel(p)}</option>)}</select></label>
      {plan && !plan.prepared && <p>Naplánováno · zásoby zatím nejsou odepsané ani rezervované.</p>}
      {plan?.prepared && <p>Připraveno · suroviny byly odepsány.</p>}
      <Rows items={(items ?? []).map(i => recipes.find(r => r.id === i.recipeId)?.name + " · " + i.portions + " porcí")} empty="Žádná jídla v plánu."/>
      <p>Při přípravě se nejdřív odepíšou zásoby s nejbližší expirací platné k dnešnímu dni.</p>
      <button disabled={!plan || plan.prepared || busy} onClick={prepare}>Potvrdit přípravu a odepsat zásoby</button></Card>
    <Card title="Nákupní seznam pro vybraný plán">
      {!plan && <p>Žádný uložený plán.</p>}{plan?.prepared && <p>Plán už je připravený.</p>}
      {plan && !plan.prepared && shopping === null && <p>Čeká se na nákupní seznam.</p>}
      {plan && !plan.prepared && shopping && <Rows items={shopping.map(i => i.name + " · " + i.missing + " " + i.unit)} empty="Zásoby pokrývají celý plán."/>}</Card>
  </div></PageContent>;
}
