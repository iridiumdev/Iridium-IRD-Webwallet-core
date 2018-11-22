package auth

import (
	"github.com/gin-gonic/gin"
	"github.com/iridiumdev/gin-jwt"
	"github.com/iridiumdev/webwallet-core/user"
	"github.com/iridiumdev/webwallet-core/util"
	"github.com/pkg/errors"
	"net/http"
	"time"
)

const (
	IdentityKey = "id"
)

func ExtractUserId(c *gin.Context) string {
	userIdRaw := jwt.ExtractClaims(c)[IdentityKey]
	if userIdRaw != nil && userIdRaw.(string) != "" {
		return userIdRaw.(string)
	} else {
		e := jwt.ErrFailedAuthentication
		util.HandleError(c, e, http.StatusUnauthorized)
		panic(e)
	}

}

func InitMiddleware(userService user.Service) *jwt.GinJWTMiddleware {

	// the jwt middleware

	authMiddleware, err := jwt.New(&jwt.GinJWTMiddleware{
		Realm:            "IRD WebWallet",
		SigningAlgorithm: "HS256",
		Key:              []byte("secret key"), // TODO: daniel 18.11.18 - change it to be really secret ;)
		Timeout:          30 * time.Minute,
		MaxRefresh:       24 * time.Hour,
		PayloadFunc: func(data interface{}) jwt.MapClaims {
			if v, ok := data.(*user.User); ok {
				return jwt.MapClaims{
					IdentityKey: v.Id,
					"username":  v.Username,
				}
			}
			return data.(jwt.MapClaims)
		},
		Authenticator: func(c *gin.Context) (interface{}, error) {

			var login user.Login
			if err := c.Bind(&login); err != nil {
				return nil, jwt.ErrMissingLoginValues
			}
			authUser, err := userService.AuthenticateUser(login)
			if err != nil {
				return nil, jwt.ErrFailedAuthentication
			}
			return authUser, nil

		},
		IdentityKey: IdentityKey,
		Unauthorized: func(c *gin.Context, code int, message string) {
			util.HandleError(c, errors.New(message), code)
		},
		// TokenLookup is a string in the form of "<source>:<name>" that is used
		// to extract token from the request.
		// Optional. Default value "header:Authorization".
		// Possible values:
		// - "header:<name>"
		// - "query:<name>"
		// - "cookie:<name>"
		TokenLookup: "header:Authorization",
		// TokenLookup: "query:token",
		// TokenLookup: "cookie:token",

		// TokenHeadName is a string in the header. Default value is "Bearer"
		TokenHeadName: "Bearer",

		// TimeFunc provides the current time. You can override it to use another time value. This is useful for testing or if your server uses a different time zone than your tokens.
		TimeFunc: time.Now,
	})

	if err != nil {
		panic(err)
	}

	return authMiddleware
}
