package epb.gatling.tests

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class Searching extends Simulation {

  private val httpProtocolFindService = http
    .baseUrl(sys.env("FIND_CERT_STAGING_URL"))
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-GB,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:105.0) Gecko/20100101 Firefox/105.0")

  private val httpProtocolGetService = http
    .baseUrl(sys.env("GET_CERT_STAGING_URL"))
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-GB,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:105.0) Gecko/20100101 Firefox/105.0")

  private val headers_get_findService = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
    "Upgrade-Insecure-Requests" -> "1",
    "Referer" -> sys.env("FIND_CERT_STAGING_URL")
  )

  private val headers_post_findService = Map(
    "Origin" -> sys.env("FIND_CERT_STAGING_URL"),
    "Referer" -> sys.env("FIND_CERT_STAGING_URL"),
    "Upgrade-Insecure-Requests" -> "1"
  )

  private val headers_get_getService = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
    "Upgrade-Insecure-Requests" -> "1",
    "Referer" -> sys.env("GET_CERT_STAGING_URL")
  )

  private val headers_post_getService = Map(
    "Origin" -> sys.env("GET_CERT_STAGING_URL"),
    "Referer" -> sys.env("GET_CERT_STAGING_URL"),
    "Upgrade-Insecure-Requests" -> "1"
  )

  private val postcodeFeeder = csv("postcodes.csv").random()
  private val streetAndTownFeeder = csv("streetsandtowns.csv").random()

  private val scnDomesticPostcodeSearch = scenario("DomesticPostcodeSearch")
    .exec(
      http("type of property form")
        .get("/find-a-certificate/type-of-property")
        .headers(headers_get_findService)
    )
    .pause(7)
    .exec(
      http("submit type of property form")
        .post("/find-a-certificate/type-of-property")
        .headers(headers_post_findService)
        .formParam("lang", "en")
        .formParam("property_type", "domestic")
    )
    .pause(5)
    .feed(postcodeFeeder)
    .exec(
      http("search domestic epc by postcode")
        .get("/find-a-certificate/search-by-postcode?postcode=#{postcode}")
        .headers(headers_get_findService)
        .check(css("tbody > tr.govuk-table__row:first a", "href").optional.saveAs("certificateUrl"))
    )
    .doIf("#{certificateUrl.exists()}") {
      pause(3)
        .exec(
          http("fetch domestic epc page")
            .get("#{certificateUrl}")
            .headers(headers_get_findService)
        )
    }

  private val scnDomesticStreetAndTownSearch = scenario("DomesticStreetAndTownSearch")
    .exec(
      http("search by street and town form")
        .get("/find-a-certificate/search-by-street-name-and-town")
        .headers(headers_get_findService)
    )
    .pause(7)
    .feed(streetAndTownFeeder)
    .exec(
      http("search domestic epc by street and town")
        .get("/find-a-certificate/search-by-street-name-and-town?street_name=#{street}&town=#{town}")
        .headers(headers_get_findService)
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
            .headers(headers_get_findService)
        )
    }

  private val scnNonDomesticPostcodeSearch = scenario("NonDomesticPostcodeSearch")
    .exec(
      http("type of property form")
        .get("/find-a-certificate/type-of-property")
        .headers(headers_get_findService)
    )
    .pause(7)
    .exec(
      http("submit type of property form")
        .post("/find-a-certificate/type-of-property")
        .headers(headers_post_findService)
        .formParam("lang", "en")
        .formParam("property_type", "non_domestic")
    )
    .pause(5)
    .feed(postcodeFeeder)
    .exec(
      http("search non domestic energy certificates by postcode")
        .get("/find-a-certificate/search-by-postcode?postcode=#{postcode}")
        .headers(headers_get_findService)
        .check(css("tbody > tr.govuk-table__row:first a", "href").optional.saveAs("certificateUrl"))
    )
    .doIf("#{certificateUrl.exists()}") {
      pause(3)
        .exec(
          http("fetch non domestic energy certificate page")
            .get("#{certificateUrl}")
            .headers(headers_get_findService)
        )
    }

  private val scnAssessorPostcodeSearch = scenario("AssessorPostcodeSearch")
    .exec(
      http("type of property form")
        .get("/find-an-assessor/type-of-property")
        .headers(headers_get_getService)
    )
    .pause(7)
    .exec(
      http("submit type of property form")
        .post("/find-an-assessor/type-of-property")
        .headers(headers_post_getService)
        .formParam("lang", "en")
        .formParam("property_type", "domestic")
    )
    .pause(5)
    .exec(
      http("submit existing or new property form")
        .post("/find-an-assessor/type-of-domestic-property")
        .headers(headers_post_getService)
        .formParam("lang", "en")
        .formParam("domestic_type", "domesticRdSap")
    )
    .pause(5)
    .feed(postcodeFeeder)
    .exec(
      http("search assessors by postcode")
        .get("/find-an-assessor/search-by-postcode?domestic_type=domesticRdSap&postcode#{postcode}")
        .headers(headers_get_getService)
    )

  setUp(scnDomesticPostcodeSearch.inject(constantUsersPerSec(10).during(15).randomized).protocols(httpProtocolFindService),
    scnDomesticStreetAndTownSearch.inject(atOnceUsers(5)).protocols(httpProtocolFindService),
    scnNonDomesticPostcodeSearch.inject(constantUsersPerSec(10).during(15).randomized).protocols(httpProtocolFindService),
    scnAssessorPostcodeSearch.inject(constantUsersPerSec(10).during(15).randomized).protocols(httpProtocolGetService)
  )
    .assertions(
      global.responseTime.percentile(95).lt(5000),
      details("search non domestic energy certificates by postcode").responseTime.percentile(95).lt(4000),
      details("search domestic epc by postcode").responseTime.percentile(95).lt(4000),
      details("search domestic epc by street and town").responseTime.percentile(95).lt(10000),
      details("search assessors by postcode").responseTime.percentile(95).lt(4000),
      global.failedRequests.count.lte(0),
    )
}
