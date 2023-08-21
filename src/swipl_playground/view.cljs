(ns swipl-playground.view
  (:require [cljs.core.async :refer [go <!]]
            [hoplon.core :as h]
            [javelin.core :as hj]
            [swipl-playground.evaluator :refer [run-scasp-query!]]))

;; (enable-console-print!)

(hj/defc swipl-program
  "p(X) :- not(q(X)).
   p(1).

   q(X) :- not(p(X)).")

(hj/defc scasp-query "p(X)")

(hj/defc scasp-result "")

;; (def query "payAmt(X)")
;; (def query1 "p(X)")

(defn update-scasp-result! []
  (reset! scasp-result "")
  (hj/dosync
   (go
     (doseq [{json :json {:keys [model justification-tree]} :natlang}
             (<! (run-scasp-query! @swipl-program @scasp-query))]
       (swap! scasp-result
              #(str %
                    "\nJSON:\n" json "\n\n"
                    "Natural language model:\n" model "\n\n"
                    "Natural language justification:\n" justification-tree))))))

(h/defelem html [_attrs _children]
  (h/div
   (h/link :rel "stylesheet"
           :href "https://cdn.jsdelivr.net/npm/bootstrap@5.3.1/dist/css/bootstrap.min.css"
           :integrity "sha384-4bw+/aepP/YC94hEpVNVgiZdgIC5+VKNBQNGCHeKRQN+PtmoHDEXuppvnDJzQIu9"
           :crossorigin "anonymous")
    ;; SWIPL stuff
   (h/script :src "https://SWI-Prolog.github.io/npm-swipl-wasm/3/5/0/index.js")

   (h/title "SWI-Prolog playground")
   (h/h1 "SWI-Prolog playground")

   (h/div :class "form-group"
          (h/label :for "swipl-program"
                   :class "col-sm-1 control-label"
                   "Prolog Program")
          (h/textarea :class "form-control"
                      :id "swipl-program"
                      :rows 7
                      :value swipl-program
                      :change #(reset! swipl-program @%)))

   (h/div (h/h2 "Scasp")
          (h/div :class "form-group"
                 (h/label :for "scasp-query"
                          :class "col-sm-1 control-label"
                          "Query")
                 (h/input :class "form-control"
                          :id "scasp-query"
                          :type "text"
                          :value scasp-query
                          :change #(reset! scasp-query @%))
                 (h/button :class "btn btn-primary"
                           :click #(update-scasp-result!)
                           (h/text "Run query")))
          (h/div (h/text "Result")
                 (h/div :id "Natlang" :style "white-space: pre-wrap"
                        (h/text "~{scasp-result}"))))))