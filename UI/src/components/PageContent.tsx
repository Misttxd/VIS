import {useEffect, useState, type ReactNode} from "react";

// První zobrazení počká na data. Další výběry už zachovají otevřené formuláře.
export function PageContent({ready, children}: {ready: boolean; children: ReactNode}) {
  const [opened, setOpened] = useState(false);
  useEffect(() => {
    if (ready) setOpened(true);
  }, [ready]);
  if (!ready && !opened) return <p role="status">Načítání…</p>;
  return children;
}
