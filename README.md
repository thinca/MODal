# MODal - Minecraft MOD for vim-jp

MODal is a Minecraft MOD([SpigotMC](https://hub.spigotmc.org/) plugin) of vim-jp community, by vim-jp community, for vim-jp community.


## Requirement

- Scala 3
- sbt
    - https://www.scala-sbt.org/
- (OPTIONAL) PlugManX: https://github.com/TheBlackEntity/PlugMan/
    1. Download it (from Spigot portal site)
    2. Expand it to .local/data/plugins/.jar

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

TBD
