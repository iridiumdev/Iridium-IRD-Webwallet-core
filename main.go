package main

import (
	"fmt"
	"github.com/gin-gonic/contrib/static"
	"github.com/gin-gonic/gin"
)

func main() {

	fmt.Println("$IRD rocks!")

	engine := gin.Default()
	engine.Use(static.Serve("/", static.LocalFile("./webapp/dist/webapp", true)))
	engine.Run(":3000")
}
