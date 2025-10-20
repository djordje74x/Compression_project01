# Analiza i kompresija binarnih fajlova

Ovaj projekat implementira **analizu i kompresiju binarnih fajlova**. Cilj je demonstracija različitih tehnika kompresije i izračunavanje entropije bajtova u fajlovima.

---

## Funkcionalnosti

### 1. Izračunavanje entropije bajtova
- Analiza učestalosti pojavljivanja svakog bajta (0–255) u fajlu.
- Računanje verovatnoće bajta:

    pi = Ni / N
gde je `Ni` broj pojavljivanja bajta `i`, a `N` ukupna veličina fajla u bajtovima.

- Izračunavanje entropije fajla:

    H(p) = - Σ pi * log2(pi)
(pretpostavlja se da je 0 * log2(0) = 0).

---

### 2. Shannon-Fano i Huffman kodiranje
- Generisanje kodova prema frekvenciji bajtova.
- Kodiranje fajlova korišćenjem generisanih kodova.
- Čuvanje koda i kodiranih podataka za kasniju dekompresiju.

---

### 3. LZ77 i LZW kompresija
- Implementacija algoritama sa rečnikom.
- Kompresija fajlova sa simbolima `0–255`.
- Efikasno skladištenje kodiranih fajlova.

---

### 4. Efikasno rukovanje podacima
- Izlazni fajlovi optimizovani za minimalnu veličinu.
- Bit-level operacije za efikasno skladištenje podataka.

---

## Pokretanje aplikacije

### IntelliJ IDEA
1. Otvori projekat u IntelliJ IDEA.
2. Dozvoli da se preuzmu Maven zavisnosti.
3. Pokreni klasu `org.example.Main` desnim klikom → **Run 'Main'**.
