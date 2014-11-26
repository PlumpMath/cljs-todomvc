(ns todomvc.core
  (:require
   [reagent.core :as reagent :refer [atom]]))

(defonce state (atom []))

(defn app []
  [:section {:id "todoapp"}
   [:div
    [:header {:id "header"}
     [:h1 "todos"]
     [:input {:id "new-todo"
              :placeholder "What needs to be done?"}
      ]]]])

(defn main []
  (reagent/render-component [app] (.getElementById js/document "app")))
