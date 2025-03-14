package com.example.cacheproxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;


import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Slf4j
@Command(name = "caching-proxy", mixinStandardHelpOptions = true, description = "CLI that serve as a proxy to cache http requests", subcommands = {
        CleanCacheCommand.class })
public class ProxyCommand implements Callable<Integer>{

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  @Option(names = { "-o", "--origin" }, description = "Endpoint")
  private String url = "";

  @Override
  public Integer call() {
    RestClient restClient = RestClient.create();

    if (url != null && this.redisTemplate.hasKey(url)) {
      var cachedResponse = this.redisTemplate.opsForValue().get(url);
      ResponseEntity<Object> response = ResponseEntity.ok().header("x-cache", "HIT").body(cachedResponse);
      System.out.println(response);

      return 1;
    }

    try {
      var targetUrl = new URI(url);
      ResponseEntity<String> response = restClient.get().uri(targetUrl).accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .toEntity(String.class);
      this.redisTemplate.opsForValue().append(url, response.getBody());

      return 1;
    } catch (RestClientException | URISyntaxException | IllegalArgumentException e) {
      e.printStackTrace();
      return 0;
    }
  }


}
