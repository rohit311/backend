package com.example.cacheproxy;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

@Component
public class MyApplicationRunner implements CommandLineRunner, ExitCodeGenerator {

    private final ProxyCommand proxyCommand;

    private final IFactory factory; // auto-configured to inject PicocliSpringFactory

    private int exitCode;

    public MyApplicationRunner(ProxyCommand proxyCommand, IFactory factory) {
        this.proxyCommand = proxyCommand;
        this.factory = factory;
    }

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(proxyCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        // let Spring instantiate and inject dependencies
        System.exit(SpringApplication.exit(SpringApplication.run(CacheproxyApplication.class, args)));
    }
}
