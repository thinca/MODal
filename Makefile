build:
	sbt package
.PHONY: build

install: build
	mkdir -p ./.local/data/plugins/
	cp ./target/scala-3.3.0-RC5/modal_*.jar ./.local/data/plugins/
.PHONY: install

start: install
	docker compose up -d
.PHONY: start

reload: install
	# You need to install PlugManX plugin
	docker compose exec minecraft rcon-cli plugman reload MODal
.PHONY: reload
