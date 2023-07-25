(ns swipl-playground.main
  (:require
    [swipl-playground.view :as view]
    [hoplon.goog]))

(defn mount-components! []
  (-> js/document
      (.getElementById "app")
      (.replaceChildren (view/html))))

(defn start []
  (mount-components!)
  (js/console.log "Starting..."))

(defn stop []
  (js/console.log "Stopping..."))

(defn init []
  (js/console.log "Initializing...")
  (start))
