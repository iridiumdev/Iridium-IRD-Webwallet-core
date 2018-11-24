package main

import (
	"context"
	"flag"
	"fmt"
	"github.com/DATA-DOG/godog"
	"github.com/DATA-DOG/godog/colors"
	"github.com/docker/docker/api/types"
	"github.com/docker/docker/api/types/filters"
	"github.com/docker/docker/client"
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
	config.Get().Webwallet.Satellite.Labels["net"] = "testnet"
	labels := config.Get().Webwallet.Satellite.Labels

	mongoSession := initMongoClient()
	dockerClient := initDockerClient()

	s.BeforeSuite(func() {
		pruneTestWallets(dockerClient, labels)
	})

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

	s.AfterSuite(func() {
		pruneTestWallets(dockerClient, labels)
	})

	s.Step(`^I am logged in as "([^"]*)"$`, apiFeature.IAmLoggedInAs)
	s.Step(`^I create a test wallet with name "([^"]*)" and password "([^"]*)"$`, apiFeature.ICreateATestWalletWithNameAndPassword)

	s.Step(`^I send a (GET|DELETE) request to "([^"]*)"$`, apiFeature.IDoARequest)
	s.Step(`^I reset the last response$`, apiFeature.ResetResponse)
	s.Step(`^I send a (POST|PUT) request to "([^"]*)" with body:$`, apiFeature.IDoARequestWithBody)
	s.Step(`^the response should be (\d+) and match this json:$`, apiFeature.TheResponseShouldBeAndMatchThisJson)
	s.Step(`^the response should be (\d+)$`, apiFeature.TheResponseShouldBe)

	s.Step(`^I keep the JSON response at "([^"]*)" as "([^"]*)"$`, apiFeature.KeepJSONResponseAt)
}

func pruneTestWallets(dockerClient *client.Client, labels map[string]string) {

	ctx := context.Background()

	listFilters := filters.NewArgs()

	for k, v := range labels {
		listFilters.Add("label", fmt.Sprintf("%s=%s", k, v))
	}

	cList, err := dockerClient.ContainerList(ctx, types.ContainerListOptions{
		Limit:   -1,
		Filters: listFilters,
	})
	if err != nil {
		panic(err)
	}

	for _, container := range cList {

		err = dockerClient.ContainerRemove(ctx, container.ID, types.ContainerRemoveOptions{
			RemoveVolumes: true,
			Force:         true,
		})
		if err != nil {
			panic(err)
		}

		vol := container.Mounts[0].Name
		err = dockerClient.VolumeRemove(ctx, vol, true)
		if err != nil {
			panic(err)
		}
	}

}
