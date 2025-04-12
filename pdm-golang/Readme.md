Steps to build & start golang server in podman contianer

- go env -w GO111MODULE=auto
- go mod init
- go mod tidy
- podman build -t pdm-golang .
- podman run --name pdm-golang -p 8080:8080 pdm-golang:latest

Reference - https://www.youtube.com/watch?v=YXfA5O5Mr18
