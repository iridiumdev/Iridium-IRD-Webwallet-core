package wallet

import (
	"github.com/gin-gonic/gin"
	"github.com/iridiumdev/webwallet-core/util"
	"net/http"
)

type Controller struct {
	apiRouter *gin.RouterGroup
}

func NewController(apiRouter *gin.RouterGroup) Controller {
	return Controller{apiRouter: apiRouter}
}

// Routes registers this controllers sub-routing in the main apiRouter. It returns a RouterGroup containing only the
// routes for the operations on the Wallet model.
func (controller *Controller) Routes() {
	api := controller.apiRouter.Group("/wallets")
	{
		api.POST("/", controller.postCreateHandler())

		api.GET("/", controller.getListHandler())
		api.GET("/:id", controller.getHandler())
	}
}

// TODO: daniel 08.11.18 - implement handler
func (controller *Controller) getListHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.JSON(http.StatusOK, []string{})
	}
}

// TODO: daniel 08.11.18 - implement handler
func (controller *Controller) getHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		//id, _ := strconv.Atoi(c.Param("id"))
		c.JSON(http.StatusOK, struct{}{})
	}
}

func (controller *Controller) postCreateHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		imp := ImportDTO{}
		c.ShouldBind(&imp)
		var wallet *Wallet
		var err error
		if imp.SpendSecretKey == "" || imp.ViewSecretKey == "" {
			wallet, err = service.CreateWallet(imp.CreateDTO)
		} else {
			wallet, err = service.ImportWallet(imp)
		}

		if !util.HandleError(c, err, http.StatusBadRequest) {
			c.JSON(http.StatusCreated, wallet)
		}
	}
}
