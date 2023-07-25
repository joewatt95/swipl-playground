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

(defn run-scasp-query!
  "Asynchronously loads a swi-prolog interpreter with Scasp and scasp-program-str
   and then runs scasp-query-str.
   Returns a channel containing a stream of results of the form
   {:clj _ :natlang _}"
  [scasp-program-str scasp-query-str]
  (go
    ;; Capture console.log because scasp outputs natural language justifications
    ;; there.
    (capture-js-console-log!)
    (let [scasp-program-str
          (str ":- ['resources/scasp/scasp_human.qlf'].\n" scasp-program-str)
          scasp-query-str
          (str "scasp(" scasp-query-str ", [model(_Model), tree(Tree)]),"
               "human_justification_tree(Tree, [])")

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
                    {:clj (js->clj result :keywordize-keys true)
                     :natlang (str/join "\n" @console-log)}))))]
      ;; Run the query and return the lazy-seq of results.
      (->> scasp-query-str
           (.query (.-prolog swipl))
           results-gen->seq-with-natlang
           (take-while some?)))))