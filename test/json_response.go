package test

import (
	"fmt"
	"github.com/tidwall/gjson"
	"strings"
)

type JSONSpec struct {
	JSONResponse string
	memory       map[string]gjson.Result
}

func NewJSONSpec(json string) *JSONSpec {
	return &JSONSpec{JSONResponse: json, memory: make(map[string]gjson.Result)}
}

func (spec *JSONSpec) KeepValue(path string, variable string) {
	value := gjson.Get(spec.JSONResponse, path)
	spec.memory[variable] = value
}

func (spec *JSONSpec) ReplaceFromMemory(template string) string {

	result := template

	for k, v := range spec.memory {
		result = strings.Replace(result, fmt.Sprintf("${%s}", k), v.Raw, -1)
	}

	return result
}
