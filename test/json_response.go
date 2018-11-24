package test

import (
	"fmt"
	"github.com/iridiumdev/webwallet-core/user"
	"github.com/tidwall/gjson"
	"strings"
)

type JSONSpec struct {
	JSONResponse string
	currentUser  *user.User
	memory       map[string]gjson.Result
	authMemory   map[string]string
}

func NewJSONSpec(json string, currentUser *user.User) *JSONSpec {
	jsonSpec := &JSONSpec{
		JSONResponse: json,
		currentUser:  currentUser,
		memory:       make(map[string]gjson.Result),
		authMemory:   make(map[string]string),
	}
	if currentUser != nil {
		jsonSpec.AddAuthValue(fmt.Sprintf("%s.id", currentUser.Username), currentUser.Id.Hex())
		jsonSpec.AddAuthValue(fmt.Sprintf("%s.username", currentUser.Username), currentUser.Username)
		jsonSpec.AddAuthValue(fmt.Sprintf("%s.email", currentUser.Username), currentUser.Email)
	}

	return jsonSpec
}

func (spec *JSONSpec) KeepValue(path string, variable string) {
	value := gjson.Get(spec.JSONResponse, path)
	spec.memory[variable] = value
}

func (spec *JSONSpec) AddAuthValue(key string, value string) {
	spec.authMemory[key] = value
}

func (spec *JSONSpec) ReplaceFromMemory(template string) string {

	result := template

	for k, v := range spec.memory {
		result = strings.Replace(result, fmt.Sprintf("${%s}", k), v.Raw, -1)
	}

	for k, v := range spec.authMemory {
		result = strings.Replace(result, fmt.Sprintf("${%s}", k), fmt.Sprintf("\"%s\"", v), -1)
	}

	return result
}
