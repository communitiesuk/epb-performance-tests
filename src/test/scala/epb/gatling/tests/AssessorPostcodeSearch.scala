package epb.gatling.tests

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class AssessorPostcodeSearch extends Simulation {

  private val httpProtocol = http
    .baseUrl("https://getting-new-energy-certificate-staging.digital.communities.gov.uk")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-GB,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:105.0) Gecko/20100101 Firefox/105.0")

  private val headers_0 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
    "Upgrade-Insecure-Requests" -> "1",
    "Referer" -> "https://getting-new-energy-certificate-staging.digital.communities.gov.uk"
  )

  private val headers_4 = Map(
    "Origin" -> "https://getting-new-energy-certificate-staging.digital.communities.gov.uk",
    "Referer" -> "https://getting-new-energy-certificate-staging.digital.communities.gov.uk",
    "Upgrade-Insecure-Requests" -> "1"
  )

  private val postcodeFeeder = csv("postcodes.csv").random()

  private val scn = scenario("PostcodeSearch")
    .exec(
      http("type of property form")
        .get("/find-an-assessor/type-of-property")
        .headers(headers_0)
    )
    .pause(7)
    .exec(
      http("submit type of property form")
        .post("/find-an-assessor/type-of-property")
        .headers(headers_4)
        .formParam("lang", "en")
        .formParam("property_type", "domestic")
    )
    .pause(5)
    .exec(
      http("submit existing or new property form")
        .post("/find-an-assessor/type-of-domestic-property")
        .headers(headers_4)
        .formParam("lang", "en")
        .formParam("domestic_type", "domesticRdSap")
    )
    .pause(5)
    .feed(postcodeFeeder)
    .exec(
      http("search assessors by postcode")
        .get("/find-an-assessor/search-by-postcode?domestic_type=domesticRdSap&postcode#{postcode}")
        .headers(headers_0)
    )

  setUp(scn.inject(atOnceUsers(30))).protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile(90).lt(5000),
      global.failedRequests.count.lte(0),
      global.requestsPerSec.lt(500.0)
    )
}
