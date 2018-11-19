package user

import (
	"github.com/gin-gonic/gin"
	"github.com/iridiumdev/webwallet-core/util"
	"github.com/pkg/errors"
	"net/http"
)

type Controller struct {
	apiRouter  *gin.RouterGroup
	authRouter *gin.RouterGroup
}

func NewController(apiRouter *gin.RouterGroup, authRouter *gin.RouterGroup) Controller {
	return Controller{apiRouter: apiRouter, authRouter: authRouter}
}

// Routes registers this controllers sub-routing in the main apiRouter. It returns a RouterGroup containing only the
// routes for the operations on the User model.
func (controller *Controller) Routes() {
	controller.authRouter.POST("/register", controller.postRegisterHandler())
}

// TODO: daniel 08.11.18 - implement handler
func (controller *Controller) postRegisterHandler() gin.HandlerFunc {
	return func(c *gin.Context) {

		user := User{}
		util.HandleBindError(c, &user, http.StatusBadRequest)

		_, err := service.CreateUser(user)
		if err != nil {
			err = errors.New("registration failed")
		}
		if !util.HandleError(c, err, http.StatusBadRequest) {
			c.JSON(http.StatusCreated, []string{})
		}

	}
}
