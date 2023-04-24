build:
	sbt package
.PHONY: build

install: build
	mkdir -p ./.local/data/plugins/
	cp ./target/scala-3.2.0/modal_*.jar ./.local/data/plugins/
.PHONY: install

start: install
	docker compose up -d
.PHONY: start
