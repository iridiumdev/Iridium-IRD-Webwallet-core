package main

import (
	"flag"
	"fmt"
	"github.com/DATA-DOG/godog"
	"github.com/DATA-DOG/godog/colors"
	"github.com/iridiumdev/webwallet-core/test"
	"github.com/onsi/gomega"
	"gopkg.in/resty.v1"
	"net/http/httptest"
	"os"
	"testing"
)

var opt = godog.Options{Output: colors.Colored(os.Stdout), Format: "pretty"}

func TestMain(m *testing.M) {
	flag.Parse()
	opt.Paths = flag.Args()
	if len(opt.Paths) == 0 {
		opt.Paths = append(opt.Paths, "test/features")
	}

	gomega.RegisterFailHandler(func(message string, callerSkip ...int) {
		panic(message)
	})

	opt.Tags = "~@ignore"

	status := godog.RunWithOptions("godogs", func(s *godog.Suite) {
		FeatureContext(s)
	}, opt)

	if st := m.Run(); st > status {
		status = st
	}
	os.Exit(status)
}

func FeatureContext(s *godog.Suite) {

	apiFeature := &test.ApiFeature{}
	resty.SetRedirectPolicy(resty.FlexibleRedirectPolicy(15))
	resty.SetHeader("Content-Type", "application/json")

	mongoSession := initMongoClient()
	dockerClient := initDockerClient()

	initStores(mongoSession)
	initServices(dockerClient)

	engine, _ := initMainEngine()

	ts := httptest.NewServer(engine)
	apiFeature.BaseUrl = ts.URL

	fmt.Print(ts.URL)

	//s.BeforeScenario(func(interface{}) {
	//
	//})

	s.Step(`^I send a (GET|DELETE) request to (\/[\S\/]*)$`, apiFeature.IDoARequest)
	s.Step(`^I send a (POST|PUT) request to (\/[\S\/]*) with body:$`, apiFeature.IDoARequestWithBody)
	s.Step(`^the response should be (\d+) and match this json:$`, apiFeature.TheResponseShouldBeAndMatchThisJson)
	s.Step(`^the response should be (\d+)$`, apiFeature.TheResponseShouldBe)

	s.Step(`^I keep the JSON response at "([^"]*)" as "([^"]*)"$`, apiFeature.KeepJSONResponseAt)
}
