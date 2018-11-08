# webwallet-core

This is the Iridium webwallet server implementation (the core).

## server

### Dev requirements
- go 1.11
- [go dep](https://github.com/golang/dep)
- mongodb 4.x
- node v10.2.x + npm v5.6.x
- @angular/cli v7.0.x

#### Not yet, but later for sure
- docker-compose v1.21.0+

### Running the server

To build the frontend run:
    
    npm install -g @angular/cli
    npm install
    
    ng build --aot

Start the mongodb, e.g. as a docker container:

    docker run -d --name mongo -p 27017:27017 mvertes/alpine-mongo


To start the backend just run the main.go file:

    dep ensure
    go run main.go
