.PHONY: test
performance-test-staging:
	@mvn gatling:test

.PHONY: test-ignore-failures
performance-test-staging-ignore-failures:
	@mvn gatling:test || true
