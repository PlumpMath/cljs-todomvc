(ns hello-clojurescript
  (:require [clojure.browser.repl :as repl]))

(repl/connect "http://localhost:9000/repl")

(defn handle-click []
  (js/alert "Hi!"))

(def clickable (.getElementById js/document "clickable"))
(.addEventListener clickable "click" handle-click)
