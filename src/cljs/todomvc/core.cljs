(ns todomvc.core
  (:require
   [reagent.core :as reagent :refer [atom]]))

(defonce state (atom []))

(defn app []
  [:div
   [:section {:id "todoapp"}
    [:div
     [:header {:id "header"}
      [:h1 "todos"]
      [:input {:id "new-todo"
               :placeholder "What needs to be done?"}
       ]]]]
   [:footer {:id "info"}
    [:p "Double-click to edit a todo"]]
   ])

(defn main []
  (reagent/render-component [app] (.getElementById js/document "app")))
