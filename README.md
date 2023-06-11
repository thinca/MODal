# MODal - Minecraft MOD for vim-jp

MODal is a Minecraft MOD([SpigotMC](https://hub.spigotmc.org/) plugin) of vim-jp community, by vim-jp community, for vim-jp community.


## Requirement

- Scala 3
- sbt
    - https://www.scala-sbt.org/

## Build

```
$ make
```

or

```
$ sbt package
```

## Install it in local

```
$ make install
```

or

## Start local server

```
$ export MINECRAFT_OPS_PLAYER_ID=xxxxxx
$ docker-compose up
```

You can set ops player id with the envar (also be able to set by direnv).

## Features

Players can become any Mode.
Players in each Mode have various rights and advantages.

### How to switch mode

TBD

### Advantages of each Modes

#### Farmer

Reaping crops with seeds in inventory automatically sows those seeds when harvested.
It also automatically cancels the reaping of crops that cannot yet be harvested.
