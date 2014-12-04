(ns todomvc.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :as async :refer [put! chan <! >! close!]]
   [reagent.core :as reagent :refer [atom]]))

(def enter-key 13)
(defonce state (atom {:editing 0 :todos []}))
(defonce events (chan))

(defn log [x]
  (.log js/console (clj->js x)))

(defn update-todo [m idx value]
  (assoc-in m [:todos idx] value))

(defn keydown [event]
  (if (= enter-key (.-keyCode event))
    {:action :add-todo :value (aget event "target" "value")}))

(defn process-event [event]
  (case (.-type event)
    "keydown" (keydown event)
    nil))

(defn update-state [action]
  (when action
    (case (:action action)
      :add-todo (swap! state update-todo 0 (:value action))
      )))

(defn queue-event [event]
  (.persist event)
  (put! events event))

(defn app []
  [:div
   [:section {:id "todoapp"}
    [:div
     [:header {:id "header"}
      [:h1 "todos"]
      [:input {:id "new-todo"
               :placeholder "What needs to be done?"
               :onKeyDown queue-event
               ;;:onChange queue-event
               }
       ]]]]
   [:footer {:id "info"}
    [:p "Double-click to edit a todo"]]
   ])

(defn main []
  (reagent/render-component [app] (.getElementById js/document "app"))
  _(go (while true
         (let [event (<! events)]
           (log (process-event event))
           (update-state (process-event event))
           (log "state is")
           (log @state)
           ))))
