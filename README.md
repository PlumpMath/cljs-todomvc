# TodoMVC

### Hacking


```
lein run
lein cljsbuild auto
```

http://localhost:3000


#### REPL


https://github.com/clojure/clojurescript/wiki/The-REPL-and-Evaluation-Environments

```
(require '[cljs.repl :as repl])
(require '[cljs.repl.browser :as browser])  ;; require the browser implementation of IJavaScriptEnv
(def env (browser/repl-env)) ;; create a new environment
(repl/repl env) ;; start the REPL
```