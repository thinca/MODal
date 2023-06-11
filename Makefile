build:
	docker compose build
	sbt package
.PHONY: build

install: build
	mkdir -p ./.local/data/plugins/
	cp ./target/scala-3.3.0-RC5/modal_*.jar ./.local/data/plugins/
.PHONY: install

start: install
	docker compose up -d
.PHONY: start

stop:
	docker compose down
.PHONY: stop

run: install
	docker compose up
.PHONY: run

# reload: install
# 	# You need to install PlugManX plugin
# 	docker compose exec minecraft /home/mc-user/mc/rcon -p "mc" -c /home/mc-user/mc/rcon.yaml plugman reload MODal
# .PHONY: reload
