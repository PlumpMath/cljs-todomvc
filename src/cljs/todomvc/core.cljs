(ns todomvc.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :as async :refer [put! chan <! >! close!]]
   [reagent.core :as reagent :refer [atom]]))

(def present? (complement clojure.string/blank?))
(def enter-key 13)

(defonce edit-buffer (atom ""))
(defonce state (atom {:editing 0 :todos ["foo", "bar", "baz", "bat"]}))
(defonce actions (chan))

(defn log [x]
  (.log js/console (clj->js x))
  x)

(defn add-todo [state todo]
  (reset! edit-buffer "")
  (-> state
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
      :destroy-todo (swap! state remove-todo (:idx action))
      )))

(defn add-todo-action [event]
  (let [value (aget event "target" "value")]
    (when (and (= enter-key (.-keyCode event))
               (present? value))
      {:action :add-todo :value value})))

(defn event->action [data event]
  (case (:action data)
    :add-todo (add-todo-action event)
    :destroy-todo data
    nil))

(defn queue-action [data event]
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
           :autocomplete="off"
           :placeholder "What needs to be done?"
           :value @edit-buffer
           ;; core.async + reagent isn't fast enough to keep
           ;; up with typing, so we edit the state directly instead
           ;; of putting the action on a channel
           :on-change #(reset! edit-buffer (-> % .-target .-value))
           :on-key-down (partial queue-action {:action :add-todo})
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
  (go (while true
         (let [action (<! actions)]
           (update-state action)
           ))))
