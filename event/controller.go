package event

import (
	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
	"github.com/iridiumdev/gin-jwt"
	"github.com/iridiumdev/webwallet-core/auth"
	"github.com/iridiumdev/webwallet-core/event/ws"
	log "github.com/sirupsen/logrus"
	"net/http"
)

type Controller struct {
	apiRouter *gin.RouterGroup
	auth      *jwt.GinJWTMiddleware
}

var upgrader = &websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
}

func NewController(apiRouter *gin.RouterGroup) Controller {
	return Controller{
		apiRouter: apiRouter,
	}
}

// Routes registers this controllers sub-routing in the main apiRouter. It returns a RouterGroup containing only the
// routes for the HTTP upgrade operations required for the websocket eventing.
func (controller *Controller) Routes() {

	api := controller.apiRouter.Group("/events")
	{
		api.GET("/connect", controller.websocketUpgradeHandler())
	}

}

func (controller *Controller) websocketUpgradeHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		userId := auth.ExtractUserId(c)

		wsConn, err := upgrader.Upgrade(c.Writer, c.Request, nil)
		if err != nil {
			log.Printf("error upgrading %s", err)
			return
		}

		conn := ws.NewConnection(wsConn)

		wg := service.WSHub().AddConnection(userId, conn)
		defer service.WSHub().CloseConnection(userId, conn)

		go conn.Writer()
		go conn.Reader()
		wg.Wait()
	}
}
