package main

import (
	"github.com/docker/docker/client"
	"github.com/gin-gonic/contrib/static"
	"github.com/gin-gonic/gin"
	"github.com/iridiumdev/webwallet-core/config"
	"github.com/iridiumdev/webwallet-core/wallet"
	log "github.com/sirupsen/logrus"
	"github.com/toorop/gin-logrus"
	"gopkg.in/mgo.v2"
)

func main() {

	log.SetFormatter(&log.TextFormatter{})

	mongoSession := initMongoClient()
	dockerClient := initDockerClient()

	initStores(mongoSession)
	initServices(dockerClient)

	initMainEngine()

	defer mongoSession.Close()
	defer dockerClient.Close()
}

func initDockerClient() *client.Client {

	log.Info("Initializing docker client")

	// TODO: daniel 10.11.18 - allow to pass a custom config, e.g. for docker swarm
	cli, err := client.NewEnvClient()
	if err != nil {
		panic(err)
	}

	log.Infof("Initialized docker client %s", cli.ClientVersion())

	return cli
}

func initMongoClient() *mgo.Session {

	log.Infof("Initializing mongodb connection to server(s): %s", config.Get().Mongo.Address)

	session, err := mgo.Dial(config.Get().Mongo.Address)
	if err != nil {
		panic(err)
	}

	info, err := session.BuildInfo()
	if err != nil {
		log.Error("Could not retrieve any build information from mongodb server!")
	} else {
		log.Infof("Initialized mongodb connection %s", info.Version)
		log.Debug(info)
	}

	// Optional. Switch the session to a monotonic behavior.
	session.SetMode(mgo.Monotonic, true)

	return session
}

func initServices(dockerClient *client.Client) {

	wallet.InitService(dockerClient)

}

func initStores(session *mgo.Session) {

	wallet.InitStore(session.DB(config.Get().Mongo.Database))

}

func initMainEngine() (*gin.Engine, *gin.RouterGroup) {

	engine := gin.Default()

	engine.Use(ginlogrus.Logger(log.StandardLogger()), gin.Recovery())

	engine.Use(static.Serve("/", static.LocalFile(config.Get().Server.StaticLocation, true)))
	api := engine.Group("/api/v1")

	initDependencyTree(api)

	engine.Run(config.Get().Server.Address)
	return engine, api
}

func initDependencyTree(api *gin.RouterGroup) {
	walletController := wallet.NewController(api)
	walletController.Routes()
}
