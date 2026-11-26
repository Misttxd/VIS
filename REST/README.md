# Kuchyně

Školní informační systém pro řízení zásob a plánování jídel v gastronomickém provozu. Backend ve složce `REST` používá Javu 21, Spring Boot, JDBC a PostgreSQL. Frontend ve vedlejší složce `UI` používá React, TypeScript a Vite.

## Spuštění

Ve složce `REST` se zapnutým Dockerem spusť:

```sh
docker compose up -d
sh ./mvnw spring-boot:run
```

Ve druhém terminálu spusť frontend ze složky `UI`:

```sh
npm install
npm run dev
```

Otevři http://localhost:5173. Přihlášení je `admin/admin` nebo `user/user`. Java API běží na portu 8081. Ve Windows použij `mvnw.cmd spring-boot:run`. Databáze běží na portu 5434 a má vlastní Docker volume. Nastavení připojení je v `src/main/resources/application.properties`. Compose název `gastro` zachovává původní databázi i po přejmenování složky. Aplikace v terminálech ukončíš pomocí Ctrl+C a databázi příkazem `docker compose stop` ze složky `REST`.

## Vyzkoušení od začátku

1. Ve **Skladu** rozbal **Kategorie a suroviny**. Vytvoř kategorii Obiloviny a do provozního katalogu přidej mouku v gramech s minimální zásobou 100 g.
2. Přijmi dodávku 300 g mouky s dnešní expirací. Ve skladu uvidíš dodávku, celkový dostupný zůstatek a upozornění na expiraci.
3. V **Receptech** založ moučné placky. Zadej postup, 4 porce a 150 g mouky. Množství suroviny platí pro všechny čtyři porce dohromady.
4. V **Plánování** pro dnešek uvidíš 8 dostupných porcí. Ulož plán na 12 porcí. V nákupním seznamu bude chybět 150 g mouky.
5. Odepiš 50 g z dodávky. Aplikace přepočítá dostupnost na 6 celých porcí a nákupní seznam na 200 g.
6. Přijmi dalších 200 g mouky s expirací zítra. U uloženého plánu stiskni **Potvrdit přípravu a odepsat zásoby**. Systém odečte 250 g z první šarže a 200 g z druhé a označí plán jako připravený. Znovu jej potvrdit nelze. Pokud zásoby nestačí, celá akce se vrátí a nic se neodepíše.

Každá surovina má jednu jednotku pro sklad i recept. Zásoba je součet zbývajících množství použitelných dodávek. Dodávka platí včetně dne expirace. Doporučení zvýrazní dostupné recepty, které využijí surovinu expirující od vybraného dne do tří dnů. Dostupnost každého jídla se počítá samostatně. Nákupní seznam počítá společnou spotřebu všech jídel konkrétního plánu. Výpočet vychází z aktuálních zůstatků. Uložení plánu zásoby nerezervuje ani neodepisuje. Teprve potvrzení skutečné přípravy odečte suroviny z konkrétních šarží v pořadí podle expirace; použitelnost posuzuje podle dne potvrzení. Potvrzuje se celý plán najednou. Ruční odpis ve skladu slouží pro jinou spotřebu nebo vyřazení zásob. Na každý den lze uložit jeden plán.

Při spuštění se také do existující tabulky `meal_plan` doplní sloupec `prepared`. Dosavadní plány zůstanou zachované jako nepotvrzené.

## Jak číst kód

Začni v `src/main/java/cz/gastro`. Při uložení receptu sleduj jednu akci od začátku do konce:

1. `UI/src/pages/Recipes.tsx` odešle jeden požadavek s celým receptem přes `UI/src/api.ts`.
2. `controller/RecipeController.java` přijme data formuláře jako `dto/RecipeRequest.java` a předá je službě.
3. `service/RecipeService.java` v metodě `create` zavolá `RecipeDao.create`, dostane ID receptu a pro každou surovinu zavolá `RecipeIngredientDao.create`. Anotace `@Transactional` drží všechny tyto zápisy v jedné transakci.
4. SQL najdeš v `dao/RecipeDao.java` a `dao/RecipeIngredientDao.java`. Strukturu tabulek najdeš v `src/main/resources/schema.sql`.

Detail receptu načítá `RecipeDao.findDetail` jedním SELECTem s JOINy přes recept, jeho položky, suroviny a kategorie. `RecipeDetailMapper` převádí řádky výsledku na modely. `RecipeService.detail` z nich sestaví jeden `RecipeDetail` se seznamem surovin. Frontend jej získá jedním požadavkem `GET /api/recipes/{id}`.

Stejný postup ukládání má `MealPlanningService.save` pro plán a jeho položky. Metoda `MealPlanningService.prepare`, volaná přes `POST /api/plans/{id}/prepare`, v jedné transakci zavolá `MealPlanDao.lockForPreparation`, sečte potřebná množství, přes `DeliveryDao.lockUsable` načte šarže podle expirace, přes `DeliveryDao.withdraw` odečte spotřebu a přes `MealPlanDao.markPrepared` potvrdí plán. Databázové zámky brání současnému dvojímu potvrzení a souběžnému použití stejných zásob. Chybí-li některá surovina, všechny odpisy se vrátí. Připravený plán už nákupní seznam nepotřebuje.

Výpočet porcí a nákupního seznamu je také v této službě. Dostupná množství počítá `StockService`.

### Factory a dvě úložiště

V katalogu surovin vybíráš mezi PostgreSQL a CSV. `CatalogService` předá volbu do `IngredientDaoFactory`. Ta vrátí objekt přes rozhraní `Dao<Ingredient>`. Obě implementace poskytují stejné operace `create` a `getAllItems`. Jejich mappery jsou ve složce `mapper`.

CSV je samostatný seznam surovin v `data/ingredients.csv`, používá výběr kategorií z aplikace. Sklad, recepty a plány pracují s provozním katalogem v PostgreSQL, kde lze v jedné transakci ukládat vazby mezi tabulkami.

