import {PageContent} from "../components/PageContent";
import {useState} from "react";
import {api, useData} from "../api";
import {Card, Options, Quantity, Rows} from "../components/Controls";
import {Form} from "../components/Form";
import {ingredientData, IngredientFields} from "../components/IngredientFields";
import type {PageProps, RecipeDetail} from "../types";

export default function Recipes(props: PageProps) {
  const {ingredients, recipes, version, report} = props;
  const [selected, select] = useState(0);
  const [lines, setLines] = useState([0]);
  const id = selected || recipes[0]?.id;
  const detail = useData<RecipeDetail | null>(Boolean(id) && "recipes/" + id, version, report, null);

  function save(data: FormData) {
    if (!lines.length) throw new Error("Přidejte alespoň jednu surovinu.");
    const items = lines.map(key => ({
      ...ingredientData(data, ingredients, key), quantity: Number(data.get("quantity" + key))
    }));
    return api<number>("recipes", {name: data.get("name"), instructions: data.get("instructions"), servings: Number(data.get("servings")), ingredients: items});
  }
  function saved(id: number) {
    setLines([0]);
    select(id);
  }
  return <PageContent ready={!id || detail !== null}><h2>Recepty</h2><div className="grid">
    <Card title="Uložené recepty"><label>Detail receptu<select value={id || ""} onChange={e => select(Number(e.target.value))}><Options items={recipes}/></select></label>
      {detail && <><p>{detail.servings} porcí · {detail.instructions}</p>
        <Rows items={detail.ingredients.map(i => i.name + " · " + i.quantity + " " + i.unit + " · " + i.category)} empty="Žádné suroviny."/></>}
      {!id && <p>Zatím žádný recept.</p>}</Card>
    <Card title="Nový recept"><Form {...props} save={save} after={saved}>
      <label>Název<input name="name" required/></label><Quantity label="Počet porcí podle receptu" name="servings" whole/>
      <label>Postup<textarea name="instructions" required/></label>
      <fieldset><legend>Suroviny pro uvedený počet porcí</legend>{lines.map(key => <div key={key} className="line">
        <IngredientFields {...props} suffix={key}/><Quantity label="Množství" name={"quantity" + key}/>
        <button type="button" className="remove" onClick={() => setLines(lines.filter(k => k !== key))}>Odebrat surovinu</button></div>)}
        <button type="button" id="addRecipeLine" onClick={() => setLines([...lines, Math.max(0, ...lines) + 1])}>Přidat surovinu</button></fieldset>
      <button>Uložit recept</button></Form></Card>
  </div></PageContent>;
}
