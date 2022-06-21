# Szerkesztőség 📰✍

A feladatod egy online újság szerkesztőség backendjének kidolgozása.
A cikkeket és a hozzájuk tartozó kommenteket relációs adatbázisban kell tárolni.

Az automata, integrációs teszteseteket készen kapod.
Ezek futtatásához először létre kell hoznod a modelleket (**fontos**, hogy a megadott package-ben hozd őket létre!),
majd implementálnod kell a meghatározott API végpontokat.

A feladat megoldását a **Clean Code** elvek betartásával, rétegekre bontva kell biztosítanod.
**Használj 3 rétegű architektúrát** (*controller, service, dao/repository* rétegek)!

A megoldáshoz csatolj konténerizációhoz szükséges fájlokat is (`Dockerfile`, futtatáshoz szükséges parancsok)!

## Modellek

- package: `com.codecool.newspaper.model`

### Category

- Enum, a következő opciókkal: `POSITIVE, NEUTRAL, NEGATIVE`

### Comment

- id: `Long` (automatikusan generált)
- text: `String`
- category: `Category`

### Article

- id: `Long` (automatikusan generált)
- title: `String`
- text: `String`
- comments: `Comment` objektumokat tartalmazó `List`

A vizsgafeladatban elég, ha a két entitás közt **1 to many** egy irányú kapcsolat van.

## Validálás

A `POST` illetve `PUT` végpontokra érkező entitásokat ellenőrizd, megfelelnek-e az alábbiaknak,
ellenkező esetben `400`-as hibával térnek vissza:

- A `Comment` osztály `text` mezője nem lehet üres
- Az `Article` osztály `text` és `title` mezője nem lehet üres

## API Végpontok
* Minden végpont JSON formátumban kommunikál.
* Minden olyan végpont, amely tömbbel válaszol, az adatbázishoz való hozzáadás sorrendjében adja vissza az adatokat,
  találat híján pedig üres tömbbel tér vissza

| HTTP kérés | erőforrás                | leírás                                                                                                                            | válasz kód |
|------------|--------------------------|-----------------------------------------------------------------------------------------------------------------------------------|------------|
| `GET`      | `/comment`               | visszaadja az adatbázisban tárolt összes kommentet                                                                                | 200        |
| `GET`      | `/comment/{id}`          | visszatér a megadott id-val rendelkező kommenttel, ha az létezik, 404-as hiba, ha nem                                             | 200, 404   |
| `POST`     | `/comment`               | a _request body_-ban kapott JSON formátumú `Comment`-ot menti le az adatbázisba, (visszatér a lementett entitással)               | 200, 400   |
| `PUT`      | `/comment/{id}`          | a _request body_-ban kapott `Comment`-ot felülírja az adatbázisban. (ha rossz `{id}`-ra küldték: 400-as hiba)                     | 200, 400   |
| `DELETE`   | `/comment/{id}`          | törli az adatbázisból a megadott azonosítójú kommentet, feltéve, hogy az nem szerepel egy `Article`-ben                           | 200        |
| `GET`      | `/article`               | visszaadja az adatbázisban tárolt összes cikket                                                                                   | 200        |
| `GET`      | `/article/{id}`          | visszaadja a megadott id-val rendelkező cikket. Ha nincs ilyen, _404-as HTTP kóddal_ válaszol (pl. `RuntimeException`).           | 200, 404   |
| `POST`     | `/article`               | _request body_-jában kapott JSON formátumú új cikket, `Article`-t menti az adatbázisba, (visszatér a lementett entitással)        | 200, 400   |
| `DELETE`   | `/article/{id}`          | törli az adatbázisból a megadott id-jú cikket (az cikkeket nem!).                                                                 | 200        |
| `GET`      | `/article/controversial` | visszaadja az adatbázisban tárolt összes olyan cikket, amelynek több mint 4 kommentje van, és a kommentjeinek többsége `NEGATIVE` | 200        |

## Docker

- Állítsd össze az alkalmazásodhoz egy `Dockerfile`-t!
- Mellékeld a `docker_build.sh` és a `docker_build.bat` parancsokban az alkalmazás docker képpé fordításához szükséges parancsokat. Az `image` név legyen `article-api`.
- Mellékeld a `docker_run.sh` és a `docker_run.bat` parancsokban a docker image futtatásához szükséges parancsot.

## Kiegészítő UML osztály diagram - model réteg

 ```mermaid
 classDiagram
 direction LR
 class Article{
    Long id
    String title
    String text
    List~Comment~ comments
  }
  class Comment{
    Long id
    String text
    Category category  
  }
  Article "1" --> "1..*" Comment

  class Category{
    <<enumeration>>
    POSITIVE
    NEUTRAL
    NEGATIVE
  }
  
  Comment --> Category: has-a
```

## Pontozás

Egy feladatra 0 pontot ér, ha:

- nem fordul le
- lefordul, de egy teszteset sem fut le sikeresen.
- ha a forráskód olvashatatlan, nem felel meg a konvencióknak, nem követi a clean code alapelveket.

0 pont adandó továbbá, ha:

- kielégíti a teszteseteket, de a szöveges követelményeknek nem felel meg

Pontokat a további működési funkciók megfelelősségének arányában kell adni a vizsgafeladatra:

- 5 pont: az adott projekt lefordul, néhány teszteset sikeresen lefut, és ezek funkcionálisan is helyesek. Azonban több
  teszteset nem fut le, és a kód is olvashatatlan.
- 10 pont: a projekt lefordul, a tesztesetek legtöbbje lefut, ezek funkcionálisan is helyesek, és a clean code elvek
  nagyrészt betartásra kerültek.
- 20 pont: ha a projekt lefordul, a tesztesetek lefutnak, funkcionálisan helyesek, és csak apróbb funkcionális vagy
  clean code hibák szerepelnek a megoldásban.

Konténerizálás feladatrész összesen 3 pont:

- 1 pont: ha megvan a Dockerfile
- +1 pont: ha megvannak az indító scriptek
- +1 pont: Docker file betartja a bevált gyakorlat elveit (best practice).

Gyakorlati pontozás a project feladatokhoz:

- Alap pontszám egy feladatra(max 20): lefutó egység tesztek száma / összes egység tesztek száma * 20, feltéve, hogy a
  megoldás a szövegben megfogalmazott feladatot valósítja meg
- 3 pont jár a docker feladat megoldásáért
- Clean kód, programozási elvek, bevett gyakorlat, kód formázás megsértéséért - pontlevonás jár. Szintén pontlevonás
  jár, ha valaki a feladatot nem a leghatékonyabb módszerrel oldja meg - amennyiben ez értelmezhető.
- Összesen szerezhető pontok száma: 23