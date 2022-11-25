# Házi feladat specifikáció

Információk [itt](https://viauac00.github.io/laborok/hf)

## Mobil- és webes szoftverek
### 2022. ősz
### Néma mód ütemező
### Vesztergombi András - RDREOA
### andras.vesztergombi@edu.bme.hu 
### Laborvezető: Pásztor Dániel

## Bemutatás

<!-- Az alkalmazás rövid, 2-3 mondatos bemutatása. Honnan az ötlet, mi szülte az igényt, ki lehetne a célközönség.
A laboron és előadáson bemutatott alkalmazásokat nem lehet házi feladatnak választani. -->
Szeretem az Android azon funkcióját, hogy le lehet némítani adott időre a telefont. Az operációs rendszer biztosította lehetőség (legalábbis MIUI-n) elég korlátozott: ½, 1, 2 vagy 8 óra. Vannak alkalmazások, amik ennek kezelését végzik, de nekem nem nyerték el maradéktalanul a tetszésemet. Így csinálok egy alkalmazást, aminek egzaktul meg lehet adni, hogy 18:00-ig némítsa le a telefont, vagy hogy minden nap némítsa le 0:00 és 5:00 között.
Célközönség elsősorban én magam, de tervezem publikálni az alkalmazást a Play Áruházba.

## Főbb funkciók

<!-- Az alkalmazás minden funkciójára kiterjedő leírás (röviden, lényegre törően). Legyen egyértelműen eldönthető, hogy az adott funkció implementálva van-e!
Pl.: Az alkalmazással lehetőség van tételek felvételére és tárolására, valamint azok rendezett megjelenítésére, illetve frissítésre X hálózati hívás segítségével. -->
Lehessen beállítani, hogy hány óráig legyen lenémítva a telefon (pl. 20:33-ig), addig legyen néma, utána legyen hangos. Lehessen időközben is feloldani a némítást az alkalmazásban.
Lehessen beállítani, hogy a hét adott napjain adott időközökben legyen lenémítva a telefon.

## Választott technológiák:

<!--- (UI)
- (fragmentek)
- (RecyclerView)
- (Perzisztens adattárolás)-->

- UI
- fragmentek
- AlarmManager
- perzisztens adattárolás

# Házi feladat dokumentáció
Az alkalmazás egyik fő funkciója, hogy adott ideig lenémítható legyen a telefon. A Mute now ablakban az órán beállítható, hogy meddig legyen néma, majd a Mute until above gomb hatására néma üzemmódba kapcsol az alkalmazás. A némításról értesítés is érkezik, amiben szerepel, hogy meddig lesz néma az alkalmazás.
A beállított időben az alkalmazás feloldja a némítást, amiről szintén értesítést küld.

Az alkalmazás Recurring mute oldalán lehetőség van ismétlődő némítás beállítására. A jobb alsó sarokban lévő plusz gombbal tudunk új időtartamot beállítani. Minden beállított időtartamban néma lesz az alkalmazás. A periódusok szerkeszthetőek, törölhetőek. Törlés hatására visszavonjuk az ébresztőket (az AlarmManagerrel), így nem fog néma üzemmódba váltani. Szerkesztés hatására módosulnak az ébresztések is.

Az alkalmazás perzisztens módon működik. Például ha lenémítom a telefonomat az alkalmazással 18:00-ig, majd a telefonom újraindul, 18:00-kor akkor is visszaállítja hangos üzemmódba. A periódusszerű némításnál is igaz, hogy az értékek nem vesznek el újraindítás után sem. Az alkalmazás akkor is működik, ha az alkalmazáselőzményekből kitörölték.

Az alkalmazás értesítéseket külön csatornákon küld, így ezek külön-külön elrejthetőek.
A várakozás passzívan történik, így nem fogyasztja jelentősen az akkumulátort. Az időzítés AlarmManagerrel történik, így percre pontosan a kiválasztott időben következik be a némítás/visszahangosítás.
Ha a telefon kikapcsolt állapotban van, amikor némítani/visszahangosítani kéne, ezt az alkalmazás nem fogja megtenni.

Az alkalmazás beállításaiban módosítható, hogy az appban 12 vagy 24 órás formátumban jelenjen nem az óra.

Az alkalmazás a készülék nyelvétől függően angolul vagy magyarul jelenik meg.
Az alkalmazás témája a telefon világos vagy sötét téma beállítását követi.