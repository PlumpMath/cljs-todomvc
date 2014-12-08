(ns todomvc.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :as async :refer [put! chan <! >! close!]]
   [reagent.core :as reagent :refer [atom]]))

(def present? (complement clojure.string/blank?))
(def enter-key 13)

(defonce state (atom {:editing 0 :new-todo "" :todos ["foo", "bar", "baz", "bat"]}))
(defonce actions (chan))

(defn log [x]
  (.log js/console (clj->js x)))

(defn add-todo [state todo]
  (-> state
      (assoc :new-todo "")
      (update-in [:todos] #(conj % todo))))

(defn update-todo [m idx value]
  (assoc-in m [:todos idx] value))

(defn remove-nth [n coll]
  (vec (concat (subvec coll 0 n) (subvec coll (inc n)))))

(defn remove-todo [state idx]
  (update-in state [:todos] #(remove-nth idx %)))

(defn update-state [action]
  (when action
    (case (:action action)
      :add-todo (swap! state add-todo (:value action))
      :update-todo (swap! state assoc :new-todo (:value action))
      :destroy-todo (swap! state remove-todo (:idx action))
      )))

(defn keydown [event]
  (let [value (aget event "target" "value")]
    (when (and (= enter-key (.-keyCode event))
               (present? value))
      {:action :add-todo :value value})))

(defn input [event]
  {:action :update-todo :value (aget event "target" "value")})

(defn event->action [data event]
  (case (:action data)
    :add-todo (keydown event)
    :update-todo (input event)
    :destroy-todo data
    nil))

(defn queue-action [data event]
  (.persist event)
  (when-let [action (event->action data event)]
    (put! actions action)))

(defn todo [idx content]
  [:li
   [:div {:class "view"}
    [:input {:class "toggle"
             :type "checkbox"}]
    [:label content]
    [:button {:class "destroy"
              :onClick (partial queue-action {:idx idx :action :destroy-todo})}]
    ]
   ])

(defn new-todo []
  [:input {:id "new-todo"
           :placeholder "What needs to be done?"
           :value (:new-todo @state)
           :onChange (partial queue-action {:action :update-todo})
           :onKeyDown (partial queue-action {:action :add-todo})
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
       (map-indexed todo (-> @state :todos))
      ]
     ]]]
   [:footer {:id "info"}
    [:p "Double-click to edit a todo"]]
   ])

(defn main []
  (reagent/render-component [app] (.getElementById js/document "app"))
  _(go (while true
         (let [action (<! actions)]
           (update-state action)
           ))))
