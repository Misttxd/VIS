import {PageContent} from "../components/PageContent";
import {useState} from "react";
import {api, useData} from "../api";
import {Options} from "../components/Controls";
import {Form} from "../components/Form";
import type {PageProps, Ingredient} from "../types";

export default function Catalog(props: PageProps) {
  const {categories, version, report} = props;
  const [storage, setStorage] = useState("postgres");
  const path = "ingredients?storage=" + storage;
  const items = useData<Ingredient[] | null>(path, version, report, null);
  function add(data: FormData) {
    return api(path, {name: data.get("name"), categoryId: Number(data.get("category")), unit: data.get("unit"), minimum: Number(data.get("minimum"))});
  }
  return <PageContent ready={items !== null}><section className="catalog-section"><h2>Katalog surovin</h2><p>Přehled surovin podle kategorií a jejich měrných jednotek.</p><div className="grid">
    <article className="card wide"><label>Otevřený seznam<select value={storage} onChange={e => setStorage(e.target.value)}>
      <option value="postgres">Katalog kuchyně</option><option value="csv">Samostatný seznam (CSV)</option></select></label>
      <div id="catalog">{items?.length === 0 && <p>V tomto seznamu zatím nejsou žádné suroviny.</p>}
        {categories.map(c => {
          const names = (items ?? []).filter(i => i.categoryId === c.id).map(i => i.name + " (" + i.unit + ")");
          if (names.length) return <div key={c.id}><strong>{c.name}</strong><p>{names.join(", ")}</p></div>;
        })}</div></article>
    <details className="card"><summary>Přidat surovinu do seznamu</summary><Form {...props} save={add}>
      <label>Název suroviny<input name="name" required/></label><label>Kategorie<select name="category" required><Options items={categories}/></select></label>
      <label>Jednotka<select name="unit"><option>g</option><option>ml</option><option>ks</option></select></label>
      <label>Minimální zásoba<input name="minimum" type="number" min="0" step="any" defaultValue="0" required/></label><button>Uložit surovinu</button></Form></details>
    <details className="card"><summary>Vytvořit kategorii</summary><Form {...props} save={d => api("categories", {name: d.get("name")})}>
      <label>Název kategorie<input name="name" required/></label><button>Uložit kategorii</button></Form></details>
  </div></section></PageContent>;
}
