package main

import (
	"fmt";
	"net/http"
)


func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {

		fmt.Fprintln(w, "Hello Podman from Go!")
	})
	http.ListenAndServe(":8080", nil)

}
