(ns todomvc.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :as async :refer [put! chan <! >! close!]]
   [reagent.core :as reagent :refer [atom]]))

(def state (atom {:editing 0 :todos []}))
(def event-chan (chan))

(defn log [x]
  (.log js/console x))

(defn update-todo [m idx value]
  (assoc-in m [:todos idx] value)
  )

(defn add-event [event]
  (put! event-chan event))

(defn app []
  [:div
   [:section {:id "todoapp"}
    [:div
     [:header {:id "header"}
      [:h1 "todos"]
      [:input {:id "new-todo"
               :placeholder "What needs to be done?"
               :onChange add-event}
       ]]]]
   [:footer {:id "info"}
    [:p "Double-click to edit a todo"]]
   ])

(defn main []
  (reagent/render-component [app] (.getElementById js/document "app"))
  _(go (while true
        (log (<! event-chan)))))
