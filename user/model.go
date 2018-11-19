package user

import "gopkg.in/mgo.v2/bson"

type User struct {
	Id       bson.ObjectId `json:"id" bson:"_id,omitempty"`
	Username string        `json:"username" binding:"required" bson:"username"`
	Email    string        `json:"email" binding:"required" bson:"email"`
	Password string        `json:"password" binding:"required,min=8" bson:"password"`
}

type Login struct {
	Username string `form:"username" json:"username" binding:"required"`
	Password string `form:"password" json:"password" binding:"required"`
}
