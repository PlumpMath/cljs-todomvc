(ns hello-clojurescript
  (:require [clojure.browser.repl :as repl]
            [figwheel.client :as fw :include-macros true]
            ))

(repl/connect "http://localhost:9000/repl")
(enable-console-print!)

(println "You can change this line an see the changes in the dev console")
(fw/watch-and-reload
  ;; :websocket-url "ws://localhost:3449/figwheel-ws" default
  :jsload-callback (fn [] (print "reloaded"))) ;; optional callback

(defn handle-click []
  (js/alert "Hi!"))

(def clickable (.getElementById js/document "clickable"))
(.addEventListener clickable "click" handle-click)
