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

;; Capture console.log because scasp outputs natural language justifications
;; there.
(capture-js-console-log!)

(enable-console-print!)

(defn run-scasp-query!
  "Asynchronously loads a swi-prolog interpreter with Scasp and scasp-program-str
   and then runs scasp-query-str.
   Returns a channel containing a stream of results of the form
   {:clj _ :natlang _}"
  [scasp-program-str scasp-query-str]
  (go
    (let [scasp-program-str
          (str ":- ['resources/swipl/scasp.qlf'].
                "
               scasp-program-str)
          scasp-query-str
          (str "scasp(" scasp-query-str ", [tree(Justification_tree)]),"
               #_"human_model(Model, []),"
               "human_justification_tree(Justification_tree, [])")

          ;; Load the swi-prolog wasm build and then load the scasp program into it.
          ;; We run this synchronously, blocking until it completes.
          swipl (<p! (js/SWIPL (clj->js {:arguments ["-q"]})))
          _ (<p! (.load_string (.-prolog swipl) scasp-program-str))

          ;; Given a generator obtained from running an swi-prolog query,
          ;; we construct a corecursive lazy-seq of results, each containing 
          ;; the clj datatype and the natural language output.
          ;; Before generating each result, we clear console-log and
          ;; capture Scasp's console.log output.
          results-gen->seq-with-natlang
          (fn [results-gen]
            (repeatedly
             #(do (clear-console-log!)
                  (when-let [result (-> results-gen (.next) (.-value))]
                    (let [[model justification-tree]
                          (->> @console-log
                               (split-with (partial re-find #"^\s*•"))
                               (mapv (partial str/join "\n")))]
                      {:json (.stringify js/JSON result)
                       ;; (js->clj result :keywordize-keys true)
                       :natlang {:model model
                                 :justification-tree justification-tree}})))))]
      ;; Run the query and return the lazy-seq of results.
      (->> scasp-query-str
           (.query (.-prolog swipl))
           results-gen->seq-with-natlang
           (into [] (take 1))
           #_(take-while some?)))))