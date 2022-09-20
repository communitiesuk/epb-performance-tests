.PHONY: test
test:
	@mvn gatling:test

.PHONY: test-ignore-failures
test-ignore-failures:
	@mvn gatling:test || true
