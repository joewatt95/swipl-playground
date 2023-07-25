(ns swipl-playground.evaluator
  (:require [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [clojure.string :as str]))

(def console-log (atom []))

(defn clear-console-log! [] (reset! console-log []))

(defn capture-js-console-log! []
  (set! js/console.stdlog (.bind js/console.log js/console))
  (set! js/console.log
        (fn [args]
          (swap! console-log conj args)
          (.call js/console.stdlog js/console args))))

(defn run-scasp-query! [scasp-program-str scasp-query-str]
  (go
    (let [scasp-program-str
          (str ":- ['resources/scasp/scasp_human.qlf'].\n" scasp-program-str)
          scasp-query-str
          (str "scasp(" scasp-query-str ", [model(_Model), tree(Tree)]),"
               "human_justification_tree(Tree, [])")

          swipl (<p! (js/SWIPL (clj->js {:arguments ["-q"]})))
          _ (<p! (.load_string (.-prolog swipl) scasp-program-str))

          results-gen->seq-with-natlang
          (fn [results-gen]
            (repeatedly
             #(do (clear-console-log!)
                  (when-let [result (-> results-gen (.next) (.-value))]
                    {:result (js->clj result :keywordize-keys true)
                     :natlang (str/join "\n" @console-log)}))))]
      (capture-js-console-log!)
      (->> scasp-query-str
           (.query (.-prolog swipl))
           results-gen->seq-with-natlang
           (take-while some?)))))