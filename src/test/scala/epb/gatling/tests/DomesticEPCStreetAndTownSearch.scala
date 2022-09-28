package epb.gatling.tests

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class DomesticEPCStreetAndTownSearch extends Simulation {

  private val httpProtocol = http
    .baseUrl("https://find-energy-certificate-staging.digital.communities.gov.uk")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-GB,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:105.0) Gecko/20100101 Firefox/105.0")

  private val headers_0 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
    "Upgrade-Insecure-Requests" -> "1",
    "Referer" -> "https://find-energy-certificate-staging.digital.communities.gov.uk"
  )

  private val headers_4 = Map(
    "Origin" -> "https://find-energy-certificate-staging.digital.communities.gov.uk",
    "Referer" -> "https://find-energy-certificate-staging.digital.communities.gov.uk",
    "Upgrade-Insecure-Requests" -> "1"
  )

  private val streetAndTownFeeder = csv("streetsandtowns.csv").random()

  private val scn = scenario("StreetAndTownSearch")
    .exec(
      http("search by street and town form")
        .get("/find-a-certificate/search-by-street-name-and-town")
        .headers(headers_0)
    )
    .pause(7)
    .feed(streetAndTownFeeder)
    .exec(
      http("search domestic epc by street and town")
        .get("/find-a-certificate/search-by-street-name-and-town?street_name=#{street}&town=#{town}")
        .headers(headers_0)
        .check(css("tbody > tr.govuk-table__row:first a", "href").optional.saveAs("certificateUrl"))
    )
    .doIf("#{certificateUrl.isUndefined()}") {
      pause(3)
          .exec { session =>
            println("Did not get a certificate for: " + session("street").as[String] + ", " + session("town").as[String])
            session
          }
    }
    .doIf("#{certificateUrl.exists()}") {
      pause(3)
        .exec(
          http("fetch domestic epc page")
            .get("#{certificateUrl}")
            .headers(headers_0)
        )
    }

  setUp(scn.inject(atOnceUsers(5))).protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile(90).lt(5000),
      global.failedRequests.count.lte(0),
      global.requestsPerSec.lt(500.0)
    )
}
