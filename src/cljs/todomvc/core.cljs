(ns todomvc.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :as async :refer [put! chan <! >! close!]]
   [reagent.core :as reagent :refer [atom]]))

(def enter-key 13)
(defonce state (atom {:editing 0 :new-todo "" :todos ["foo", "bar", "baz", "bat"]}))
(defonce events (chan))

(defn log [x]
  (.log js/console (clj->js x)))

(defn add-todo [state todo]
  (-> state
      (assoc :new-todo "")
      (update-in [:todos] #(conj % todo))))

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
      :add-todo (swap! state add-todo (:value action))
      )))

(defn queue-event [event]
  (.persist event)
  (put! events event))

(defn todo [content]
  [:li
   [:div {:class "view"}
    [:input {:class "toggle"
             :type "checkbox"}]
    [:label content]
    [:button {:class "destroy"}]
    ]
   ])

(defn new-todo []
  [:input {:id "new-todo"
           :placeholder "What needs to be done?"
           :onKeyDown queue-event
           }
   ]
  )

(defn app []
  [:div
   [:section {:id "todoapp"}
    [:div
     [:header {:id "header"}
      [:h1 "todos"]
      (new-todo)
      ]
     [:section {:id "main"}
      [:input {:id "toggle-all"
               :type "checkbox"}]
      [:ul {:id "todo-list"}
       (map todo (-> @state :todos))
      ]
     ]]]
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
