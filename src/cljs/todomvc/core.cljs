(ns todomvc.core
  (:require
   [reagent.core :as reagent :refer [atom]]))

(defonce state (atom []))

(defn app []
  [:div
   [:header {:id "header"}
    [:h1 "todos"]]])

(defn main []
  (reagent/render-component [app] (.getElementById js/document "app"))
  )
