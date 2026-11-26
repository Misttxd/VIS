import type {ReactNode} from "react";
import type {NamedItem} from "../types";

export function Card({title, children, extra = ""}: {title: string; children: ReactNode; extra?: string}) {
  return <article className={"card " + extra}><h3>{title}</h3>{children}</article>;
}

export function Rows({items, empty}: {items: string[]; empty: string}) {
  if (!items.length) return <p>{empty}</p>;
  return <ul>{items.map((text, index) => <li key={index}>{text}</li>)}</ul>;
}

export function Options({items}: {items: NamedItem[]}) {
  return items.map(item => <option key={item.id} value={item.id}>{item.name}</option>);
}

export function Quantity({label, name, whole = false}: {label: string; name: string; whole?: boolean}) {
  let step = "0.001";
  if (whole) step = "1";
  return <label>{label}<input type="number" name={name} min={step} step={step} required /></label>;
}
