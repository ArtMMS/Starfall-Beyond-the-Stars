# 📖 Starfall: Beyond the Stars — Player Guide

This guide explains how everything in Starfall works. It grows with the mod: each new feature gets its section here.

> **Version:** 0.0.1 (in development) · Minecraft 1.21.1 · NeoForge

---

## Table of Contents

1. [The Big Picture](#-the-big-picture)
2. [Fuel System](#-fuel-system)
3. [Crude Oil](#️-crude-oil)
4. [Oil Extractor](#-oil-extractor)
5. [Fluid Tank](#-fluid-tank)
6. [Recipes](#-recipes)
7. [Configuration](#️-configuration)
8. [Compatibility with Other Mods](#-compatibility-with-other-mods)
9. [Coming Soon](#-coming-soon)

---

## 🌌 The Big Picture

Starfall is about **exploration and fun**, not complex industry.

1. Find and extract fuel in the Overworld.
2. Store it in tanks.
3. Fuel your **rocket** and launch into space.
4. Explore planets, survive their conditions and witness rare cosmic events.

**There is no electricity in Starfall.** Machines work on their own (slowly) and can be sped up by hand.

### The rocket

The rocket is controlled from a **GUI**, through **first-person hotkeys**, or by **talking to the onboard AI** (with a voice chat mod). You do not walk around inside it.

---

## ⛽ Fuel System

Every fuel in Starfall is converted into **Fuel Points (FP)**. FP is what your rocket spends to launch and travel.

| Fuel | FP per bucket (1000 mB) | Status |
|---|---|---|
| Crude Oil | 100 | ✅ Available |
| Coal / Charcoal | — | 🔜 Planned |
| Lava | — | 🔜 Planned |
| Natural Gas | — | 🔜 Planned |

- Fluids are measured in **mB (millibuckets)**: **1 bucket = 1000 mB**.
- Crude oil does **not** need refining: it goes straight into the rocket.
- All FP values can be changed in the [configuration](#️-configuration).

---

## 🛢️ Crude Oil

A thick, black, flammable liquid found underground.

### Where to find it

- **Underground pockets** in the Overworld, between **Y -16 and Y 56**.
- About **1 pocket every 16 chunks**.
- They form inside natural stone and deepslate, always with a few blocks of rock above them.

### The deeper, the bigger

| Depth | Size | Approx. buckets | Tar on the surface? |
|---|---|---|---|
| Shallow (Y 35 and up) | Small | ~25 to 70 | Never |
| Medium (Y 15 to 34) | Medium | ~60 to 300 | Sometimes (30%) |
| Deep (below Y 15) | Large | ~300 to 800 | **Always** |

Shallow pockets are easy to reach but run out fast. The big ones are worth the dig.

### Tar Patches: the surface hint

**Large pockets** (and some medium ones) leave **Tar Patches** on the ground right above them: small, sticky black patches.

> 💡 **Found a Tar Patch? Dig straight down!** A big oil pocket is right below.

- Tar Patches slow you down when you walk on them.
- They **drop nothing** when broken.
- They only appear on dry land (not under water).

### Finding pockets with /locate

Oil pockets can be located like any structure:

```
/locate structure starfall:oil_pocket
```

It shows the coordinates of the nearest pocket (requires cheats/operator).

### How it behaves

| Property | Behavior |
|---|---|
| 🐢 **Thick** | Flows slowly and not very far, like lava |
| 🔥 **Flammable** | Catches fire from fire, lava and lightning, and **burns away** |
| 🕸️ **Sticky** | Slows down anything inside it |
| 🫧 **Heavy** | Swimming is **very slow**, and you **can drown** |
| 🪣 **Collectable** | Pick it up with a regular bucket (it becomes a bucket full of black oil) |

> ⚠️ **Never bring fire or lava near an oil pocket.** An exposed pocket can burn for a long time.

---

## ⚙️ Oil Extractor

The machine that pulls oil out of a pocket.

### How to use it

1. Find an oil pocket.
2. Place the **Oil Extractor** **at the bottom of the pocket**, touching the oil.
3. It starts working on its own.

### How it works

- **Free, but slow:** pulls **1 bucket every 10 seconds** by itself.
- **The crank:** **right-click with an empty hand** to turn the crank. Each turn adds progress (about 1 second's worth). Crank fast to extract fast. It's optional.
- **Drains from the top down:** it takes the highest oil block in the pocket first, so the pocket empties naturally.
- **Pockets run out:** each bucket extracted removes one oil block from the world. When a pocket is empty, find a new one.
- **Range:** it reaches up to **8 blocks** away through connected oil.

### Getting the oil out

The extractor has a small **internal tank (4 buckets)**. You can:

- **Right-click with an empty bucket** to take 1 bucket.
- **Place a [Fluid Tank](#-fluid-tank) touching the extractor** (any side). The extractor fills it automatically.

When the internal tank is full and nothing is taking oil out, the extractor pauses.

### Status messages

Turning the crank shows the extractor status above the hotbar:

| Message | Meaning |
|---|---|
| `Extracting... 45%` | Working. The percentage is the progress of the next bucket |
| `No crude oil nearby` | No oil connected within range: the pocket is empty, or the extractor isn't touching it |
| `Internal tank full` | Take the oil out or connect a tank |

---

## 🧪 Fluid Tank

Stores large amounts of a single fluid.

- **Capacity:** 16 buckets (16,000 mB).
- **Accepts:** water, lava and crude oil, **one fluid at a time**.
- **Right-click with a bucket:** a full bucket empties into the tank; an empty bucket fills from the tank.
- **Right-click with an empty hand:** shows what's inside, the amount and, for fuels, the **FP** value.
- Extractors next to the tank fill it automatically.

> ⚠️ **For now, breaking a tank destroys its contents.** Empty it before moving it.

---

## 🔨 Recipes

### Fluid Tank

```
[Iron] [Glass] [Iron]
[Glass] [    ] [Glass]
[Iron] [Glass] [Iron]
```

### Oil Extractor

```
[Iron] [Piston] [Iron]
[Iron] [Bucket] [Iron]
[Iron] [Iron]   [Iron]
```

---

## 🛠️ Configuration

All values can be changed in **Mods → Starfall: Beyond the Stars → Config**, or in the file `config/starfall-common.toml`.

### Fuel

| Option | Default | Description |
|---|---|---|
| `crudeOilFuelPoints` | 100 | FP per bucket of crude oil |

### Machines

| Option | Default | Description |
|---|---|---|
| `basicTankCapacity` | 16000 mB | Fluid Tank capacity |
| `extractorCapacity` | 4000 mB | Oil Extractor internal tank |
| `extractorTicksPerBucket` | 200 (10 s) | Time to extract 1 bucket without cranking |
| `extractorCrankBoost` | 20 (1 s) | Progress added by each crank turn |
| `extractorRange` | 8 blocks | How far the extractor reaches into a pocket |

### World generation (modpacks / datapacks)

Oil pockets are a structure, so modpacks can change them with a datapack:

- `data/starfall/worldgen/structure_set/oil_pockets.json`: how often they spawn (`spacing` and `separation`, in chunks; default 4 and 2).
- `data/starfall/worldgen/structure/oil_pocket.json`: which biomes get pockets (default: all Overworld biomes).

---

## 🔌 Compatibility with Other Mods

- **Crude oil uses the common tag `c:crude_oil`.** Oil from other mods works in Starfall machines, and Starfall oil works in theirs.
- **Tanks and extractors expose the standard NeoForge fluid handler,** so pipes and machines from other mods can connect to them.
- Fluids are measured in **mB**, like in every other mod.

---

## 🔜 Coming Soon

- **Pipes** to move fluids between extractors and tanks.
- **Coal, lava and natural gas** as fuels.
- **Tank tiers:** basic, reinforced and industrial.
- **Machine GUIs** with fluid levels and progress.
- **The rocket:** refueling, launch costs and its control GUI.