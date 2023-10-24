dlogin:
	@echo "Login to dockerhub with username: federatedbdi"
	docker login -u federatedbdi

dbuild:
	@echo "Building docker image and push it to dockerhub, usage: make dbuild tag=0.3.8"
	docker buildx build --push --platform linux/arm64/v8,linux/amd64 --tag federatedbdi/bdi-api:$(tag) .