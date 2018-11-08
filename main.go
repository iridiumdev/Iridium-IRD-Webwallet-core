package main

import (
	"context"
	"fmt"
	"github.com/docker/docker/api/types"
	"github.com/docker/docker/client"
	"github.com/gin-gonic/contrib/static"
	"github.com/gin-gonic/gin"
	"github.com/iridiumdev/webwallet-core/config"
	"github.com/iridiumdev/webwallet-core/wallet"
	"gopkg.in/mgo.v2"
)

func main() {

	session, err := mgo.Dial(config.Get().Mongo.Address)
	if err != nil {
		panic(err)
	}

	// TODO: daniel 08.11.18 - remove this
	fooDocker()

	initStores(session)

	initMainEngine()

	defer session.Close()
}

func initStores(session *mgo.Session) {

	// Optional. Switch the session to a monotonic behavior.
	session.SetMode(mgo.Monotonic, true)

	wallet.InitStore(session.DB(config.Get().Mongo.Database))

}

func initMainEngine() (*gin.Engine, *gin.RouterGroup) {

	engine := gin.Default()
	engine.Use(static.Serve("/", static.LocalFile(config.Get().Server.StaticLocation, true)))
	api := engine.Group("/api/v1")

	// TODO: daniel 08.11.18 - build routes and setup dependency graph here
	initDependencyTree(api)

	engine.Run(config.Get().Server.Address)
	return engine, api
}

func initDependencyTree(api *gin.RouterGroup) {
	walletController := wallet.NewController(api)
	walletController.Routes()
}

// TODO: daniel 08.11.18 - remove this
func fooDocker() {
	cli, err := client.NewEnvClient()
	if err != nil {
		panic(err)
	}

	containers, err := cli.ContainerList(context.Background(), types.ContainerListOptions{})
	if err != nil {
		panic(err)
	}

	for _, container := range containers {
		fmt.Printf("%s %s\n", container.ID[:10], container.Image)
	}

}
