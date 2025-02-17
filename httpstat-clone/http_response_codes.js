const httpCodes ={
  "100": {
    "message": "Continue",
    "use_case": "The server acknowledges part of the request and expects the client to continue sending the request body."
  },
  "101": {
    "message": "Switching Protocols",
    "use_case": "The server agrees to switch protocols as requested by the client (e.g., upgrading from HTTP to WebSockets)."
  },
  "102": {
    "message": "Processing",
    "use_case": "Used in WebDAV to indicate that the server is still processing a long-running request."
  },
  "103": {
    "message": "Early Hints",
    "use_case": "Used to provide hints about resources that will be required later in the request."
  },
  "200": {
    "message": "OK",
    "use_case": "The request was successful, and the response contains the requested data."
  },
  "201": {
    "message": "Created",
    "use_case": "A new resource was successfully created (e.g., after a POST request)."
  },
  "202": {
    "message": "Accepted",
    "use_case": "The request has been accepted for processing but is not completed yet."
  },
  "203": {
    "message": "Non-Authoritative Information",
    "use_case": "The response is modified from the origin server but still valid."
  },
  "204": {
    "message": "No Content",
    "use_case": "The request was successful, but there is no content to return (e.g., a DELETE request)."
  },
  "205": {
    "message": "Reset Content",
    "use_case": "The client should reset the document view (e.g., a form reset)."
  },
  "206": {
    "message": "Partial Content",
    "use_case": "The server is delivering part of the requested resource due to a range request."
  },
  "207": {
    "message": "Multi-Status",
    "use_case": "A WebDAV-specific response that provides multiple independent status codes for different parts of a request."
  },
  "208": {
    "message": "Already Reported",
    "use_case": "Used in WebDAV to indicate that members of a DAV binding have already been enumerated in a previous response."
  },
  "226": {
    "message": "IM Used",
    "use_case": "Indicates the server applied instance manipulations to the resource before responding."
  },
  "300": {
    "message": "Multiple Choices",
    "use_case": "The requested resource has multiple representations, and the client must choose one."
  },
  "301": {
    "message": "Moved Permanently",
    "use_case": "The resource has been permanently moved to a new URL."
  },
  "302": {
    "message": "Found",
    "use_case": "The resource is temporarily available at a different URL."
  },
  "303": {
    "message": "See Other",
    "use_case": "The response can be found at another URL, and the client should use a GET request to retrieve it."
  },
  "304": {
    "message": "Not Modified",
    "use_case": "Indicates the resource has not changed and the cached version should be used."
  },
  "305": {
    "message": "Use Proxy (Deprecated)",
    "use_case": "Previously used to indicate a resource must be accessed through a proxy. Deprecated due to security concerns."
  },
  "306": {
    "message": "Unused",
    "use_case": "This status code is reserved and is not used."
  },
  "307": {
    "message": "Temporary Redirect",
    "use_case": "Similar to 302, but ensures the request method is not changed."
  },
  "308": {
    "message": "Permanent Redirect",
    "use_case": "Similar to 301 but ensures the request method is not changed."
  },
  "400": {
    "message": "Bad Request",
    "use_case": "The server cannot process the request due to a client error (e.g., malformed request syntax)."
  },
  "401": {
    "message": "Unauthorized",
    "use_case": "Authentication is required for the request."
  },
  "402": {
    "message": "Payment Required",
    "use_case": "Reserved for future use, often associated with digital transactions."
  },
  "403": {
    "message": "Forbidden",
    "use_case": "The server understands the request but refuses to authorize it."
  },
  "404": {
    "message": "Not Found",
    "use_case": "The requested resource was not found on the server."
  },
  "405": {
    "message": "Method Not Allowed",
    "use_case": "The request method is not allowed on the target resource."
  },
  "406": {
    "message": "Not Acceptable",
    "use_case": "The requested resource is not available in a format acceptable to the client."
  },
  "407": {
    "message": "Proxy Authentication Required",
    "use_case": "The client must authenticate with a proxy before making the request."
  },
  "408": {
    "message": "Request Timeout",
    "use_case": "The client took too long to send a request."
  },
  "409": {
    "message": "Conflict",
    "use_case": "The request conflicts with the current state of the resource."
  },
  "410": {
    "message": "Gone",
    "use_case": "The resource is no longer available and has been permanently removed."
  },
  "411": {
    "message": "Length Required",
    "use_case": "The request must include a valid Content-Length header."
  },
  "412": {
    "message": "Precondition Failed",
    "use_case": "One or more conditions in the request header fields failed."
  },
  "413": {
    "message": "Payload Too Large",
    "use_case": "The request payload exceeds the server's allowed limit."
  },
  "414": {
    "message": "URI Too Long",
    "use_case": "The URI requested is too long for the server to process."
  },
  "415": {
    "message": "Unsupported Media Type",
    "use_case": "The media type of the request is not supported by the server."
  },
  "416": {
    "message": "Range Not Satisfiable",
    "use_case": "The requested range cannot be served."
  },
  "417": {
    "message": "Expectation Failed",
    "use_case": "The expectation given in the request header cannot be met by the server."
  },
  "418": {
    "message": "I'm a teapot",
    "use_case": "An April Fools' joke from RFC 2324 (Hyper Text Coffee Pot Control Protocol)."
  },
  "421": {
    "message": "Misdirected Request",
    "use_case": "The request was sent to a server that cannot produce a valid response."
  },
  "422": {
    "message": "Unprocessable Entity",
    "use_case": "The request was well-formed but could not be processed due to semantic errors (used in WebDAV)."
  },
  "423": {
    "message": "Locked",
    "use_case": "The requested resource is locked and cannot be modified (used in WebDAV)."
  },
  "424": {
    "message": "Failed Dependency",
    "use_case": "A request failed due to the failure of a previous request it depended on (used in WebDAV)."
  },
  "429": {
    "message": "Too Many Requests",
    "use_case": "The client has sent too many requests in a given time (rate limiting)."
  },
  "500": {
    "message": "Internal Server Error",
    "use_case": "A generic error indicating the server encountered an unexpected condition."
  },
  "501": {
    "message": "Not Implemented",
    "use_case": "The server does not support the functionality required to fulfill the request."
  },
  "502": {
    "message": "Bad Gateway",
    "use_case": "The server, while acting as a gateway or proxy, received an invalid response from an upstream server."
  },
  "503": {
    "message": "Service Unavailable",
    "use_case": "The server is temporarily unavailable, usually due to maintenance or overload."
  },
  "504": {
    "message": "Gateway Timeout",
    "use_case": "The server, acting as a gateway, did not receive a timely response from the upstream server."
  },
  "505": {
    "message": "HTTP Version Not Supported",
    "use_case": "The server does not support the HTTP version used in the request."
  },
  "506": {
    "message": "Variant Also Negotiates",
    "use_case": "A misconfiguration caused the server to enter a negotiation loop when choosing a resource variant."
  },
  "507": {
    "message": "Insufficient Storage",
    "use_case": "The server cannot store the representation needed to complete the request (used in WebDAV)."
  },
  "508": {
    "message": "Loop Detected",
    "use_case": "The server detected an infinite loop while processing the request (used in WebDAV)."
  },
  "510": {
    "message": "Not Extended",
    "use_case": "Further extensions to the request are required for it to be fulfilled."
  },
  "511": {
    "message": "Network Authentication Required",
    "use_case": "The client must authenticate to gain network access (often used in captive portals)."
  }
};

module.exports = httpCodes;