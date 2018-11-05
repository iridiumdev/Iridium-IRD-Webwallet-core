# webwallet-core

This is the Iridium webwallet server implementation (the core).

## server

### Dev requirements
- go 1.11
- docker-compose v1.21.0+
- node v10.2.x + npm v5.6.x
- @angular/cli v7.0.x

### Running the server

To build the frontend run:
    
    npm install -g @angular/cli
    npm install
    
    ng build --aot

To start the backend just run the main.go file:

    go run main.go
