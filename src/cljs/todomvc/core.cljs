(ns todomvc.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [clojure.string :as str :refer [trim]]
   [cljs.core.async :as async :refer [put! chan <! >! close!]]
   [reagent.core :as reagent :refer [atom]]))

(def present? (complement clojure.string/blank?))
(def enter-key 13)
(def escape-key 27)

(defonce actions (chan))
(defonce new-todo-buffer (atom ""))
(defonce edit-todo-buffer (atom ""))
(defonce state (atom {:editing null
                      :todos [{:completed false :content "foo"}
                              {:completed false :content "bar"}]}))

(defn log [x]
  (.log js/console (clj->js x))
  x)

(defn remove-nth [n coll]
  (vec (concat (subvec coll 0 n) (subvec coll (inc n)))))

(defn toggle-completed! [state idx]
  (update-in state [:todos idx :completed] not))

(defn add-todo! [state todo-content]
  (reset! new-todo-buffer "")
  (update-in state [:todos] #(conj % {:completed false :content todo-content})))

(defn remove-todo! [state idx]
  (update-in state [:todos] #(remove-nth idx %)))

(defn update-state! [action]
  (when action
    (case (:action action)
      :add-todo (swap! state add-todo! (:value action))
      :destroy-todo (swap! state remove-todo! (:idx action))
      :edit-todo (do
                   (reset! edit-todo-buffer (:content action))
                   (swap! state assoc-in [:editing] (:idx action)))
      :save-todo (do
                   (reset! edit-todo-buffer "")
                   (swap! state assoc-in [:editing] nil)
                   (swap! state assoc-in [:todos (:idx action)] (:content action)))
      :cancel-edit (do
                     ;; TODO - de-dupe
                     (swap! state assoc-in [:editing] nil)
                     (reset! edit-todo-buffer ""))
      )))

(defn add-todo-action [event]
  (let [value (trim (aget event "target" "value"))]
    (when (and (= enter-key (.-keyCode event))
               (present? value))
      {:action :add-todo :value value})))

(defn destroy-todo-action [idx]
  {:idx idx :action :destroy-todo})

(defn edit-todo-action [idx content]
  {:idx idx :action :edit-todo :content content})

(defn save-todo-action [idx event]
  ;; todo - dedupe
  (let [value (trim (aget event "target" "value"))]
    (when (and (= enter-key (.-keyCode event))
               (present? value))
      {:idx idx :action :save-todo :content value})))

(defn on-key-down [idx event]
  (.persist event)
  (queue-action (condp = (.-keyCode event)
                  enter-key {:idx idx :action :save-todo :content (aget event "target" "value")}
                  escape-key {:action :cancel-edit}
                  nil))
  )

(defn queue-action [action]
  (when action (put! actions action)))

(defn class-set [classes]
  (let [class (->> classes
                  (filter (fn [[k,v]] v))
                  (map (fn [[k v]] (name k)))
                  (str/join " ")
                  )]
    {:class class}))

(defn editing? [idx]
  (= idx (:editing @state)))

(defn completed? [idx]
   (-> @state
       :todos
       (get idx)
       :completed))

(defn todo-component [idx todo]
  (let [editing (editing? idx)
        completed (completed? idx)]
    [:li (merge {:key idx} (class-set {:editing editing :completed completed}))
     [:div {:class "view"}
      [:input {:class "toggle"
               :type "checkbox"
               :checked completed
               :on-click #(swap! state toggle-completed! idx)
               }]
      [:label
       {:on-double-click #(queue-action (edit-todo-action idx (:content todo)))}
       (:content todo)]
      [:button {:class "destroy"
                :on-click #(queue-action (destroy-todo-action idx))}]
      ]
     (when editing
         [:input {:class "edit"
                  :value @edit-todo-buffer
                  :on-change #(reset! edit-todo-buffer (-> % .-target .-value))
                  :on-key-down (partial on-key-down idx)
                  }
          ])
     ]))

(defn new-todo []
  [:input {:id "new-todo"
           :autocomplete="off"
           :placeholder "What needs to be done?"
           :value @new-todo-buffer
           ;; core.async + reagent isn't fast enough to keep
           ;; up with typing, so we edit the state directly instead
           ;; of putting the action on a channel
           :on-change #(reset! new-todo-buffer (-> % .-target .-value))
           :on-key-down #(queue-action (add-todo-action %))
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
       ;; force evaluation of lazy seq to avoid
       ;; "Reactive deref not supported in seq" warning
       (doall (map-indexed todo-component (-> @state :todos)))
      ]
     ]]]
   [:footer {:id "info"}
    [:p @edit-todo-buffer]
    [:p "Double-click to edit a todo"]]
   ])

(defn main []
  (reagent/render-component [app] (.getElementById js/document "app"))
  (go (while true
         (let [action (<! actions)]
           (update-state! action)))))
