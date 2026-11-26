import {PageContent} from "../components/PageContent";
import {api, useData} from "../api";
import {Card, Quantity, Rows} from "../components/Controls";
import {Form} from "../components/Form";
import {ingredientData, IngredientFields} from "../components/IngredientFields";
import {today} from "../date";
import type {PageProps, StockOverview, Delivery} from "../types";

export default function Stock(props: PageProps) {
  const {ingredients, version, report} = props;
  const stock = useData<StockOverview | null>("stock/overview?day=" + today(), version, report, null);
  const expiring = useData<Delivery[] | null>("expiring?day=" + today(), version, report, null);
  const deliveries = stock?.deliveries || [];
  function label(row: Delivery) {
    const item = ingredients.find(i => i.id === row.ingredientId);
    return "#" + row.id + " " + (item?.name || "") + " · " + row.remaining + " " + (item?.unit || "") + " · " + row.expires;
  }
  function receive(data: FormData) {
    return api("deliveries", {...ingredientData(data, ingredients), remaining: Number(data.get("quantity")), expires: data.get("expires")});
  }
  function withdraw(data: FormData) {
    return api("deliveries/" + data.get("delivery") + "/withdraw?quantity=" + data.get("quantity"), {});
  }
  return <PageContent ready={stock !== null && expiring !== null}><h2>Sklad</h2><div className="grid">
    <Card title="Spotřebovat do tří dnů" extra={expiring?.length && "attention" || ""}><Rows items={(expiring ?? []).map(label)} empty="V nejbližších třech dnech nic neexpiruje."/></Card>
    <Card title="Docházející suroviny" extra={stock?.low?.length && "attention" || ""}><Rows items={(stock?.low || []).map(i => i.name + " · " + i.available + " " + i.unit)} empty="Žádná z dostupných surovin nedochází."/></Card>
    <Card title="Příjem na sklad"><Form {...props} save={receive} id="deliveryForm">
      <IngredientFields {...props}/><Quantity label="Množství" name="quantity"/><label>Expirace<input name="expires" type="date" required/></label><button className="wide">Přidat na sklad</button>
    </Form></Card>
    <Card title="Odpis ze skladu"><Form {...props} save={withdraw}><label>Skladová položka<select required name="delivery">{deliveries.map(d => <option key={d.id} value={d.id}>{label(d)}</option>)}</select></label>
      <Quantity label="Množství k odpisu" name="quantity"/><button>Odepsat</button></Form></Card>
    <Card title="Aktuální zásoby" extra="wide"><Rows items={(stock?.available || []).map(i => i.name + " · " + i.available + " " + i.unit)} empty="Sklad je prázdný."/></Card>
    <details className="card wide"><summary>Zásoby podle expirace</summary><Rows items={deliveries.map(label)} empty="Sklad je prázdný."/></details>
  </div></PageContent>;
}
