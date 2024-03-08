# Round-robin load balancer

## Description

This is a simple round-robin load balancer which routes
requests to underlying servers in a round-robin manner.
An echo server which echoes back JSON POST requests is
also provided under this project. Endpoints for backend
servers can be configured in applications-dev.properties.

## Scenarios

### One of the backend server goes down

If this happens, the load balancer will fail over and
attempt to get a response from other servers in a round-robin
manner until it is able to get a valid response.
If all servers are down, a response with an HTTP status code
of 500 will be returned with an error message indicating that
all backend servers are down.

### One of the backend servers start to go slowly

Timeouts are configured in the HttpClient. If a request to one
server timeouts, the load balancer will route the request to another
server until it is able to get a valid response.
