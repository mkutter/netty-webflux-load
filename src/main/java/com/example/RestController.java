package com.example;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@org.springframework.web.bind.annotation.RestController
public class RestController {
	@PostMapping(value = "/api/echo", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Request> echo(@RequestBody Request request) {
		return Mono.just(request);
	}
}
