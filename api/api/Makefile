dl:
	@echo "Login to dockerhub with username: federatedbdi"
	docker login -u federatedbdi

dbp:
	@echo "Building docker image and push it to dockerhub, usage: make dbp tag=0.4.2"
	docker buildx build --push --platform linux/arm64/v8,linux/amd64 --tag federatedbdi/federated-node-api:$(tag) .