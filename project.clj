(defproject todomvc "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2197"]
                 [ring "1.3.1"]
                 [figwheel "0.1.5-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.10"]
            [lein-figwheel "0.1.5-SNAPSHOT"]]
  :hooks [leiningen.cljsbuild]
  :source-paths ["src/clj"]
  :cljsbuild {
    :builds {
      :main {
        :source-paths ["src/cljs"]
        :compiler {:output-to "resources/public/js/cljs.js"
                   :output-dir "resources/public/js/compiled/out"
                   :optimizations :none
                   :pretty-print true}
        :jar true}}}
  :main todomvc.server
  :ring {:handler todomvc.server/app}
  ;;:profiles {:dev {:plugins [[com.cemerick/austin "0.1.5"]]}}


  :figwheel {
   :http-server-root "public" ;; this will be in resources/
   :server-port 3449          ;; default

   ;; CSS reloading (optional)
   ;; :css-dirs has no default value
   ;; if :css-dirs is set figwheel will detect css file changes and
   ;; send them to the browser
   :css-dirs ["resources/public/css"]

   ;; Server Ring Handler (optional)
   ;; if you want to embed a ring handler into the figwheel http-kit
   ;; server
   ;;:ring-handler example.server/handler
  }
)
