package main

import (
	"fmt"
	"github.com/gin-gonic/gin"
)

func main() {

	fmt.Println("$IRD rocks!")

	engine := gin.Default()
	engine.Run(":3000")
}
