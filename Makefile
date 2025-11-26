.PHONY: all build run stop clean test

all: build

build:
	mvn clean package -DskipTests

run:
	docker-compose up --build

run-db:
	docker-compose up db -d

stop:
	docker-compose down

clean:
	mvn clean
	docker-compose down -v

test:
	mvn test

test-unit:
	mvn test -Dtest=DealServiceTest
