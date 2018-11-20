package util

import "github.com/gin-gonic/gin"

func HandleError(c *gin.Context, err error, statusCode int) bool {
	if err != nil {
		c.JSON(statusCode, gin.H{"error": err.Error()})
		return true
	}
	return false
}

func BindAndHandleError(c *gin.Context, obj interface{}, statusCode int) bool {
	err := c.Bind(obj)
	if err != nil {
		c.JSON(statusCode, gin.H{"error": err.Error()})
		return true
	}
	return false
}
