package net.ofnir.spring.clojure.repl

import clojure.java.api.Clojure
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@SpringBootApplication
class ReplApplication {
    static void main(String[] args) {
        SpringApplication.run(ReplApplication, args)
    }
}

@Service
class ClojureRepl {

    @PostConstruct
    def init() {
        def require = Clojure.var('clojure.core', 'require')
        require.invoke(Clojure.read('net.ofnir.repl'))
        Clojure.var('clojure.core.server', 'start-server').invoke(
                Clojure.read("{:port 5555 :name spring-repl :accept clojure.core.server/repl}")
        )
    }

    @PreDestroy
    def destroy() {
        Clojure.var('clojure.core.server', 'stop-server').invoke(
                Clojure.read("{:name spring-repl}")
        )
    }

}

@Service
class ClojureBackedService {
    BigDecimal add(BigDecimal a, BigDecimal b) {
        Clojure.var('net.ofnir.repl', 'add').invoke(a, b)
    }
}

@RestController
class MathController {
    private final ClojureBackedService backend

    MathController(ClojureBackedService backend) {
        this.backend = backend
    }

    @PostMapping
    def add(BigDecimal a, BigDecimal b) {
        backend.add(a, b)
    }
}