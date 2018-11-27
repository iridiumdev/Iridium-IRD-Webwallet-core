package wallet

import (
	"github.com/gin-gonic/gin"
	"github.com/iridiumdev/webwallet-core/auth"
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
		api.POST("/:id/instance", controller.postInstanceHandler())
		api.DELETE("/:id/instance", controller.deleteInstanceHandler())
	}
}

func (controller *Controller) getListHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		userId := auth.ExtractUserId(c)
		wallets, err := service.GetWallets(userId)
		if !handleWalletErrors(c, err) {
			c.JSON(http.StatusOK, wallets)
		}
	}
}

func (controller *Controller) getHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		userId := auth.ExtractUserId(c)
		walletId := c.Param("id")

		wallet, err := service.GetWallet(walletId, userId)
		if !handleWalletErrors(c, err) {
			c.JSON(http.StatusOK, wallet)
		}
	}
}

func (controller *Controller) postInstanceHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		userId := auth.ExtractUserId(c)
		walletId := c.Param("id")

		dto := PasswordDTO{}
		util.BindAndHandleError(c, &dto, http.StatusBadRequest)

		wallet, err := service.StartWallet(walletId, dto.Password, userId)
		if !handleWalletErrors(c, err) {
			c.JSON(http.StatusCreated, wallet)
		}
	}
}

func (controller *Controller) deleteInstanceHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		userId := auth.ExtractUserId(c)
		walletId := c.Param("id")

		wallet, err := service.StopWallet(walletId, userId)
		if !handleWalletErrors(c, err) {
			c.JSON(http.StatusOK, wallet)
		}
	}
}

func (controller *Controller) postCreateHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		imp := ImportDTO{}
		util.BindAndHandleError(c, &imp, http.StatusBadRequest)

		userId := auth.ExtractUserId(c)

		var wallet *DetailedWallet
		var err error
		if imp.SpendSecretKey == "" || imp.ViewSecretKey == "" {
			wallet, err = service.CreateWallet(imp.CreateDTO, userId)
		} else {
			wallet, err = service.ImportWallet(imp, userId)
		}

		if !handleWalletErrors(c, err) {
			c.JSON(http.StatusCreated, wallet)
		}
	}
}

func handleWalletErrors(c *gin.Context, err error) bool {
	if err == ErrWalletNotFound {
		return util.HandleError(c, err, http.StatusNotFound)
	}
	if err == ErrWalletNotRunning {
		return util.HandleError(c, err, http.StatusFailedDependency)
	}

	return util.HandleError(c, err, http.StatusBadRequest)

}
