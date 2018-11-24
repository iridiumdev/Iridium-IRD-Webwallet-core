package main

import (
	"flag"
	"github.com/DATA-DOG/godog"
	"github.com/DATA-DOG/godog/colors"
	"github.com/iridiumdev/webwallet-core/config"
	"github.com/iridiumdev/webwallet-core/test"
	"github.com/iridiumdev/webwallet-core/user"
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

	config.Get().Mongo.Database = "iridium-test"

	mongoSession := initMongoClient()
	dockerClient := initDockerClient()

	s.BeforeScenario(func(scenarioArg interface{}) {

		mongoSession.DB(config.Get().Mongo.Database).DropDatabase()

		initStores(mongoSession)
		userService, _ := initServices(dockerClient)

		engine, _, authMiddleware := initMainEngine(userService)

		ts := httptest.NewServer(engine)
		apiFeature.BaseUrl = ts.URL
		apiFeature.AuthMiddleware = authMiddleware

		testuser, _ := userService.CreateUser(user.User{Username: "testuser", Email: "test@ird.cash", Password: "secr3tPw"})

		apiFeature.TestUsers = map[string]*user.User{
			"testuser": testuser,
		}

		//scenario := scenarioArg.(*gherkin.Scenario)
		//scenario.Tags

	})

	s.Step(`^I am logged in as "([^"]*)"$`, apiFeature.IAmLoggedInAs)

	s.Step(`^I send a (GET|DELETE) request to "([^"]*)"$`, apiFeature.IDoARequest)
	s.Step(`^I reset the last response$`, apiFeature.ResetResponse)
	s.Step(`^I send a (POST|PUT) request to "([^"]*)" with body:$`, apiFeature.IDoARequestWithBody)
	s.Step(`^the response should be (\d+) and match this json:$`, apiFeature.TheResponseShouldBeAndMatchThisJson)
	s.Step(`^the response should be (\d+)$`, apiFeature.TheResponseShouldBe)

	s.Step(`^I keep the JSON response at "([^"]*)" as "([^"]*)"$`, apiFeature.KeepJSONResponseAt)
}
