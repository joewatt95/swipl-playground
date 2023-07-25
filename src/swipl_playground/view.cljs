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

(hj/defc scasp-justifications "")

;; (def query "payAmt(X)")
;; (def query1 "p(X)")

(defn update-scasp-justifications! []
  (reset! scasp-justifications "")
  (hj/dosync
   (go
     (doseq [{:keys [clj natlang]}
             (<! (run-scasp-query! @swipl-program @scasp-query))]
       (swap! scasp-justifications
              #(str %
                    "\nEDN:\n" clj "\n\n"
                    "Natural language:\n" natlang))))))

(h/defelem html [_attrs _children]
  (h/div
   (h/link :rel "stylesheet"
           :href "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
           :integrity "sha384-9ndCyUaIbzAi2FUVXJi0CjmCapSmO7SnpJef0486qhLnuZ2cdeRhO02iuK6FUUVM"
           :crossorigin "anonymous")
    ;;  (h/script :src "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"
    ;;            :integrity "sha384-geWF76RCwLtnZ8qwWowPQNguL3RmwHVBC9FhGdlKrxdiJJigb/j/68SIy3Te4Bkz"
    ;;            :crossorigin "anonymous")
    ;; SWIPL stuff
   (h/script :src "https://SWI-Prolog.github.io/npm-swipl-wasm/3/4/5/index.js")

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
                           :click #(update-scasp-justifications!)
                           (h/text "Run query")))
          (h/div (h/text "Justifications")
                 (h/div :id "scasp-justifications" :style "white-space: pre-wrap"
                        (h/text "~{scasp-justifications}"))))))