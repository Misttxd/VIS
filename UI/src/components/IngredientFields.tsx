import {useState} from "react";
import type {Ingredient, NamedItem} from "../types";
import {Options} from "./Controls";

export function ingredientData(data: FormData, ingredients: Ingredient[], suffix: string | number = "") {
  const name = String(data.get("ingredient" + suffix) ?? "").trim();
  const known = ingredients.find(i => i.name.toLocaleLowerCase("cs") === name.toLocaleLowerCase("cs"));
  if (known) return {ingredientId: known.id};
  return {name, categoryId: Number(data.get("category" + suffix)), unit: data.get("unit" + suffix)};
}

export function IngredientFields({ingredients, categories, suffix = ""}: {ingredients: Ingredient[]; categories: NamedItem[]; suffix?: string | number}) {
  const [name, setName] = useState("");
  const known = ingredients.find(i => i.name.toLocaleLowerCase("cs") === name.trim().toLocaleLowerCase("cs"));
  const disabled = !!known || !name.trim();
  return <>
    <label className="wide">Surovina<input list="names" name={"ingredient" + suffix} value={name}
      onChange={e => setName(e.target.value)} placeholder="Název stávající nebo nové suroviny" required /></label>
    <label>Kategorie<select key={known?.id || "new"} name={"category" + suffix} defaultValue={known?.categoryId || ""} disabled={disabled} required>
      <option value="">Bez výběru</option><Options items={categories}/></select></label>
    <label>Jednotka<select key={known?.unit || "new"} name={"unit" + suffix} defaultValue={known?.unit || ""} disabled={disabled} required>
      <option value="">Bez výběru</option><option>g</option><option>ml</option><option>ks</option></select></label>
  </>;
}
